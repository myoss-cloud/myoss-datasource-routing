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

import java.util.Map;

import app.myoss.cloud.datasource.routing.jdbc.loadbalancer.DataSourceLoadBalancer;
import app.myoss.cloud.datasource.routing.jdbc.loadbalancer.impl.RoundRobinDataSourceLoadBalanced;
import lombok.Data;

/**
 * 分组数据源的配置属性
 *
 * @author Jerry.Chen
 * @since 2019年5月10日 下午5:05:40
 */
@Data
public class GroupDataSourceProperty {
    /**
     * 分组数据源的负载均衡器。默认值：{@link app.myoss.cloud.datasource.routing.jdbc.loadbalancer.impl.RoundRobinDataSourceLoadBalanced}
     *
     * @see app.myoss.cloud.datasource.routing.config.DataSourceProperty#groupName
     */
    private Class<? extends DataSourceLoadBalancer> loadBalancer = RoundRobinDataSourceLoadBalanced.class;

    /**
     * 分组数据源的负载均衡器，初始化配置
     *
     * @see app.myoss.cloud.datasource.routing.jdbc.loadbalancer.DataSourceLoadBalancer#init(Map)
     */
    private Map<String, Object>                     initConfig;
}
