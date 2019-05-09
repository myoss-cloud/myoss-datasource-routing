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

package app.myoss.cloud.datasource.routing.testcase.hikari.history.mapper;

import org.springframework.stereotype.Repository;

import app.myoss.cloud.datasource.routing.testcase.hikari.history.entity.UserHistory;
import app.myoss.cloud.mybatis.mapper.template.CrudMapper;

/**
 * This mapper interface access the database table t_sys_user_history
 * <p>
 * Database Table Remarks: 系统用户信息历史备份表
 * </p>
 *
 * @author jerry
 * @since 2018年5月14日 下午3:39:43
 */
@Repository
public interface UserHistoryMapper extends CrudMapper<UserHistory> {

}
