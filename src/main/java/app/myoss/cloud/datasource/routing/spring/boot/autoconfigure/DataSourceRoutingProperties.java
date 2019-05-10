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

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;

import app.myoss.cloud.datasource.routing.annotation.DataSource;
import app.myoss.cloud.datasource.routing.aspectj.DataSourceAnnotationInterceptor;
import app.myoss.cloud.datasource.routing.aspectj.DataSourceMethodPointcutInterceptor;
import app.myoss.cloud.datasource.routing.config.DataSourceProperty;
import app.myoss.cloud.datasource.routing.constants.DataSourceRoutingConstants;
import app.myoss.cloud.datasource.routing.jdbc.loadbalancer.DataSourceLoadBalancer;
import app.myoss.cloud.datasource.routing.jdbc.loadbalancer.impl.RoundRobinDataSourceLoadBalanced;
import lombok.Data;

/**
 * 动态数据源路由，配置属性
 *
 * @author Jerry.Chen
 * @since 2019年5月5日 下午11:05:04
 */
@Data
@ConfigurationProperties(prefix = DataSourceRoutingConstants.CONFIG_PREFIX)
public class DataSourceRoutingProperties {
    /**
     * 是否启用 "动态数据源路由" 自动配置
     */
    private Boolean                                 enabled;

    /**
     * 分组数据源的负载均衡器
     *
     * @see DataSourceProperty#groupName
     */
    private Class<? extends DataSourceLoadBalancer> groupDataSourceLoadBalancer    = RoundRobinDataSourceLoadBalanced.class;

    /**
     * {@link DataSource} AOP切面顺序，默认优先级最高。
     * {@link DataSourceProperty#methodPointcuts} 自定义的AOP切面顺序 -1，优先级比
     * {@link DataSource} AOP切面顺序 低。
     *
     * @see DataSourceAnnotationInterceptor
     * @see DataSourceMethodPointcutInterceptor
     */
    private Integer                                 dataSourcePointcutAdvisorOrder = Ordered.HIGHEST_PRECEDENCE + 10;

    /**
     * {@link DataSourceProperty#properties} 全局配置，设置 DataSource
     * 特有的属性，请参考自己使用的哪种数据源。
     *
     * @see com.zaxxer.hikari.HikariDataSource
     * @see com.alibaba.druid.pool.DruidDataSource
     * @see org.apache.tomcat.jdbc.pool.DataSource
     * @see org.apache.commons.dbcp2.BasicDataSource
     */
    private Map<String, Object>                     globalDatabaseProperties;

    /**
     * 数据源具体的属性配置
     */
    private List<DataSourceProperty>                databases;
}
