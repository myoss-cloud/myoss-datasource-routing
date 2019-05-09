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

package app.myoss.cloud.datasource.routing.testcase.jdbctemplate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.zaxxer.hikari.HikariDataSource;

import app.myoss.cloud.datasource.routing.aspectj.DataSourcePointcutAdvisor;
import app.myoss.cloud.datasource.routing.jdbc.GroupDataSource;
import app.myoss.cloud.datasource.routing.jdbc.MultiDataSource;
import app.myoss.cloud.datasource.routing.spring.boot.autoconfigure.DataSourceRoutingAutoConfiguration;
import app.myoss.cloud.datasource.routing.testcase.jdbctemplate.JdbcTemplateCase2Tests.MyConfig;
import app.myoss.cloud.datasource.routing.testcase.jdbctemplate.entity.User;
import app.myoss.cloud.datasource.routing.testcase.jdbctemplate.service.UserService;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link org.springframework.jdbc.core.JdbcTemplate}
 * 数据源路由测试，master/slave数据库不是同一个库
 *
 * @author Jerry.Chen
 * @since 2019年5月9日 下午10:37:22
 */
@Slf4j
@ActiveProfiles({ "h2-jdbc-template-case2", "JdbcTemplateCase2Tests" })
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { DataSourceRoutingAutoConfiguration.class, JdbcTemplateAutoConfiguration.class,
        MyConfig.class, AopAutoConfiguration.class })
public class JdbcTemplateCase2Tests {
    @Autowired
    private UserService        userService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private DataSource         dataSource;

    /**
     * 校验 {@link HikariDataSource} 数据源，设置的属性设置正确
     */
    @Test
    public void checkDataSource() {
        MultiDataSource multiDataSource = (MultiDataSource) dataSource;
        Map<String, DataSource> targetDataSources = multiDataSource.getTargetDataSources();
        for (Entry<String, DataSource> entry : targetDataSources.entrySet()) {
            DataSource value = entry.getValue();
            assertThat(value).isInstanceOfAny(HikariDataSource.class, GroupDataSource.class);
            if (value instanceof GroupDataSource) {
                GroupDataSource groupDataSource = (GroupDataSource) value;
                List<DataSource> dataSources = groupDataSource.getTargetDataSources();
                for (DataSource item : dataSources) {
                    checkDataSourceValue(item);
                }
            } else {
                checkDataSourceValue(value);
            }
        }
    }

    private void checkDataSourceValue(DataSource dataSource) {
        HikariDataSource target = (HikariDataSource) dataSource;
        assertThat(target).matches(hikari -> hikari.getConnectionTestQuery().equals("SELECT 'X' FROM DUAL"))
                .matches(hikari -> {
                    Properties properties = hikari.getDataSourceProperties();
                    boolean isTrue = properties.size() == 2 && (Boolean) properties.get("remarks")
                            && (Boolean) properties.get("useInformationSchema");
                    if (hikari.getPoolName().equals("master")) {
                        return isTrue && hikari.getMaximumPoolSize() == 20;
                    } else if (hikari.getPoolName().equals("slave")) {
                        return isTrue && hikari.getMaximumPoolSize() == 30;
                    } else {
                        throw new IllegalArgumentException("unexpected name: " + hikari.getPoolName());
                    }
                });
    }

    @Test
    public void checkDataSourceMethodPointcutAdvisor() {
        Map<String, DataSourcePointcutAdvisor> beans = applicationContext
                .getBeansOfType(DataSourcePointcutAdvisor.class);
        assertThat(beans).hasSize(2).containsKeys("dataSourcePointcutAdvisor", "slaveDataSourcePointcutAdvisor0");
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void createTest() {
        User record = new User();
        String name = "Jerry-" + RandomStringUtils.randomAlphabetic(10);
        record.setName(name);
        record.setEmployeeNumber("001" + RandomStringUtils.randomAlphabetic(3));
        Long id = userService.create(record);
        Assert.assertTrue(id > 0L);

        List<User> list = userService.findList(name);
        Assert.assertNotNull(list);
        Assert.assertEquals(0, list.size());

        User one = userService.findOne(name);
        Assert.assertNull(one);
    }

    @ComponentScan(basePackageClasses = JdbcTemplateCase2Tests.class)
    @Profile("JdbcTemplateCase2Tests")
    @Configuration
    public static class MyConfig {

    }
}
