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

package app.myoss.cloud.datasource.routing.config;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.context.annotation.Primary;

import app.myoss.cloud.datasource.routing.aspectj.DataSourceMethodPointcutInterceptor;
import app.myoss.cloud.datasource.routing.spring.boot.autoconfigure.DataSourceRoutingProperties;
import lombok.Data;

/**
 * Base class for configuration of a data source.
 *
 * @author Jerry.Chen
 * @since 2019年5月6日 下午10:56:26
 */
@Data
public class DataSourceProperty {
    /**
     * Name of the datasource.
     */
    private String                       name;

    /**
     * describe of the datasource.
     */
    private String                       describe;

    /**
     * Group name of the datasource.
     *
     * @see DataSourceRoutingProperties#groupDataSourceLoadBalancer
     */
    private String                       groupName;

    /**
     * {@link Primary}
     */
    private boolean                      primary;

    /**
     * Fully qualified name of the connection pool implementation to use. By
     * default, it is auto-detected from the classpath.
     *
     * @see com.zaxxer.hikari.HikariDataSource
     * @see com.alibaba.druid.pool.DruidDataSource
     * @see org.apache.tomcat.jdbc.pool.DataSource
     * @see org.apache.commons.dbcp2.BasicDataSource
     */
    private Class<? extends DataSource>  type;

    /**
     * Fully qualified name of the JDBC driver. Auto-detected based on the URL
     * by default.
     */
    private String                       driverClassName;

    /**
     * JDBC URL of the database.
     */
    private String                       url;

    /**
     * Login username of the database.
     */
    private String                       username;

    /**
     * Login password of the database.
     */
    private String                       password;

    /**
     * JNDI location of the datasource. Class, url, username & password are
     * ignored when set.
     */
    private String                       jndiName;

    /**
     * 设置 DataSource 特有的属性，请参考自己使用的哪种数据源
     *
     * @see com.zaxxer.hikari.HikariDataSource
     * @see com.alibaba.druid.pool.DruidDataSource
     * @see org.apache.tomcat.jdbc.pool.DataSource
     * @see org.apache.commons.dbcp2.BasicDataSource
     */
    private Map<String, Object>          properties;

    /**
     * The pointcut configuration of Method
     *
     * @see DataSourceMethodPointcutInterceptor
     */
    private List<MethodPointcutProperty> methodPointcuts;

}
