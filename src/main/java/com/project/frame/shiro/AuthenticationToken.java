package com.project.frame.shiro;

import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * 登录令牌 - 认证时使用
 *
 * @author mxy
 * @date 2019/12/16
 */
public class AuthenticationToken extends UsernamePasswordToken {

    private static final long serialVersionUID = 4628652632307774263L;

    // 验证码ID
    private String captchaId;

    // 验证码
    private String captcha;

    // ip保留
    public AuthenticationToken(String loginName, String password, boolean rememberMe, String ip, String captchaId, String captcha) {
        super(loginName, password, rememberMe);
        this.captchaId = captchaId;
        this.captcha = captcha;
    }

    public String getCaptchaId() {
        return captchaId;
    }

    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

}
