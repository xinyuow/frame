package com.project.frame.mapper.core;

import com.project.frame.mapper.common.BaseMapper;
import com.project.frame.model.core.Menu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单Mapper
 *
 * @author mxy
 * @date 2019/12/15
 */
public interface MenuMapper extends BaseMapper<Menu, Long> {

    /**
     * 通过用户ID查询对应的菜单权限集合
     *
     * @param userId 用户ID
     * @return 菜单权限集合
     */
    List<Menu> findOrdinaryMenu(@Param("userId") Long userId);
}