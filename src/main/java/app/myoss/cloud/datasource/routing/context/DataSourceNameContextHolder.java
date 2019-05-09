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

package app.myoss.cloud.datasource.routing.context;

import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 管理线程堆栈的多数据源名称上下文，只操作当前线程的。
 * {@code https://spring.io/blog/2007/01/23/dynamic-datasource-routing/}
 *
 * @author Jerry.Chen
 * @since 2019年5月1日 下午11:58:09
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataSourceNameContextHolder {
    /**
     * 记录了当前线程，方法调用堆栈的不同数据源名称
     */
    private static final ThreadLocal<Deque<String>> DATA_SOURCE_NAME_CONTEXT = ThreadLocal.withInitial(ArrayDeque::new);

    /**
     * 将数据源名称，放入堆栈顶部
     *
     * @param dataSourceName 数据源名称
     */
    public static void push(String dataSourceName) {
        Deque<String> deque = DATA_SOURCE_NAME_CONTEXT.get();
        String name = (StringUtils.isBlank(dataSourceName) ? StringUtils.EMPTY : dataSourceName);
        deque.push(name);
    }

    /**
     * 获取堆栈顶部的数据源名称，但不从堆栈中移除它。
     *
     * @return 数据源名称
     */
    public static String peek() {
        Deque<String> deque = DATA_SOURCE_NAME_CONTEXT.get();
        return deque.peek();
    }

    /**
     * 移除堆栈顶部的数据源名称，并作为此函数的值返回该对象
     *
     * @return 堆栈顶部的数据源名称
     */
    public static String pop() {
        Deque<String> deque = DATA_SOURCE_NAME_CONTEXT.get();
        String dataSourceName = deque.poll();
        if (deque.isEmpty()) {
            DATA_SOURCE_NAME_CONTEXT.remove();
        }
        return dataSourceName;
    }

    /**
     * 清空堆栈
     */
    public static void clear() {
        DATA_SOURCE_NAME_CONTEXT.remove();
    }
}
