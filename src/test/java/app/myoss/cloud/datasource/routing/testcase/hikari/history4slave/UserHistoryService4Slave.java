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

package app.myoss.cloud.datasource.routing.testcase.hikari.history4slave;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import app.myoss.cloud.datasource.routing.annotation.DataSource;
import app.myoss.cloud.datasource.routing.testcase.hikari.history.entity.UserHistory;
import app.myoss.cloud.datasource.routing.testcase.hikari.history.service.impl.UserHistoryServiceImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link UserHistoryServiceImpl} 操作 "从数据库" 的数据
 *
 * @author Jerry.Chen
 * @since 2019年5月10日 上午12:06:31
 */
@Slf4j
@DataSource(name = "slave")
@Service
public class UserHistoryService4Slave {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<UserHistory> findList() {
        String sql = "SELECT id, employee_number, name FROM t_sys_user_history";
        log.info(sql);
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            UserHistory user = new UserHistory();
            user.setId(rs.getLong("id"));
            user.setName(rs.getString("name"));
            user.setEmployeeNumber(rs.getString("employee_number"));
            return user;
        });
    }
}
