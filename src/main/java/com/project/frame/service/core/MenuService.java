package com.project.frame.service.core;

import com.project.frame.model.core.Menu;
import com.project.frame.service.common.BaseService;

import java.util.List;

/**
 * 菜单 业务接口类
 *
 * @author mxy
 * @date 2019/12/16
 */
public interface MenuService extends BaseService<Menu, Long> {

    /**
     * 通过用户ID查询对应的菜单权限集合
     *
     * @param userId 用户ID
     * @return 菜单权限集合
     */
    List<Menu> findOrdinaryMenu(Long userId);
}
