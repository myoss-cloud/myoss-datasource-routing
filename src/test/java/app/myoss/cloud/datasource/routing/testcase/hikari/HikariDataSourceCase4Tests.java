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

package app.myoss.cloud.datasource.routing.testcase.hikari;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.zaxxer.hikari.HikariDataSource;

import app.myoss.cloud.core.constants.MyossConstants;
import app.myoss.cloud.core.lang.dto.Page;
import app.myoss.cloud.core.lang.dto.Result;
import app.myoss.cloud.core.lang.dto.Sort;
import app.myoss.cloud.datasource.routing.aspectj.DataSourcePointcutAdvisor;
import app.myoss.cloud.datasource.routing.config.GroupDataSourceProperty;
import app.myoss.cloud.datasource.routing.jdbc.GroupDataSource;
import app.myoss.cloud.datasource.routing.jdbc.MultiDataSource;
import app.myoss.cloud.datasource.routing.jdbc.loadbalancer.impl.RandomDataSourceLoadBalanced;
import app.myoss.cloud.datasource.routing.spring.boot.autoconfigure.DataSourceRoutingAutoConfiguration;
import app.myoss.cloud.datasource.routing.spring.boot.autoconfigure.DataSourceRoutingProperties;
import app.myoss.cloud.datasource.routing.testcase.hikari.HikariDataSourceCase4Tests.MyConfig;
import app.myoss.cloud.datasource.routing.testcase.hikari.history.entity.UserHistory;
import app.myoss.cloud.datasource.routing.testcase.hikari.history.service.UserHistoryService;
import app.myoss.cloud.datasource.routing.testcase.hikari.history4slave.UserHistoryService4Slave;
import app.myoss.cloud.mybatis.plugin.ParameterHandlerCustomizer;
import app.myoss.cloud.mybatis.repository.entity.AuditIdEntity;
import app.myoss.cloud.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link com.zaxxer.hikari.HikariDataSource} 数据源路由测试，使用
 * {@link app.myoss.cloud.datasource.routing.constants.MethodPointcutEnum#AspectJExpression}
 * 表记 app.myoss.cloud.datasource.routing.testcase.hikari.history
 * 这个package及所有子package下任何类的任何方法
 *
 * @author Jerry.Chen
 * @since 2018年5月11日 上午10:45:16
 */
@ActiveProfiles({ "h2-hikari-case4", "HikariDataSourceCase4Tests" })
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { DataSourceRoutingAutoConfiguration.class, MyConfig.class, AopAutoConfiguration.class,
        MybatisAutoConfiguration.class, JdbcTemplateAutoConfiguration.class })
