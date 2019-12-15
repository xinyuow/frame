package com.project.frame.service.core.impl;

import com.project.frame.model.core.User;
import com.project.frame.service.common.impl.BaseServiceImpl;
import com.project.frame.service.core.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户 业务实现类
 *
 * @author mxy
 * @date 2019/12/15
 */
@Service("userServiceImpl")
public class UserServiceImpl extends BaseServiceImpl<User, Long> implements UserService {
    private static final long serialVersionUID = 6634653112247755914L;
}
