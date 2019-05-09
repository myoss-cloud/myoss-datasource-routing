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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.aop.Pointcut;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.JdkRegexpMethodPointcut;

import app.myoss.cloud.datasource.routing.config.DataSourceProperty;
import app.myoss.cloud.datasource.routing.config.MethodPointcutProperty;
import app.myoss.cloud.datasource.routing.constants.MethodPointcutEnum;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * {@link DataSourceProperty#methodPointcuts} PointcutAdvisor
 *
 * @author Jerry.Chen
 * @since 2019年5月8日 下午10:26:44
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceMethodPointcutAdvisorBuilder {
    /**
     * create {@link DataSourceProperty#methodPointcuts} PointcutAdvisors
     *
     * @param allMethodPointcuts all Method pointcut
     * @param pointCutOrder pointcut order
     * @return {@link DataSourceProperty#methodPointcuts} PointcutAdvisors
     */
    public static List<DataSourcePointcutAdvisor> create(LinkedHashMap<String, List<MethodPointcutProperty>> allMethodPointcuts,
                                                         int pointCutOrder) {
        Map<String, String> duplicateExpressionCheck = new HashMap<>(16);
        Map<String, String> matchMethod = new HashMap<>(24);
        return allMethodPointcuts.entrySet().stream().map(entry -> {
            String dataSourceName = entry.getKey();
            return entry.getValue().stream().map(item -> {
                ComposablePointcut composablePointcut = null;
                MethodPointcutEnum type = item.getType();
                Set<String> expressions = item.getExpressions();
                for (String expression : expressions) {
                    String previous = duplicateExpressionCheck.putIfAbsent(expression.trim(), dataSourceName);
                    if (previous != null) {
                        // AOP 切面表达式重复校验
                        throw new IllegalArgumentException(
                                "Method pointcut expression [" + expression + "] is already use DataSource '" + previous
                                        + "', cannot set another DataSource '" + dataSourceName + "'");
                    }

                    Pointcut pointcut = buildPointcut(dataSourceName, matchMethod, type, expression);
                    if (composablePointcut == null) {
                        composablePointcut = new ComposablePointcut(pointcut);
                    } else {
                        composablePointcut.union(pointcut);
                    }
                }
                DataSourcePointcutAdvisor pointcutAdvisor = new DataSourcePointcutAdvisor(dataSourceName,
                        composablePointcut, new DataSourceMethodPointcutInterceptor(dataSourceName));
                pointcutAdvisor.setOrder(pointCutOrder);
                return pointcutAdvisor;
            }).collect(Collectors.toList());
        }).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private static Pointcut buildPointcut(String dataSourceName, Map<String, String> matchMethod,
                                          MethodPointcutEnum type, String expression) {
        if (type == MethodPointcutEnum.AspectJExpression) {
            AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut() {
                private static final long serialVersionUID = 308965026639128900L;

                @Override
                public boolean matches(Method method, Class<?> targetClass, boolean hasIntroductions) {
                    if (super.matches(method, targetClass, hasIntroductions)) {
                        String methodQualified = targetClass.getName() + "." + method.getName();
                        String previous = matchMethod.putIfAbsent(methodQualified, dataSourceName);
                        if (previous != null && !previous.equals(dataSourceName)) {
                            // AOP 切面表达式重复校验，目前重复不会发生在这里，双重保险
                            throw new IllegalArgumentException(
                                    "Method [" + methodQualified + "] is already use DataSource '" + previous
                                            + "', cannot set another DataSource '" + dataSourceName + "'");
                        }
                        return true;
                    }
                    return false;
                }
            };
            pointcut.setExpression(expression);
            return pointcut;
        } else if (type == MethodPointcutEnum.JdkRegexpMethod) {
            JdkRegexpMethodPointcut pointcut = new JdkRegexpMethodPointcut() {
                private static final long serialVersionUID = -2853478842657883590L;

                @Override
                protected boolean matches(String pattern, int patternIndex) {
                    if (super.matches(pattern, patternIndex)) {
                        String previous = matchMethod.putIfAbsent(pattern, dataSourceName);
                        if (previous != null && !previous.equals(dataSourceName)) {
                            // AOP 切面表达式重复校验，目前重复不会发生在这里，双重保险
                            throw new IllegalArgumentException("Method [" + pattern + "] is already use DataSource '"
                                    + previous + "', cannot set another DataSource '" + dataSourceName + "'");
                        }
                        return true;
                    }
                    return false;
                }
            };
            pointcut.setPatterns(expression);
            return pointcut;
        } else {
            throw new UnsupportedOperationException("MethodPointcutEnum: " + type);
        }
    }

}
