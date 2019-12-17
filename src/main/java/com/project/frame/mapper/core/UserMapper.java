package com.project.frame.mapper.core;

import com.project.frame.mapper.common.BaseMapper;
import com.project.frame.model.core.User;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper
 *
 * @author mxy
 * @date 2019/12/15
 */
public interface UserMapper extends BaseMapper<User, Long> {

    /**
     * 根据登录名称查询用户
     *
     * @param loginName 登录名称
     * @return 用户对象
     */
    User getByLoginName(@Param("loginName") String loginName);

    /**
     * 查询用户并级联查询用户角色
     *
     * @param userId 用户ID
     * @return 用户对象
     */
    User getUserCascadeRole(@Param("userId") Long userId);
}