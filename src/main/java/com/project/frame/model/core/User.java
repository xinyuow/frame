package com.project.frame.model.core;

import com.project.frame.model.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 用户类
 *
 * @author mxy
 * @date 2019/12/15
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    private static final long serialVersionUID = -4687324719648772244L;

    /**
     * 登录名称
     */
    private String loginName;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 登录密码
     */
    private String loginPwd;

    /**
     * 用户状态
     * 0：启用
     * 1：禁用
     */
    private Integer status;

    /**
     * 用户状态枚举
     */
    public enum STATUS_ENUM {
        ENABLE(0, "启用"),
        DISABLE(1, "禁用");

        /**
         * 枚举值
         */
        private Integer value;

        /**
         * 枚举名称
         */
        private String name;

        /**
         * 枚举有参构造函数
         *
         * @param value 枚举值
         * @param name  枚举名
         */
        STATUS_ENUM(Integer value, String name) {
            this.value = value;
            this.name = name;
        }

        /**
         * 获取枚举值
         */
        public Integer getValue() {
            return value;
        }

        /**
         * 获取枚举名
         */
        public String getName() {
            return name;
        }
    }

    /**
     * 登录失败次数
     */
    private Integer loginFailCnt;

    /**
     * 是否锁定  1:是  0:否
     */
    private Boolean lockFlag;

    /**
     * 锁定时间
     */
    private Date lockedDate;

    /**
     * 是否删除  1:是  0:否
     */
    private Boolean delFlag;


    /* ************************* 扩展属性 ********************************/

    /**
     * 用户对应的角色信息集合 - 登录时查询使用
     */
    private List<Role> roles;
}
