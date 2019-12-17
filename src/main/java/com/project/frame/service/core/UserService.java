package com.project.frame.service.core;

import com.project.frame.model.core.User;
import com.project.frame.service.common.BaseService;

/**
 * 用户 业务接口类
 *
 * @author mxy
 * @date 2019/12/15
 */
public interface UserService extends BaseService<User, Long> {

    /**
     * 根据登录名称查询用户
     *
     * @param loginName 登录名称
     * @return 用户对象
     */
    User getByLoginName(String loginName);

    /**
     * 查询用户并级联查询用户角色
     *
     * @param userId 用户ID
     * @return 用户对象
     */
    User getUserCascadeRole(Long userId);
}
