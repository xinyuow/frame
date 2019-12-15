package com.project.frame.model.core;

import com.project.frame.model.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色类
 *
 * @author mxy
 * @date 2019/12/15
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Role extends BaseEntity {
    private static final long serialVersionUID = 2683117216877573631L;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色状态
     * 0：启用
     * 1：禁用
     */
    private Integer status;

    /**
     * 角色状态枚举
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
     * 是否系统角色 - 系统角色不允许删除
     */
    private Boolean isSys;
}
