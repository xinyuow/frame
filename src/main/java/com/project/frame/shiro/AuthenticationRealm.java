package com.project.frame.shiro;

import com.project.frame.model.core.Menu;
import com.project.frame.model.core.Role;
import com.project.frame.model.core.User;
import com.project.frame.service.core.MenuService;
import com.project.frame.service.core.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 认证
 *
 * @author mxy
 * @date 2019/12/16
 */
public class AuthenticationRealm extends AuthorizingRealm {

    @Resource(name = "userServiceImpl")
    private UserService userService;

    @Resource(name = "menuServiceImpl")
    private MenuService menuService;

    /**
     * 获取认证信息
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(org.apache.shiro.authc.AuthenticationToken authenticationToken) throws AuthenticationException {
        AuthenticationToken authToken = (AuthenticationToken) authenticationToken;
        // 获取登录名、密码
        String loginName = authToken.getUsername();
        String password = new String(authToken.getPassword());

        // 判断登录信息
        if (loginName != null && password != null) {
            // 获取登录用户信息
            User user = userService.getByLoginName(loginName);

            // 判断用户是否存在
            if (user == null || user.getDelFlag()) {
                throw new UnknownAccountException();
            }

            // 判断是否可用
            if (user.getStatus().equals(User.STATUS_ENUM.DISABLE.getValue())) {
                throw new DisabledAccountException();
            }

            // 判断是否锁定
            if (user.getLockFlag()) {
                // 账号锁定分钟数
                Date lockedDate = user.getLockedDate();
                Date unlockedDate = DateUtils.addMinutes(lockedDate, 10);

                // 判断锁定时间是否已过
                if (new Date().after(unlockedDate)) {
                    // 更新锁定信息
                    user.setLoginFailCnt(0);
                    user.setLockFlag(false);
                    user.setLockedDate(null);
                    userService.update(user);
                } else {
                    throw new LockedAccountException();
                }
            }

            // 密码错误
            if (!DigestUtils.md5Hex(password).equals(user.getLoginPwd())) {
                // 设置失败次数+1
                int loginFailCount = user.getLoginFailCnt() + 1;
                // 如果失败次数大于等于指定次数，则锁定账户。规定时间后登录会自动解除锁定。
                if (loginFailCount >= 5) {
                    user.setLockFlag(true);
                    user.setLockedDate(new Date());
                }
                user.setLoginFailCnt(loginFailCount);
                userService.update(user);
                throw new IncorrectCredentialsException();
            }

            // 更新用户信息，并清空之前失败的次数
            user.setLoginFailCnt(0);
            userService.update(user);
            return new SimpleAuthenticationInfo(new Principal(user.getId(), loginName), password, getName());
        } else {
            // 未知用户
            throw new UnknownAccountException();
        }
    }

    /**
     * 获取授权信息
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        // 从缓存Realm中获取自定义的主体
        Principal principal = (Principal) principals.fromRealm(getName()).iterator().next();

        if (principal != null && null != principal.getId()) {
            // 使用SimpleAuthorizationInfo做授权
            SimpleAuthorizationInfo authInfo = new SimpleAuthorizationInfo();

            // 获取admin对象，并级联查询对应的角色信息集合
            User user = userService.getUserCascadeRole(principal.getId());

            // 获取用户已启用的角色信息
            Set<String> roleSet = new HashSet<>();
            for (Role role : user.getRoles()) {
                if (role.getStatus().equals(Role.STATUS_ENUM.ENABLE.getValue())) {
                    roleSet.add(role.getRoleCode());
                }
            }

            // 通过用户ID查询对应的菜单权限集合
            List<Menu> menuList = menuService.findOrdinaryMenu(principal.getId());

            // 获取用户不为空的菜单权限集合
            Set<String> menuSet = new HashSet<>();
            for (Menu menu : menuList) {
                if (StringUtils.isNotBlank(menu.getUrl())) {
                    menuSet.add(menu.getUrl());
                }
            }

            // 将角色和权限放入授权对象中
            authInfo.addRoles(roleSet);
            authInfo.addStringPermissions(menuSet);
            return authInfo;
        } else {
            return null;
        }
    }

}
