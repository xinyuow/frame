package com.project.frame.service.core.impl;

import com.project.frame.mapper.core.MenuMapper;
import com.project.frame.model.core.Menu;
import com.project.frame.service.common.impl.BaseServiceImpl;
import com.project.frame.service.core.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 菜单 业务实现类
 *
 * @author mxy
 * @date 2019/12/16
 */
@Service("menuServiceImpl")
public class MenuServiceImpl extends BaseServiceImpl<Menu, Long> implements MenuService {
    private static final long serialVersionUID = 2122723016353610611L;

    @Autowired
    private MenuMapper menuMapper;

    /**
     * 通过用户ID查询对应的菜单权限集合
     *
     * @param userId 用户ID
     * @return 菜单权限集合
     */
    @Override
    public List<Menu> findOrdinaryMenu(Long userId) {
        return menuMapper.findOrdinaryMenu(userId);
    }
}
