package com.project.frame.model.core;

import com.project.frame.model.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色菜单类
 *
 * @author mxy
 * @date 2019/12/15
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleMenu extends BaseEntity {
    private static final long serialVersionUID = -5427331221946899123L;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 菜单ID
     */
    private Long menuId;
}
