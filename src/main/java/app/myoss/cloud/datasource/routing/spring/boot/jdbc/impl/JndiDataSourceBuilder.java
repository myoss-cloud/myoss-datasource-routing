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

import javax.sql.DataSource;

import app.myoss.cloud.datasource.routing.spring.boot.jdbc.DataSourceBuilder;

/**
 * JNDI-based {@link DataSourceBuilder} implementation
 *
 * @author Jerry.Chen
 * @since 2019年5月8日 下午10:02:44
 */
public class JndiDataSourceBuilder implements DataSourceBuilder {
    private Object jndiDataSourceLookup;

    /**
     * JNDI-based {@link DataSourceBuilder} implementation
     */
    public JndiDataSourceBuilder() {
        this.jndiDataSourceLookup = new org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup();
    }

    @Override
    public DataSource build() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataSource build(Object params) {
        String dataSourceName = (String) params;
        return ((org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup) jndiDataSourceLookup)
                .getDataSource(dataSourceName);
    }
}
