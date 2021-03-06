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

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;

import app.myoss.cloud.datasource.routing.annotation.DataSource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * {@link DataSource} PointcutAdvisor
 *
 * @author Jerry.Chen
 * @since 2019年5月7日 下午11:50:33
 * @see DataSourceAnnotationInterceptor
 */
@Setter
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class DataSourcePointcutAdvisor extends AbstractPointcutAdvisor {
    private static final long serialVersionUID = 2295136997349757127L;
    private String            name;
    @NonNull
    private Pointcut          pointcut;
    @NonNull
    private Advice            advice;
}
