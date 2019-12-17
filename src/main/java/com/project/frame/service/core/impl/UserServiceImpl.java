package com.project.frame.service.core.impl;

import com.project.frame.mapper.core.UserMapper;
import com.project.frame.model.core.User;
import com.project.frame.service.common.impl.BaseServiceImpl;
import com.project.frame.service.core.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private UserMapper userMapper;

    /**
     * 根据登录名称查询用户
     *
     * @param loginName 登录名称
     * @return 用户对象
     */
    @Override
    public User getByLoginName(String loginName) {
        return userMapper.getByLoginName(loginName);
    }

    /**
     * 查询用户并级联查询用户角色
     *
     * @param userId 用户ID
     * @return 用户对象
     */
    @Override
    public User getUserCascadeRole(Long userId) {
        return userMapper.getUserCascadeRole(userId);
    }
}
