package com.project.frame.model.core;

import com.project.frame.model.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户角色类
 *
 * @author mxy
 * @date 2019/12/15
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserRole extends BaseEntity {
    private static final long serialVersionUID = -5267710533185427593L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;
}
