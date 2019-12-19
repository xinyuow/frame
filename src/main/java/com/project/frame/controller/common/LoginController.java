package com.project.frame.controller.common;

import com.project.frame.shiro.AuthenticationToken;
import com.project.frame.utils.exception.BASE_RESPONSE_CODE_ENUM;
import com.project.frame.utils.exception.BaseCustomException;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录控制器
 *
 * @author mxy
 * @date 2019/12/17
 */
@RestController
@RequestMapping(value = "/admin")
public class LoginController extends BaseController {
    private static final long serialVersionUID = -7625287497326488077L;

    /**
     * 登录接口
     *
     * @param loginName 登录名称
     * @param password  登录密码
     * @return 操作结果
     */
    @PostMapping(value = "/login")
    public Map<String, Object> login(String loginName, String password) {
        // 参数校验
        if (StringUtils.isBlank(loginName) || StringUtils.isBlank(password)) {
            throw new BaseCustomException(BASE_RESPONSE_CODE_ENUM.REQUEST_PARAMS_ERROR);
        }
        try {
            Subject subject = SecurityUtils.getSubject();
            AuthenticationToken token = new AuthenticationToken(loginName, password, false, "", "", "");
            // 调用doGetAuthenticationInfo
            subject.login(token);

            Map<String, Object> map = new HashMap<>();
            map.put("token", subject.getSession().getId());
            return getResult(map);
        } catch (UnknownAccountException | IncorrectCredentialsException e) {
            // 账号不存在或密码错误
            return getFailResult(BASE_RESPONSE_CODE_ENUM.ACCOUNT_OR_PASSWORD_ERROR.getCode(), BASE_RESPONSE_CODE_ENUM.ACCOUNT_OR_PASSWORD_ERROR.getMsg());
        } catch (LockedAccountException e) {
            // 账号被锁定
            return getFailResult(BASE_RESPONSE_CODE_ENUM.ACCOUNT_IS_LOCKED.getCode(), BASE_RESPONSE_CODE_ENUM.ACCOUNT_IS_LOCKED.getMsg());
        } catch (DisabledAccountException e) {
            // 账号被禁用
            return getFailResult(BASE_RESPONSE_CODE_ENUM.ACCOUNT_HAS_BEEN_DISABLED.getCode(), BASE_RESPONSE_CODE_ENUM.ACCOUNT_HAS_BEEN_DISABLED.getMsg());
        } catch (AuthenticationException e) {
            // 认证失败
            return getFailResult(BASE_RESPONSE_CODE_ENUM.ACCOUNT_AUTHENTICATION_FAILED.getCode(), BASE_RESPONSE_CODE_ENUM.ACCOUNT_AUTHENTICATION_FAILED.getMsg());
        }
    }

    /**
     * 重新登录
     *
     * @return 操作结果
     */
    @GetMapping(value = "/notLogin")
    public Map<String, Object> notLogin() {
        // 告知用户需要重新登录
        return getFailResult(BASE_RESPONSE_CODE_ENUM.NOT_LOGIN_ERROR.getCode(), BASE_RESPONSE_CODE_ENUM.NOT_LOGIN_ERROR.getMsg());
    }

    /**
     * 权限不足
     *
     * @return 操作结果
     */
    @GetMapping(value = "/unauthorized")
    public Map<String, Object> unauthorized() {
        // 告知用户权限不足
        return getFailResult(BASE_RESPONSE_CODE_ENUM.UNAUTHORIZED.getCode(), BASE_RESPONSE_CODE_ENUM.UNAUTHORIZED.getMsg());
    }

    /**
     * 退出登录
     *
     * @return 操作结果
     */
    @GetMapping(value = "/logout")
    public Map<String, Object> logout() {
        // 注销登录
        SecurityUtils.getSubject().logout();
        return getSuccessResult();
    }
}
