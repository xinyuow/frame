package com.project.frame.controller.common;

import com.project.frame.shiro.AuthenticationToken;
import com.project.frame.utils.exception.BASE_RESPONSE_CODE_ENUM;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 登录控制器
 *
 * @author mxy
 * @date 2019/12/17
 */
@RestController()
@RequestMapping("/admin")
public class LoginController extends BaseController {
    private static final long serialVersionUID = -7625287497326488077L;

    /**
     * 登录接口
     *
     * @param loginName  登录名称
     * @param enPassword 登录密码
     * @return 操作结果
     */
    @PostMapping(value = "/login")
    public Map<String, Object> login(String loginName, String enPassword) {
        try {
            Subject subject = SecurityUtils.getSubject();
            AuthenticationToken token = new AuthenticationToken(loginName, enPassword, false, "", "", "");
            // 调用doGetAuthenticationInfo
            subject.login(token);

            return getSuccessResult();
        } catch (UnknownAccountException | IncorrectCredentialsException e) {
            // 账号或密码错误
            return getFailResult(BASE_RESPONSE_CODE_ENUM.ACCOUNT_OR_PASSWORD_ERROR.getCode(), BASE_RESPONSE_CODE_ENUM.ACCOUNT_OR_PASSWORD_ERROR.getMsg());
        } catch (Exception exception) {
            // 其他异常
            return getFailResult(BASE_RESPONSE_CODE_ENUM.REQUEST_FAIL.getCode(), BASE_RESPONSE_CODE_ENUM.REQUEST_FAIL.getMsg());
        }
    }
}
