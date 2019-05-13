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

package app.myoss.cloud.datasource.routing.spring.boot.autoconfigure;

import java.util.Set;

import javax.sql.DataSource;

import org.springframework.aop.Pointcut;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import app.myoss.cloud.datasource.routing.aspectj.DataSourceAnnotationInterceptor;
import app.myoss.cloud.datasource.routing.aspectj.DataSourceMethodPointcutAdvisorDynamicBeanRegistry;
import app.myoss.cloud.datasource.routing.aspectj.DataSourcePointcutAdvisor;
import app.myoss.cloud.datasource.routing.config.DataSourceProperty;
import app.myoss.cloud.datasource.routing.constants.DataSourceRoutingConstants;
import app.myoss.cloud.datasource.routing.spring.boot.jdbc.DataSourceBuilder;
import app.myoss.cloud.datasource.routing.spring.boot.jdbc.impl.MultiDataSourceBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * 动态数据源路由 Spring Boot 项目自动配置
 *
 * @author Jerry.Chen
 * @since 2019年5月1日 下午11:20:14
 */
@Slf4j
@ConditionalOnProperty(prefix = DataSourceRoutingConstants.CONFIG_PREFIX, value = "enabled", matchIfMissing = true)
@EnableConfigurationProperties({ DataSourceRoutingProperties.class })
@AutoConfigureOrder(DataSourceRoutingConstants.CONFIG_ORDER)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@AutoConfigureAfter(RefreshAutoConfiguration.class)
@Configuration
public class DataSourceRoutingAutoConfiguration {
    /**
     * 初始化
     */
    public DataSourceRoutingAutoConfiguration() {
    }

    /**
     * 创建数据源构造器
     *
     * @param dataSourceRoutingProperties 动态数据源路由，配置属性
     * @return 数据源构造器
     */
    @Bean
    @ConditionalOnMissingBean
    public DataSourceBuilder dataSourceBuilder(DataSourceRoutingProperties dataSourceRoutingProperties) {
        return new MultiDataSourceBuilder(dataSourceRoutingProperties);
    }

    /**
     * 注册 {@link DataSource} 注解的 AOP
     * 切面，在有注解的Method之前执行设置数据源名称，Method执行之后清除掉数据源名称
     *
     * @param dataSourceRoutingProperties 动态数据源路由，配置属性
     * @return {@link DataSource} 注解的 AOP 切面
     */
    @Bean
    @ConditionalOnMissingBean
    public DataSourcePointcutAdvisor dataSourcePointcutAdvisor(DataSourceRoutingProperties dataSourceRoutingProperties) {
        DataSourceAnnotationInterceptor interceptor = new DataSourceAnnotationInterceptor();
        Pointcut classPointCut = new AnnotationMatchingPointcut(
                app.myoss.cloud.datasource.routing.annotation.DataSource.class, true);
        Pointcut methodPointcut = AnnotationMatchingPointcut
                .forMethodAnnotation(app.myoss.cloud.datasource.routing.annotation.DataSource.class);
        ComposablePointcut pointcut = new ComposablePointcut(classPointCut).union(methodPointcut);
        DataSourcePointcutAdvisor pointcutAdvisor = new DataSourcePointcutAdvisor(pointcut, interceptor);
        pointcutAdvisor.setOrder(dataSourceRoutingProperties.getDataSourcePointcutAdvisorOrder());
        return pointcutAdvisor;
    }

    /**
     * 注册 {@link DataSourceProperty#methodPointcuts} 自定义的 AOP
     * 切面，在有Method之前执行设置数据源名称，Method执行之后清除掉数据源名称。
     * <p>
     * 特别注意：此 AOP 切面优先级要低于
     * {@link #dataSourcePointcutAdvisor(DataSourceRoutingProperties)}，放在
     * Method/class 上的
     * {@link app.myoss.cloud.datasource.routing.annotation.DataSource} 优先级最高
     *
     * @param environment 环境变量属性
     * @return 自定义的 AOP 切面注册器
     */
    @Bean
    @ConditionalOnMissingBean
    public DataSourceMethodPointcutAdvisorDynamicBeanRegistry dataSourceMethodPointcutAdvisorDynamicBeanRegistry(Environment environment) {
        DataSourceRoutingProperties dataSourceRoutingProperties = new DataSourceRoutingProperties();
        Binder binder = Binder.get(environment);
        binder.bind(DataSourceRoutingConstants.CONFIG_PREFIX, Bindable.ofInstance(dataSourceRoutingProperties));
        DataSourceMethodPointcutAdvisorDynamicBeanRegistry registry = new DataSourceMethodPointcutAdvisorDynamicBeanRegistry();
        registry.setDataSourceRoutingProperties(dataSourceRoutingProperties);
        return registry;
    }

    /**
     * 非 Spring Cloud 项目动态数据源路由自动配置
     */
    @ConditionalOnMissingBean(type = "org.springframework.cloud.context.scope.refresh.RefreshScope")
    @Configuration
    public static class NotSpringCloudProjectAutoConfiguration {
        /**
         * 创建 {@link DataSource}
         *
         * @param dataSourceBuilder 数据源构造器
         * @return 新的 {@link DataSource}
         */
        @Primary
        @Bean
        @ConditionalOnMissingBean(DataSource.class)
        public DataSource dataSource(DataSourceBuilder dataSourceBuilder) {
            return dataSourceBuilder.build();
        }
    }

    /**
     * Spring Cloud 项目动态数据源路由自动配置，会注册 {@link RefreshScope}
     * 事件，当环境变量发生变更的时候，重新创建数据源
     */
    @ConditionalOnBean(org.springframework.cloud.context.scope.refresh.RefreshScope.class)
    @Configuration
    public static class SpringCloudProjectAutoConfiguration {
        @Autowired
        private DataSourceBuilder dataSourceBuilder;

        /**
         * 创建 {@link DataSource}，会注册 {@link RefreshScope}
         * 事件，当环境变量发生变更的时候，重新创建数据源
         *
         * @param dataSourceBuilder 数据源构造器
         * @return 新的 {@link DataSource}
         */
        @RefreshScope
        @Primary
        @Bean
        @ConditionalOnMissingBean(DataSource.class)
        public DataSource dataSource(DataSourceBuilder dataSourceBuilder) {
            return dataSourceBuilder.build();
        }

        /**
         * 监听 {@link EnvironmentChangeEvent}，通知数据源构造器，数据库配置发生了变更
         *
         * @param event {@link EnvironmentChangeEvent}
         */
        @EventListener
        public void onEnvironmentChangeEvent(EnvironmentChangeEvent event) {
            Set<String> keys = event.getKeys();
            if (CollectionUtils.isEmpty(keys)) {
                return;
            }
            StringBuilder changed = new StringBuilder();
            for (String key : keys) {
                if (key.startsWith(DataSourceRoutingConstants.CONFIG_PREFIX)) {
                    changed.append(key).append(", ");
                }
            }
            int length = changed.length();
            if (length > 0) {
                log.info("Refresh keys changed: {}", changed.delete(length - 3, length));
                dataSourceBuilder.onEnvironmentChangeEvent(event);
            }
        }
    }
}
