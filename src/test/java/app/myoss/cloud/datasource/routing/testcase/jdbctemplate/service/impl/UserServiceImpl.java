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

package app.myoss.cloud.datasource.routing.testcase.jdbctemplate.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import app.myoss.cloud.core.constants.MyossConstants;
import app.myoss.cloud.core.exception.BizRuntimeException;
import app.myoss.cloud.datasource.routing.annotation.DataSource;
import app.myoss.cloud.datasource.routing.constants.DataSourceRoutingConstants;
import app.myoss.cloud.datasource.routing.testcase.jdbctemplate.entity.User;
import app.myoss.cloud.datasource.routing.testcase.jdbctemplate.service.UserService;

/**
 * 用户服务实现类
 *
 * @author Jerry.Chen
 * @since 2019年5月9日 下午10:49:15
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Long create(User record) {
        String sql = "INSERT INTO t_sys_user ( employee_number, `name`, is_deleted, creator, modifier, gmt_created, gmt_modified ) values ( ?, ?, ?, ?, ?, ?, ? )";
        int update = jdbcTemplate.update(sql, record.getEmployeeNumber(), record.getName(), MyossConstants.N,
                MyossConstants.SYSTEM, MyossConstants.SYSTEM, new Date(), new Date());
        if (update < 1) {
            throw new BizRuntimeException("创建记录失败");
        }
        Long value = jdbcTemplate.queryForObject("select max(id) from t_sys_user", Long.class);
        return (value != null ? value : 0L);
    }

    @DataSource(name = DataSourceRoutingConstants.MASTER_DATA_SOURCE_NAME)
    @Override
    public Boolean deleteByPrimaryKey(Long id) {
        String sql = "DELETE FROM t_sys_user WHERE id = ?";
        int update = jdbcTemplate.update(sql, id);
        if (update < 1) {
            throw new BizRuntimeException("删除记录失败");
        }
        return true;
    }

    @Override
    public User findOne(String name) {
        String sql = "SELECT id, employee_number, name FROM t_sys_user WHERE name = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setName(rs.getString("name"));
            user.setEmployeeNumber(rs.getString("employee_number"));
            return user;
        }, name);
    }

    @Override
    public List<User> findList(String name) {
        String sql = "SELECT id, employee_number, name FROM t_sys_user WHERE name = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setName(rs.getString("name"));
            user.setEmployeeNumber(rs.getString("employee_number"));
            return user;
        }, name);
    }
}
