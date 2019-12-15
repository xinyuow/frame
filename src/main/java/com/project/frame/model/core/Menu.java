package com.project.frame.model.core;

import com.project.frame.model.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单类
 *
 * @author mxy
 * @date 2019/12/15
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Menu extends BaseEntity {
    private static final long serialVersionUID = -2525335985698556735L;

    /**
     * 父级菜单ID
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 权限路径
     */
    private String url;

    /**
     * 排序
     */
    private int sort;

    /**
     * 菜单备注
     */
    private String remark;
}
