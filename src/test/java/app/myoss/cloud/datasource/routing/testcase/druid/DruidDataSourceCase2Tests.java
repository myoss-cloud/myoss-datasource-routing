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

package app.myoss.cloud.datasource.routing.testcase.druid;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.druid.pool.DruidDataSource;

import app.myoss.cloud.core.lang.dto.Result;
import app.myoss.cloud.core.lang.json.JsonApi;
import app.myoss.cloud.datasource.routing.aspectj.DataSourcePointcutAdvisor;
import app.myoss.cloud.datasource.routing.config.GroupDataSourceProperty;
import app.myoss.cloud.datasource.routing.jdbc.GroupDataSource;
import app.myoss.cloud.datasource.routing.jdbc.MultiDataSource;
import app.myoss.cloud.datasource.routing.jdbc.loadbalancer.impl.RandomDataSourceLoadBalanced;
import app.myoss.cloud.datasource.routing.jdbc.loadbalancer.impl.RoundRobinDataSourceLoadBalanced;
import app.myoss.cloud.datasource.routing.spring.boot.autoconfigure.DataSourceRoutingAutoConfiguration;
import app.myoss.cloud.datasource.routing.spring.boot.autoconfigure.DataSourceRoutingProperties;
import app.myoss.cloud.datasource.routing.testcase.druid.DruidDataSourceCase2Tests.MyConfig;
import app.myoss.cloud.datasource.routing.testcase.druid.entity.User;
import app.myoss.cloud.datasource.routing.testcase.druid.service.UserService;
import app.myoss.cloud.mybatis.constants.MybatisConstants;
import app.myoss.cloud.mybatis.plugin.ParameterHandlerCustomizer;
import app.myoss.cloud.mybatis.plugin.impl.DefaultParameterHandlerCustomizer;
import app.myoss.cloud.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link com.alibaba.druid.pool.DruidDataSource} 数据源路由测试，master/slave数据库不是同一个库
 *
 * @author Jerry.Chen
 * @since 2019年5月9日 下午11:43:27
 */
@Slf4j
@ActiveProfiles({ "h2-druid-case2", "DruidDataSourceCase2Tests" })
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { DataSourceRoutingAutoConfiguration.class, MyConfig.class, AopAutoConfiguration.class,
        MybatisAutoConfiguration.class })
public class DruidDataSourceCase2Tests {
    @Autowired
    private UserService        userService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private DataSource         dataSource;

    /**
     * 校验 {@link DruidDataSource} 数据源，设置的属性设置正确
     */
    @Test
    public void checkDataSource() {
        MultiDataSource multiDataSource = (MultiDataSource) dataSource;
        Map<String, DataSource> targetDataSources = multiDataSource.getTargetDataSources();
        for (Entry<String, DataSource> entry : targetDataSources.entrySet()) {
            DataSource value = entry.getValue();
            assertThat(value).isInstanceOfAny(DruidDataSource.class, GroupDataSource.class);
            if (value instanceof GroupDataSource) {
                GroupDataSource groupDataSource = (GroupDataSource) value;
                List<DataSource> dataSources = groupDataSource.getTargetDataSources();
                for (DataSource item : dataSources) {
                    checkDataSourceValue(item);
                }
                assertThat(groupDataSource).matches(source -> source.getDataSourceLoadBalancer()
                        .getClass() == RoundRobinDataSourceLoadBalanced.class);
            } else {
                checkDataSourceValue(value);
            }
        }
    }

    private void checkDataSourceValue(DataSource dataSource) {
        DruidDataSource target = (DruidDataSource) dataSource;
        assertThat(target).matches(druid -> druid.getValidationQuery().equals("SELECT 'X' FROM DUAL"))
                .matches(druid -> {
                    if (druid.getName().equals("master")) {
                        return druid.getMaxActive() == 20;
                    } else if (druid.getName().equals("slave")) {
                        return druid.getMaxActive() == 30;
                    } else {
                        throw new IllegalArgumentException("unexpected name: " + druid.getName());
                    }
                });
    }

