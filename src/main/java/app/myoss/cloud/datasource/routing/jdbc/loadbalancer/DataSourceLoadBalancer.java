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

package app.myoss.cloud.datasource.routing.jdbc.loadbalancer;

import java.util.List;

import javax.sql.DataSource;

/**
 * 多数据源负载均衡器
 *
 * @author Jerry.Chen
 * @since 2019年5月6日 下午10:30:32
 */
public interface DataSourceLoadBalancer {
    /**
     * Retrieve the current target DataSource.
     *
     * @param targetDataSources Specify the list of target DataSources
     * @return determine target DataSource
     */
    DataSource determineDataSource(List<DataSource> targetDataSources);
}
