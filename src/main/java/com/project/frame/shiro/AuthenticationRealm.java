package com.project.frame.shiro;

import com.alibaba.fastjson.JSON;
import com.project.frame.model.core.Menu;
import com.project.frame.model.core.Role;
import com.project.frame.model.core.User;
import com.project.frame.service.core.MenuService;
import com.project.frame.service.core.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import javax.annotation.Resource;
import java.util.*;

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
            // 未知用户，认证失败
            throw new AuthenticationException();
        }
    }

    /**
     * 获取授权信息
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        // 从缓存Realm中获取自定义的主体
        Object object = principals.fromRealm(getName()).iterator().next();
        /*
            此处不能直接转成Principal对象的原因是：
            网上说是ClassLoader类加载器的不同导致的类型转换异常，
            项目启动时加载项目中的类使用的加载器都是 org.springframework.boot.devtools.restart.classloader.RestartClassLoader
            而从shiro session 取出来的对象（从redis中取出经过反序列化）的类加载器都是 sun.misc.Launcher.AppClassLoader，
            很明显会导致类型转换异常，原来Spring的dev-tools为了实现重新装载class自己实现了一个类加载器，
            来加载项目中会改变的类，方便重启时将新改动的内容更新进来。
            解决方案1：可以去掉pom文件中的spring-boot-devtools
            解决方案2：就是以下的方法
         */
        Principal principal;
        if(object instanceof Principal) {
            principal = (Principal) object;
        } else {
            principal = JSON.parseObject(JSON.toJSON(object).toString(), Principal.class);
        }

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

    /**
     * 鉴权时调用
     *
     * @param principals 身份集合
     * @param permission 访问的权限
     * @return 鉴权结果
     */
    @Override
    public boolean isPermitted(PrincipalCollection principals, Permission permission) {
        AuthorizationInfo info = getAuthorizationInfo(principals);
        Collection<Permission> perms = getPermissions(info);
        if (CollectionUtils.isEmpty(perms)) {
            return false;
        }
        // 鉴权
        for (Permission perm : perms) {
            if (perm.implies(permission)) {
                return true;
            }
        }
        return false;
    }
}
