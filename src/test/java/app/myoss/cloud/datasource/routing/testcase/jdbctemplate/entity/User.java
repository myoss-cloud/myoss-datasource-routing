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

package app.myoss.cloud.datasource.routing.testcase.jdbctemplate.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * This class corresponds to the database table t_sys_user
 * <p>
 * Database Table Remarks: 系统用户信息表
 * </p>
 *
 * @author jerry
 * @since 2018年5月11日 上午10:41:47
 */
@Accessors(chain = true)
@Data
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Database Column Name: t_sys_user.id
     * <p>
     * Database Column Remarks: 主键id
     * </p>
     */
    private Long              id;

    /**
     * Database Column Name: t_sys_user.employee_number
     * <p>
     * Database Column Remarks: 员工编号
     * </p>
     */
    private String            employeeNumber;

    /**
     * Database Column Name: t_sys_user.account
     * <p>
     * Database Column Remarks: 登录账号
     * </p>
     */
    private String            account;

    /**
     * Database Column Name: t_sys_user.password
     * <p>
     * Database Column Remarks: 密码
     * </p>
     */
    private String            password;

    /**
     * Database Column Name: t_sys_user.salt
     * <p>
     * Database Column Remarks: 密钥
     * </p>
     */
    private String            salt;

    /**
     * Database Column Name: t_sys_user.name
     * <p>
     * Database Column Remarks: 姓名
     * </p>
     */
    private String            name;

    /**
     * Database Column Name: t_sys_user.gender
     * <p>
     * Database Column Remarks: 性别
     * </p>
     */
    private String            gender;

    /**
     * Database Column Name: t_sys_user.birthday
     * <p>
     * Database Column Remarks: 生日
     * </p>
     */
    private Date              birthday;

    /**
     * Database Column Name: t_sys_user.email
     * <p>
     * Database Column Remarks: 邮箱
     * </p>
     */
    private String            email;

    /**
     * Database Column Name: t_sys_user.phone
     * <p>
     * Database Column Remarks: 联系电话
     * </p>
     */
    private String            phone;

    /**
     * Database Column Name: t_sys_user.telephone
     * <p>
     * Database Column Remarks: 工作电话
     * </p>
     */
    private String            telephone;

    /**
     * Database Column Name: t_sys_user.company_id
     * <p>
     * Database Column Remarks: 所属公司
     * </p>
     */
    private Long              companyId;

    /**
     * Database Column Name: t_sys_user.dept_id
     * <p>
     * Database Column Remarks: 用户所属部门
     * </p>
     */
    private Long              deptId;

    /**
     * Database Column Name: t_sys_user.position_id
     * <p>
     * Database Column Remarks: 所在职位
     * </p>
     */
    private Long              positionId;

    /**
     * Database Column Name: t_sys_user.parent_user_id
     * <p>
     * Database Column Remarks: 归属领导id
     * </p>
     */
    private Long              parentUserId;

    /**
     * Database Column Name: t_sys_user.status
     * <p>
     * Database Column Remarks: 状态（1: 启用; 2: 禁用）
     * </p>
     */
    private String            status;

    /**
     * Database Column Name: t_sys_user.entry_date
     * <p>
     * Database Column Remarks: 入职时间
     * </p>
     */
    private Date              entryDate;

    /**
     * Database Column Name: t_sys_user.leave_date
     * <p>
     * Database Column Remarks: 离职日期
     * </p>
     */
    private Date              leaveDate;

    /**
     * Database Column Name: t_sys_user.is_deleted
     * <p>
     * Database Column Remarks: 是否删除
     * </p>
     */
    private String            isDeleted;

    /**
     * Database Column Name: t_sys_user.creator
     * <p>
     * Database Column Remarks: 创建者
     * </p>
     */
    private String            creator;

    /**
     * Database Column Name: t_sys_user.modifier
     * <p>
     * Database Column Remarks: 修改者
     * </p>
     */
    private String            modifier;

    /**
     * Database Column Name: t_sys_user.gmt_created
     * <p>
     * Database Column Remarks: 创建时间
     * </p>
     */
    private Date              gmtCreated;

    /**
     * Database Column Name: t_sys_user.gmt_modified
     * <p>
     * Database Column Remarks: 修改时间
     * </p>
     */
    private Date              gmtModified;

}