public class HikariDataSourceCase4Tests {
    @Autowired
    private UserHistoryService       userHistoryService;
    @Autowired
    private UserHistoryService4Slave userHistoryService4Slave;
    @Autowired
    private ApplicationContext       applicationContext;
    @Autowired
    private DataSource               dataSource;

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
                assertThat(groupDataSource).matches(
                        source -> source.getDataSourceLoadBalancer().getClass() == RandomDataSourceLoadBalanced.class);
            } else {
                checkDataSourceValue(value);
            }
        }
    }

    private void checkDataSourceValue(DataSource dataSource) {
        HikariDataSource target = (HikariDataSource) dataSource;
        assertThat(target).matches(hikari -> hikari.getConnectionTestQuery() == null).matches(hikari -> {
            Properties properties = hikari.getDataSourceProperties();
            boolean isTrue = properties.size() == 0;
            if (hikari.getPoolName().equals("master")) {
                return isTrue && hikari.getMaximumPoolSize() == -1;
            } else if (hikari.getPoolName().equals("slave")) {
                return isTrue && hikari.getMaximumPoolSize() == -1;
            } else {
                throw new IllegalArgumentException("unexpected name: " + hikari.getPoolName());
            }
        });
    }

    @Test
    public void checkDataSourceMethodPointcutAdvisor() {
        Map<String, DataSourcePointcutAdvisor> beans = applicationContext
                .getBeansOfType(DataSourcePointcutAdvisor.class);
        assertThat(beans).hasSize(2).containsKeys("dataSourcePointcutAdvisor", "masterDataSourcePointcutAdvisor0");

        DataSourceRoutingProperties routingProperties = applicationContext.getBean(DataSourceRoutingProperties.class);
        assertThat(routingProperties).matches(properties -> {
            GroupDataSourceProperty groupDataSourceProperty = properties.getGroupDataSourceConfig();
            return (groupDataSourceProperty.getLoadBalancer() == RandomDataSourceLoadBalanced.class
                    && groupDataSourceProperty.getInitConfig() == null);
        }).matches(properties -> properties.getGroupDataSourceConfigs() == null);
    }

    /**
     * 增删改查测试案例1
     */
    @Test
    public void crudTest1() {
        Long exceptedId = 1L;
        UserHistory record = new UserHistory();
        record.setEmployeeNumber("10000");
        record.setName("Jerry");

        // 创建记录
        Result<Long> createResult = userHistoryService.create(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(createResult).isNotNull();
            softly.assertThat(createResult.isSuccess()).isTrue();
            softly.assertThat(createResult.getErrorCode()).isNull();
            softly.assertThat(createResult.getErrorMsg()).isNull();
            softly.assertThat(createResult.getValue()).isNotNull().isEqualTo(exceptedId);
        });

        // 使用各种查询 API 查询数据库中的记录，和保存之后的记录进行比较
        Result<UserHistory> idResult = userHistoryService.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult).isNotNull();
            softly.assertThat(idResult.isSuccess()).isTrue();
            softly.assertThat(idResult.getErrorCode()).isNull();
            softly.assertThat(idResult.getErrorMsg()).isNull();
            softly.assertThat(idResult.getValue()).isNotNull().isEqualTo(record);
        });

        Result<UserHistory> idResult2 = userHistoryService.findByPrimaryKey(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult2).isNotNull();
            softly.assertThat(idResult2.isSuccess()).isTrue();
            softly.assertThat(idResult2.getErrorCode()).isNull();
            softly.assertThat(idResult2.getErrorMsg()).isNull();
            softly.assertThat(idResult2.getValue()).isNotNull().isEqualTo(record);
        });

        Result<List<UserHistory>> listResult = userHistoryService.findList(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(listResult).isNotNull();
            softly.assertThat(listResult.isSuccess()).isTrue();
            softly.assertThat(listResult.getErrorCode()).isNull();
            softly.assertThat(listResult.getErrorMsg()).isNull();
            softly.assertThat(listResult.getValue()).isNotEmpty().hasSize(1);
            softly.assertThat(listResult.getValue().get(0)).isEqualTo(record);
        });

        Result<UserHistory> oneResult = userHistoryService.findOne(record);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(oneResult).isNotNull();
            softly.assertThat(oneResult.isSuccess()).isTrue();
            softly.assertThat(oneResult.getErrorCode()).isNull();
            softly.assertThat(oneResult.getErrorMsg()).isNull();
            softly.assertThat(oneResult.getValue()).isNotNull().isEqualTo(record);
        });

        // 这里查询的 "从数据库"，所以找不到记录
        List<UserHistory> listResult4Slave = userHistoryService4Slave.findList();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(listResult4Slave).isNotNull();
            softly.assertThat(listResult4Slave).isEmpty();
        });

        Page<UserHistory> pageCondition = new Page<>();
        pageCondition.setParam(record);
        pageCondition.setSort(new Sort("id", "name"));
        Result<List<UserHistory>> sortResult = userHistoryService.findListWithSort(pageCondition);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(sortResult).isNotNull();
            softly.assertThat(sortResult.isSuccess()).isTrue();
            softly.assertThat(sortResult.getErrorCode()).isNull();
            softly.assertThat(sortResult.getErrorMsg()).isNull();
            softly.assertThat(sortResult.getValue()).isNotEmpty().hasSize(1);
            softly.assertThat(sortResult.getValue().get(0)).isEqualTo(record);
        });

        Page<UserHistory> pageResult = userHistoryService.findPage(pageCondition);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(pageResult).isNotNull();
            softly.assertThat(pageResult.isSuccess()).isTrue();
            softly.assertThat(pageResult.getErrorCode()).isNull();
            softly.assertThat(pageResult.getErrorMsg()).isNull();
            softly.assertThat(pageResult.getPageNum()).isEqualTo(1);
            softly.assertThat(pageResult.getPageSize()).isEqualTo(Page.DEFAULT_PAGE_SIZE);
            softly.assertThat(pageResult.getTotalCount()).isEqualTo(1);
            softly.assertThat(pageResult.getValue()).isNotEmpty().hasSize(1);
            softly.assertThat(pageResult.getValue().get(0)).isEqualTo(record);
        });

        // 更新数据
        UserHistory updateUser = new UserHistory();
        updateUser.setId(exceptedId);
        updateUser.setAccount("10000");
        updateUser.setName("LEO");
        Result<Boolean> updateResult = userHistoryService.updateByPrimaryKey(updateUser);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(updateResult).isNotNull();
            softly.assertThat(updateResult.isSuccess()).isTrue();
            softly.assertThat(updateResult.getErrorCode()).isNull();
            softly.assertThat(updateResult.getErrorMsg()).isNull();
            softly.assertThat(updateResult.getValue()).isNotNull().isEqualTo(true);
            softly.assertThat(updateUser.getModifier()).isNotNull();
            softly.assertThat(updateUser.getGmtModified()).isNotNull();
        });
        record.setModifier(updateUser.getModifier());
        record.setGmtModified(updateUser.getGmtModified());

        Result<UserHistory> idResult3 = userHistoryService.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult3).isNotNull();
            softly.assertThat(idResult3.isSuccess()).isTrue();
            softly.assertThat(idResult3.getErrorCode()).isNull();
            softly.assertThat(idResult3.getErrorMsg()).isNull();
            softly.assertThat(idResult3.getValue()).isNotNull().isNotEqualTo(record);

            UserHistory target = new UserHistory();
            BeanUtils.copyProperties(record, target);
            target.setAccount(updateUser.getAccount());
            target.setName(updateUser.getName());
            softly.assertThat(idResult3.getValue()).isNotNull().isEqualTo(target);
        });

        // 删除数据
        UserHistory deleteUser = new UserHistory();
        deleteUser.setId(exceptedId);
        deleteUser.setAccount("10000");
        Result<Boolean> deleteResult = userHistoryService.deleteByPrimaryKey(deleteUser);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(deleteResult).isNotNull();
            softly.assertThat(deleteResult.isSuccess()).isTrue();
            softly.assertThat(deleteResult.getErrorCode()).isNull();
            softly.assertThat(deleteResult.getErrorMsg()).isNull();
            softly.assertThat(deleteResult.getValue()).isNotNull().isEqualTo(true);
        });

        Result<UserHistory> idResult4 = userHistoryService.findByPrimaryKey(exceptedId);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(idResult4).isNotNull();
            softly.assertThat(idResult4.isSuccess()).isTrue();
            softly.assertThat(idResult4.getErrorCode()).isNull();
            softly.assertThat(idResult4.getErrorMsg()).isNull();
            softly.assertThat(idResult4.getValue()).isNull();
        });
    }

    @EnableAutoConfiguration
    @ComponentScan(basePackageClasses = HikariDataSourceCase4Tests.class)
    @Profile("HikariDataSourceCase4Tests")
    @Configuration
    public static class MyConfig {
        @Bean
        public ParameterHandlerCustomizer persistenceParameterHandler() {
            return new ParameterHandlerCustomizer() {
                @Override
                public void handlerInsert(MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject) {
                    AuditIdEntity metaObject = (AuditIdEntity) parameterObject;
                    metaObject.setIsDeleted(MyossConstants.N);
                    metaObject.setCreator("system");
                    metaObject.setModifier("system");
                    metaObject.setGmtCreated(new Date());
                    metaObject.setGmtModified(new Date());
                }

                @Override
                public void handlerUpdate(MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject) {
                    AuditIdEntity metaObject = (AuditIdEntity) parameterObject;
                    metaObject.setModifier("system");
                    metaObject.setGmtModified(new Date());
                }
            };
        }
    }
}
