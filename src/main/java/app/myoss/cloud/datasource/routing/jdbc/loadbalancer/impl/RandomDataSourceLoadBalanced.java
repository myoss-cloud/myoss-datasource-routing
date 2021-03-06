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
import java.util.concurrent.ThreadLocalRandom;

import javax.sql.DataSource;

import app.myoss.cloud.datasource.routing.jdbc.loadbalancer.DataSourceLoadBalancer;

/**
 * 随机负载均衡算法
 *
 * @author Jerry.Chen
 * @since 2019年5月7日 下午10:04:26
 */
public class RandomDataSourceLoadBalanced implements DataSourceLoadBalancer {
    @Override
    public DataSource determineDataSource(List<DataSource> targetDataSources) {
        int index = ThreadLocalRandom.current().nextInt(targetDataSources.size());
        return targetDataSources.get(index);
    }
}
