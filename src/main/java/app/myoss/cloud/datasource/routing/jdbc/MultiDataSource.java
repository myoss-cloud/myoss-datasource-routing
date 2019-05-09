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
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;

import app.myoss.cloud.datasource.routing.context.DataSourceNameContextHolder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Multi DataSource
 *
 * @author Jerry.Chen
 * @since 2019年5月6日 下午11:48:26
 */
@Slf4j
@Setter
@Getter
@RequiredArgsConstructor
public class MultiDataSource extends AbstractRoutingDataSource {
    /**
     * name of the datasource.
     */
    private String                  name;
    /**
     * primary DataSource Name
     */
    @NonNull
    private String                  primaryDataSourceName;
    /**
     * Specify the map of target DataSources
     */
    @NonNull
    private Map<String, DataSource> targetDataSources;

    /**
     * name of the datasource.
     *
     * @return name of the datasource.
     */
    public String getName() {
        if (name != null) {
            return name;
        }
        final String prefix = "MultiDataSource-";
        name = getDataSourceNumber(prefix);
        return name;
    }

    @Override
    public void close() throws IOException {
        logger.info("{} - Shutdown initiated...", getName());
        for (Entry<String, DataSource> entry : targetDataSources.entrySet()) {
            DataSource value = entry.getValue();
            if (value instanceof Closeable) {
                ((Closeable) value).close();
            }
        }
        targetDataSources.clear();
        logger.info("{} - Shutdown completed.", getName());
    }

    @Override
    protected DataSource determineTargetDataSource() {
        String dataSourceName = DataSourceNameContextHolder.peek();
        if (StringUtils.isBlank(dataSourceName)) {
            dataSourceName = this.primaryDataSourceName;
        }
        DataSource dataSource = targetDataSources.get(dataSourceName);
        if (dataSource == null) {
            throw new IllegalStateException("Cannot find DataSource [" + dataSourceName + "]");
        }
        return dataSource;
    }
}
