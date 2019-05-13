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

package app.myoss.cloud.datasource.routing.constants;

import app.myoss.cloud.core.constants.MyossConstants;

/**
 * 动态数据源路由常量
 *
 * @author Jerry.Chen
 * @since 2019年5月5日 下午11:14:52
 */
public class DataSourceRoutingConstants {
    /**
     * 动态数据源路由配置前缀
     */
    public static final String CONFIG_PREFIX           = MyossConstants.CONFIG_PREFIX + ".datasource-routing";
    /**
     * Cloud configuration needs to happen early (before database, mybatis etc.)
     */
    public static final int    CONFIG_ORDER            = -1000;

    /**
     * 主数据库名称
     */
    public static final String MASTER_DATA_SOURCE_NAME = "master";
    /**
     * 从数据库名称
     */
    public static final String SLAVE_DATA_SOURCE_NAME  = "slave";
}
