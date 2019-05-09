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

package app.myoss.cloud.datasource.routing.support.mybatis.plugin;

import static app.myoss.cloud.datasource.routing.constants.DataSourceRoutingConstants.MASTER_DATA_SOURCE_NAME;
import static app.myoss.cloud.datasource.routing.constants.DataSourceRoutingConstants.SLAVE_DATA_SOURCE_NAME;

import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import app.myoss.cloud.datasource.routing.constants.DataSourceRoutingConstants;
import app.myoss.cloud.datasource.routing.context.DataSourceNameContextHolder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * MyBatis SQL 执行插件，用于自动选择 "主/从数据源"，查询数据库的操作使用 "从数据源"，写数据库的操作使用 "主数据源".
 *
 * @author Jerry.Chen
 * @since 2019年5月8日 下午11:39:14
 */
@Intercepts({ @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
        @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class }) })
@NoArgsConstructor
public class MasterSlaveDataSourcePlugin implements Interceptor {
    private Properties properties;
    @Setter
    private boolean    init;
    @NonNull
    private String     master = MASTER_DATA_SOURCE_NAME;
    @NonNull
    private String     slave  = SLAVE_DATA_SOURCE_NAME;

    /**
     * MyBatis SQL 执行插件，用于自动选择 "主/从数据源"，查询数据库的操作使用 "从数据源"，写数据库的操作使用 "主数据源".
     *
     * @param master "主数据源"名称
     * @param slave "从数据源"名称
     */
    public MasterSlaveDataSourcePlugin(String master, String slave) {
        this.master = master;
        this.slave = slave;
        this.init = true;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        try {
            if (!init && properties != null) {
                master = properties.getProperty(MASTER_DATA_SOURCE_NAME, MASTER_DATA_SOURCE_NAME);
                slave = properties.getProperty(SLAVE_DATA_SOURCE_NAME, SLAVE_DATA_SOURCE_NAME);
                init = true;
            }
            String dataSourceName = (SqlCommandType.SELECT == ms.getSqlCommandType() ? slave : master);
            DataSourceNameContextHolder.push(dataSourceName);
            return invocation.proceed();
        } finally {
            DataSourceNameContextHolder.clear();
        }
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    /**
     * "主数据源"，设置key： {@link DataSourceRoutingConstants#MASTER_DATA_SOURCE_NAME}
     * <br>
     * "从数据源"，设置key： {@link DataSourceRoutingConstants#SLAVE_DATA_SOURCE_NAME}
     *
     * @param properties 设置 "主/从数据源" 的名称
     */
    @Override
    public void setProperties(@NonNull Properties properties) {
        this.properties = properties;
    }
}
