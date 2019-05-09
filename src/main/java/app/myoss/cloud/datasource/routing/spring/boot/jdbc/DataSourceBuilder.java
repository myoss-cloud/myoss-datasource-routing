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

package app.myoss.cloud.datasource.routing.spring.boot.jdbc;

import javax.sql.DataSource;

import org.springframework.core.env.Environment;

/**
 * Convenience class for building a {@link DataSource}
 *
 * @author Jerry.Chen
 * @since 2019年5月7日 下午10:48:37
 */
public interface DataSourceBuilder {
    /**
     * building a {@link DataSource}
     *
     * @return {@link DataSource}
     */
    DataSource build();

    /**
     * building a {@link DataSource}
     *
     * @param params parameters
     * @return {@link DataSource}
     */
    default DataSource build(Object params) {
        return build();
    }

    /**
     * change in the {@link Environment} event.
     *
     * @param event event param
     */
    default void onEnvironmentChangeEvent(Object event) {
        // do nothing
    }
}
