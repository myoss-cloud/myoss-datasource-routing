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

package app.myoss.cloud.datasource.routing.jdbc.loadbalancer.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import app.myoss.cloud.datasource.routing.jdbc.loadbalancer.DataSourceLoadBalancer;

/**
 * 轮询负载均衡算法
 *
 * @author Jerry.Chen
 * @since 2019年5月7日 下午9:52:59
 */
public class RoundRobinDataSourceLoadBalanced implements DataSourceLoadBalancer {
    private AtomicInteger counter = new AtomicInteger(0);

    @Override
    public DataSource determineDataSource(List<DataSource> targetDataSources) {
        int currentValue = counter.getAndAdd(1);
        int index = Math.abs(currentValue % targetDataSources.size());
        return targetDataSources.get(index);
    }
}
