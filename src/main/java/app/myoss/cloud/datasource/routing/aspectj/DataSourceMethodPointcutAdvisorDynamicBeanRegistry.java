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

package app.myoss.cloud.datasource.routing.aspectj;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.util.CollectionUtils;

import app.myoss.cloud.datasource.routing.config.DataSourceProperty;
import app.myoss.cloud.datasource.routing.config.MethodPointcutProperty;
import app.myoss.cloud.datasource.routing.spring.boot.autoconfigure.DataSourceRoutingProperties;
import lombok.Setter;

/**
 * 注册 {@link DataSourceProperty#methodPointcuts} 自定义的 AOP
 * 切面，在有Method之前执行设置数据源名称，Method执行之后清除掉数据源名称。
 * <p>
 * 特别注意：此 AOP 切面优先级要低于，放在 Method/class 上的
 * {@link app.myoss.cloud.datasource.routing.annotation.DataSource}
 *
 * @author Jerry.Chen
 * @since 2019年5月9日 下午4:08:30
 */
public class DataSourceMethodPointcutAdvisorDynamicBeanRegistry implements BeanDefinitionRegistryPostProcessor {
    /**
     * 动态数据源路由，配置属性
     */
    @Setter
    private DataSourceRoutingProperties dataSourceRoutingProperties;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (dataSourceRoutingProperties == null
                || CollectionUtils.isEmpty(dataSourceRoutingProperties.getDatabases())) {
            return;
        }
        LinkedHashMap<String, List<MethodPointcutProperty>> allMethodPointcuts = dataSourceRoutingProperties
                .getDatabases()
                .stream()
                .filter(database -> !CollectionUtils.isEmpty(database.getMethodPointcuts()))
                .collect(Collectors.toMap(DataSourceProperty::getName, DataSourceProperty::getMethodPointcuts,
                        (k, v) -> {
                            throw new IllegalStateException(String.format("Duplicate key %s", k));
                        }, LinkedHashMap::new));
        if (CollectionUtils.isEmpty(allMethodPointcuts)) {
            return;
        }

        int pointCutOrder = dataSourceRoutingProperties.getDataSourcePointcutAdvisorOrder() - 1;
        List<DataSourcePointcutAdvisor> pointcutAdvisors = DataSourceMethodPointcutAdvisorBuilder
                .create(allMethodPointcuts, pointCutOrder);
        for (int i = 0; i < pointcutAdvisors.size(); i++) {
            DataSourcePointcutAdvisor pointcutAdvisor = pointcutAdvisors.get(i);
            String dataSourceBeanName = pointcutAdvisor.getName() + DataSourcePointcutAdvisor.class.getSimpleName() + i;
            BeanDefinition beanDefinition = BeanDefinitionBuilder
                    .genericBeanDefinition(DataSourcePointcutAdvisor.class, () -> pointcutAdvisor)
                    .getRawBeanDefinition();
            registry.registerBeanDefinition(dataSourceBeanName, beanDefinition);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
