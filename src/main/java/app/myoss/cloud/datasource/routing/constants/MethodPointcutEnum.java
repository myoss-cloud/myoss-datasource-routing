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

/**
 * The pointcut type for Method
 *
 * @author Jerry.Chen
 * @since 2019年5月8日 下午11:39:41
 */
public enum MethodPointcutEnum {
    /**
     * AspectJ weaver to evaluate a pointcut expression.
     *
     * @see org.springframework.aop.aspectj.AspectJExpressionPointcut
     */
    AspectJExpression,
    /**
     * Regular expression pointcut based on the {@code java.util.regex} package.
     *
     * @see org.springframework.aop.support.JdkRegexpMethodPointcut
     */
    JdkRegexpMethod
}
