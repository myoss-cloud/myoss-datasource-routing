/*
 * Copyright 2018-2019 https://github.com/myoss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package app.myoss.cloud.datasource.routing.spring.boot.jdbc.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;

import app.myoss.cloud.datasource.routing.config.DataSourceProperty;
import app.myoss.cloud.datasource.routing.constants.DataSourceRoutingConstants;
import app.myoss.cloud.datasource.routing.jdbc.GroupDataSource;
import app.myoss.cloud.datasource.routing.jdbc.MultiDataSource;
import app.myoss.cloud.datasource.routing.jdbc.loadbalancer.DataSourceLoadBalancer;
import app.myoss.cloud.datasource.routing.spring.boot.autoconfigure.DataSourceRoutingProperties;
import app.myoss.cloud.datasource.routing.spring.boot.jdbc.DataSourceBuilder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Convenience class for building a {@link MultiDataSource}
 *
 * @author Jerry.Chen
 * @since 2019年5月7日 下午10:53:09
 */
@Slf4j
@Setter
@Getter
@RequiredArgsConstructor
public class MultiDataSourceBuilder implements DataSourceBuilder, ApplicationContextAware, BeanClassLoaderAware {
    private JndiDataSourceBuilder       jndiDataSourceBuilder;
    private ApplicationContext          applicationContext;
    private ClassLoader                 beanClassLoader;
    @NonNull
    private DataSourceRoutingProperties dataSourceRoutingProperties;

    @Override
    public DataSource build() {
        List<DataSourceProperty> databases = dataSourceRoutingProperties.getDatabases();
        Map<String, DataSource> targetDataSources = new LinkedHashMap<>();
        LinkedMultiValueMap<String, DataSource> targetGroupDataSources = new LinkedMultiValueMap<>();
        String primaryDataSourceName = null;
        for (DataSourceProperty item : databases) {
            String name = item.getName();
            if (StringUtils.isBlank(name)) {
                throw new IllegalArgumentException("Property 'name' is required");
            }
            if (item.isPrimary()) {
                if (primaryDataSourceName != null) {
                    throw new IllegalArgumentException(
                            "Property 'primary' is already set 'true' in DataSource [" + primaryDataSourceName + "]");
                }
                primaryDataSourceName = name;
            }

            // create DataSource
            DataSource result;
            if (StringUtils.isNotBlank(item.getJndiName())) {
                if (jndiDataSourceBuilder == null) {
                    jndiDataSourceBuilder = new JndiDataSourceBuilder();
                }
                result = jndiDataSourceBuilder.build(item.getJndiName());
            } else {
                Map<String, Object> properties = (item.getProperties() != null ? item.getProperties()
                        : new LinkedHashMap<>(8));
                DefaultDataSourceBuilder<? extends DataSource> dataSourceBuilder = DefaultDataSourceBuilder
                        .create(properties, beanClassLoader)
                        .type(item.getType())
                        .name(name)
                        .url(item.getUrl())
                        .driverClassName(item.getDriverClassName())
                        .username(item.getUsername())
                        .password(item.getPassword());
                result = dataSourceBuilder.build();
            }
            DataSource previous = targetDataSources.putIfAbsent(name, result);
            if (previous != null) {
                throw new IllegalArgumentException("Property 'name' is already set '" + name + "'");
            }

            // group DataSource
            String groupName = item.getGroupName();
            if (StringUtils.isNotBlank(groupName)) {
                targetGroupDataSources.add(groupName, result);
            }
        }

        // create group DataSource
        for (Entry<String, List<DataSource>> entry : targetGroupDataSources.entrySet()) {
            DataSourceLoadBalancer dataSourceLoadBalancer = BeanUtils
                    .instantiateClass(dataSourceRoutingProperties.getGroupDataSourceLoadBalancer());
            String groupName = entry.getKey();
            GroupDataSource groupDataSource = new GroupDataSource(groupName, dataSourceLoadBalancer, entry.getValue());
            DataSource previous = targetDataSources.putIfAbsent(groupName, groupDataSource);
            if (previous != null) {
                throw new IllegalArgumentException("Property 'name' or 'groupName' is already set '" + groupName + "'");
            }
        }

        return new MultiDataSource(primaryDataSourceName, targetDataSources);
    }

    @Override
    public void onEnvironmentChangeEvent(Object event) {
        Binder binder = Binder.get(applicationContext.getEnvironment());
        binder.bind(DataSourceRoutingConstants.CONFIG_PREFIX, Bindable.ofInstance(dataSourceRoutingProperties));
    }
}
