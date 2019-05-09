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

package app.myoss.cloud.datasource.routing.aspectj;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import app.myoss.cloud.datasource.routing.config.DataSourceProperty;
import app.myoss.cloud.datasource.routing.context.DataSourceNameContextHolder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * {@link DataSourceProperty#methodPointcuts} MethodInterceptor
 *
 * @author Jerry.Chen
 * @since 2019年5月9日 下午9:31:09
 * @see DataSourcePointcutAdvisor
 * @see DataSourceNameContextHolder
 */
@Setter
@Getter
@AllArgsConstructor
public class DataSourceMethodPointcutInterceptor implements MethodInterceptor {
    /**
     * DataSource name: {@link DataSourceProperty#name}
     */
    private String dataSourceName;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            DataSourceNameContextHolder.push(dataSourceName);
            return invocation.proceed();
        } finally {
            DataSourceNameContextHolder.pop();
        }
    }
}