    @Test
    public void checkDataSourceMethodPointcutAdvisor() {
        Map<String, DataSourcePointcutAdvisor> beans = applicationContext
                .getBeansOfType(DataSourcePointcutAdvisor.class);
        assertThat(beans).hasSize(3)
                .containsKeys("dataSourcePointcutAdvisor", "slaveDataSourcePointcutAdvisor0",
                        "slaveDataSourcePointcutAdvisor1");

        DataSourceRoutingProperties routingProperties = applicationContext.getBean(DataSourceRoutingProperties.class);
        assertThat(routingProperties).matches(properties -> {
            GroupDataSourceProperty groupDataSourceProperty = properties.getGroupDataSourceConfig();
            return (groupDataSourceProperty.getLoadBalancer() == RoundRobinDataSourceLoadBalanced.class
                    && JsonApi.toJson(groupDataSourceProperty.getInitConfig()).equals("{\"key1\":\"value1\"}"));
        }).matches(properties -> {
            GroupDataSourceProperty groupDataSourceProperty = properties.getGroupDataSourceConfigs().get("test");
            return (groupDataSourceProperty == null);
        }).matches(properties -> {
            GroupDataSourceProperty groupDataSourceProperty = properties.getGroupDataSourceConfigs().get("test2");
            return (groupDataSourceProperty.getLoadBalancer() == RandomDataSourceLoadBalanced.class
                    && JsonApi.toJson(groupDataSourceProperty.getInitConfig()).equals("{\"key2\":\"value2\"}"));
        });
    }

    @Test
    public void createTest() {
        User record = new User();
        String name = "Jerry-" + RandomStringUtils.randomAlphabetic(10);
        record.setName(name);
        record.setEmployeeNumber("001" + RandomStringUtils.randomAlphabetic(3));
        Result<Long> createResult = userService.create(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(createResult).isNotNull();
            softly.assertThat(createResult.isSuccess()).isTrue();
            softly.assertThat(createResult.getErrorCode()).isNull();
            softly.assertThat(createResult.getErrorMsg()).isNull();
            softly.assertThat(createResult.getValue()).isNotNull();
        });

        // 这里查询的是从库，是找不到记录的
        Result<Integer> countResult = userService.findCount(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(countResult).isNotNull();
            softly.assertThat(countResult.isSuccess()).isTrue();
            softly.assertThat(countResult.getErrorCode()).isNull();
            softly.assertThat(countResult.getErrorMsg()).isNull();
            softly.assertThat(countResult.getValue()).isNotNull().isEqualTo(0);
        });

        // 删除数据
        User deleteUser = new User();
        deleteUser.setId(record.getId());
        Result<Boolean> deleteResult = userService.deleteByCondition(deleteUser);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(deleteResult).isNotNull();
            softly.assertThat(deleteResult.isSuccess()).isTrue();
            softly.assertThat(deleteResult.getErrorCode()).isNull();
            softly.assertThat(deleteResult.getErrorMsg()).isNull();
            softly.assertThat(deleteResult.getValue()).isNotNull().isEqualTo(true);
        });

        // 这里删除的是从库，是找不到记录的
        Result<Boolean> deleteResult2 = userService.deleteByPrimaryKey(deleteUser);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(deleteResult2).isNotNull();
            softly.assertThat(deleteResult2.isSuccess()).isFalse();
            softly.assertThat(deleteResult2.getErrorCode()).isNotBlank().isEqualTo(MybatisConstants.NOT_MATCH_RECORDS);
            softly.assertThat(deleteResult2.getErrorMsg()).isNotBlank().isEqualTo("更新失败，未匹配到相应的记录");
            softly.assertThat(deleteResult2.getValue()).isNotNull().isEqualTo(false);
        });
    }

    @EnableAutoConfiguration
    @ComponentScan(basePackageClasses = DruidDataSourceCase2Tests.class)
    @Profile("DruidDataSourceCase2Tests")
    @Configuration
    public static class MyConfig {
        @Bean
        public ParameterHandlerCustomizer persistenceParameterHandler() {
            return new DefaultParameterHandlerCustomizer();
        }
    }
}
