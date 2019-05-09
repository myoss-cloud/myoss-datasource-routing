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

package app.myoss.cloud.datasource.routing.testcase.jdbctemplate.service;

import java.util.List;

import app.myoss.cloud.datasource.routing.testcase.jdbctemplate.entity.User;

/**
 * 用户服务接口
 *
 * @author Jerry.Chen
 * @since 2019年5月9日 下午10:49:03
 */
public interface UserService {
    /**
     * 创建新的记录
     *
     * @param record 待保存的实体对象
     * @return 创建结果
     */
    Long create(User record);

    /**
     * 根据主键删除记录
     *
     * @param id 匹配的条件，主键有值的实体对象
     * @return 影响的行数
     */
    Boolean deleteByPrimaryKey(Long id);

    /**
     * 根据条件查询匹配的实体对象
     *
     * @param name 匹配的条件
     * @return 匹配的实体对象
     */
    User findOne(String name);

    /**
     * 根据条件查询匹配的实体对象
     *
     * @param name 匹配的条件
     * @return 匹配的实体对象
     */
    List<User> findList(String name);
}
