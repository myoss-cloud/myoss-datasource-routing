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

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotationUtils;

import app.myoss.cloud.datasource.routing.annotation.DataSource;
import app.myoss.cloud.datasource.routing.context.DataSourceNameContextHolder;

/**
 * {@link DataSource} MethodInterceptor
 *
 * @author Jerry.Chen
 * @since 2019年5月7日 下午11:19:26
 * @see DataSourcePointcutAdvisor
 * @see DataSourceNameContextHolder
 */
public class DataSourceAnnotationInterceptor implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Method method = methodInvocation.getMethod();
        DataSource dataSource = AnnotationUtils.findAnnotation(method, DataSource.class);
        if (dataSource == null) {
            dataSource = AnnotationUtils.findAnnotation(method.getDeclaringClass(), DataSource.class);
        }
        String name = dataSource.name();
        try {
            DataSourceNameContextHolder.push(name);
            return methodInvocation.proceed();
        } finally {
            DataSourceNameContextHolder.pop();
        }
    }
}
