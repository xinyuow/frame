package com.project.frame.service.core.impl;

import com.project.frame.model.core.Role;
import com.project.frame.service.common.impl.BaseServiceImpl;
import com.project.frame.service.core.RoleService;
import org.springframework.stereotype.Service;

/**
 * 角色 业务实现类
 *
 * @author mxy
 * @date 2019/12/16
 */
@Service("roleServiceImpl")
public class RoleServiceImpl extends BaseServiceImpl<Role, Long> implements RoleService {
    private static final long serialVersionUID = -4451596683892473864L;
}
