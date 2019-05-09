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

package app.myoss.cloud.datasource.routing.jdbc;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import javax.sql.DataSource;

import app.myoss.cloud.datasource.routing.jdbc.loadbalancer.DataSourceLoadBalancer;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Group DataSource
 *
 * @author Jerry.Chen
 * @since 2019年5月6日 下午10:21:42
 */
@Setter
@Getter
@RequiredArgsConstructor
public class GroupDataSource extends AbstractRoutingDataSource {
    /**
     * Group name of the datasource.
     */
    @NonNull
    private String                 groupName;
    /**
     * Determine the current DataSource load balancer
     */
    @NonNull
    private DataSourceLoadBalancer dataSourceLoadBalancer;
    /**
     * Specify the list of target DataSources
     */
    @NonNull
    private List<DataSource>       targetDataSources;

    @Override
    public void close() throws IOException {
        logger.info("GroupDataSource: {} - Shutdown initiated...", groupName);
        for (DataSource value : targetDataSources) {
            if (value instanceof Closeable) {
                ((Closeable) value).close();
            }
        }
        targetDataSources.clear();
        logger.info("GroupDataSource: {} - Shutdown completed.", groupName);
    }

    @Override
    protected DataSource determineTargetDataSource() {
        DataSource dataSource = dataSourceLoadBalancer.determineDataSource(targetDataSources);
        if (dataSource == null) {
            throw new IllegalStateException("Cannot determine target DataSource");
        }
        return dataSource;
    }
}
