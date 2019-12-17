package com.project.frame.shiro;

import com.alibaba.fastjson.JSONObject;
import com.project.frame.commons.config.ExceptionHandlerAdvice;
import com.project.frame.utils.exception.BASE_RESPONSE_CODE_ENUM;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 自定义表单认证过滤器
 *
 * @author mxy
 * @date 2019/12/16
 */
public class AuthenticationFilter extends FormAuthenticationFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    // 加密密码参数
    private static final String DEFAULT_EN_PASSWORD_PARAM = "enPassword";

    // 默认的登录名称
    private static final String DEFAULT_USERNAME_PARAM = "loginName";

    // 默认验证码ID参数
    private static final String DEFAULT_CAPTCHA_ID_PARAM = "captchaId";

    // 默认验证码参数
    private static final String DEFAULT_CAPTCHA_PARAM = "captcha";

    private String captchaIdParam = DEFAULT_CAPTCHA_ID_PARAM;

    private String captchaParam = DEFAULT_CAPTCHA_PARAM;

    private String usernameParam = DEFAULT_USERNAME_PARAM;

    private String enPasswordParam = DEFAULT_EN_PASSWORD_PARAM;

    /**
     * 创建自定义登录令牌【token】
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        String loginName = getUsername(request);
        String password = getPassword(request);
        boolean isRememberMe = isRememberMe(request);
        String ip = getHost(request);
        return new com.project.frame.shiro.AuthenticationToken(loginName, password, isRememberMe, ip, "", "");
    }

    /**
     * 登录拒绝。增加Ajax异步处理
     *
     * @param servletRequest  请求对象
     * @param servletResponse 响应对象
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        // 判断是否为ajax异步请求
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 判断是否为登录请求
        if (this.isLoginRequest(servletRequest, response)) {
            if (this.isLoginSubmission(servletRequest, response)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Login submission detected.  Attempting to execute login.");
                }
                return executeLogin(servletRequest, response);
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("Login page view.");
                }
                return true;
            }
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Attempting to access a path which requires authentication. Forwarding to the Authentication url [" + this.getLoginUrl() + "]");
            }

            // 异步请求报错
            if (isAjaxReq(request)) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                PrintWriter out = response.getWriter();
                JSONObject json = new JSONObject();
                json.put(ExceptionHandlerAdvice.RESPONSE_CODE_NAME, BASE_RESPONSE_CODE_ENUM.NOT_LOGIN_ERROR.getCode());
                json.put(ExceptionHandlerAdvice.RESPONSE_MSG_NAME, BASE_RESPONSE_CODE_ENUM.NOT_LOGIN_ERROR.getMsg());
                out.println(json);
                out.flush();
                out.close();
                return false;
            }

            // 如果同步请求继续执行基类方法（当为同步方法的时候，基类会直接跳转登录页面）
            return super.onAccessDenied(request, response);
        }
    }

    /**
     * 重写登录成功的方法。如果为异步请求，直接返回成功响应
     *
     * @param token           Token令牌
     * @param subject         应用程序与shiro交互的核心对象
     * @param servletRequest  请求对象
     * @param servletResponse 响应对象
     */
    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        // 如果为异步请求，登录成功后，直接返回数据，前台跳转登录后的页面处理
        if (isAjaxReq(servletRequest)) {
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            JSONObject json = new JSONObject();
            json.put(ExceptionHandlerAdvice.RESPONSE_CODE_NAME, BASE_RESPONSE_CODE_ENUM.REQUEST_SUCCESS.getCode());
            json.put(ExceptionHandlerAdvice.RESPONSE_MSG_NAME, BASE_RESPONSE_CODE_ENUM.REQUEST_SUCCESS.getMsg());
            out.write(json.toJSONString());
            out.flush();
            out.close();
            return true;
        }
        return super.onLoginSuccess(token, subject, servletRequest, servletResponse);
    }

    /**
     * 重写登录失败的方法；如果为异步请求，直接返回失败响应
     *
     * @param token    Token令牌
     * @param e        异常信息
     * @param request  请求对象
     * @param response 响应对象
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        // 如果为异步登录，直接返回错误结果
        if (isAjaxReq(request)) {
            PrintWriter out = null;
            try {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                out = response.getWriter();
                JSONObject json = new JSONObject();
                if (e.equals("org.apache.shiro.authc.pam.UnsupportedTokenException")) {
                    // 验证码错误
                    json.put(ExceptionHandlerAdvice.RESPONSE_CODE_NAME, BASE_RESPONSE_CODE_ENUM.CAPTCHA_ERROR.getCode());
                    json.put(ExceptionHandlerAdvice.RESPONSE_MSG_NAME, BASE_RESPONSE_CODE_ENUM.CAPTCHA_ERROR.getMsg());
                } else if (e.equals("org.apache.shiro.authc.UnknownAccountException")) {
                    // 此账号不存在
                    json.put(ExceptionHandlerAdvice.RESPONSE_CODE_NAME, BASE_RESPONSE_CODE_ENUM.ACCOUNT_OR_PASSWORD_ERROR.getCode());
                    json.put(ExceptionHandlerAdvice.RESPONSE_MSG_NAME, BASE_RESPONSE_CODE_ENUM.ACCOUNT_OR_PASSWORD_ERROR.getMsg());
                } else if (e.equals("org.apache.shiro.authc.DisabledAccountException")) {
                    // 此账号已被禁用
                    json.put(ExceptionHandlerAdvice.RESPONSE_CODE_NAME, BASE_RESPONSE_CODE_ENUM.ACCOUNT_HAS_BEEN_DISABLED.getCode());
                    json.put(ExceptionHandlerAdvice.RESPONSE_MSG_NAME, BASE_RESPONSE_CODE_ENUM.ACCOUNT_HAS_BEEN_DISABLED.getMsg());
                } else if (e.equals("org.apache.shiro.authc.LockedAccountException")) {
                    // 此账号已被锁定
                    json.put(ExceptionHandlerAdvice.RESPONSE_CODE_NAME, BASE_RESPONSE_CODE_ENUM.ACCOUNT_IS_LOCKED.getCode());
                    json.put(ExceptionHandlerAdvice.RESPONSE_MSG_NAME, BASE_RESPONSE_CODE_ENUM.ACCOUNT_IS_LOCKED.getMsg());
                } else if (e.equals("org.apache.shiro.authc.IncorrectCredentialsException")) {
                    // 密码错误
                    json.put(ExceptionHandlerAdvice.RESPONSE_CODE_NAME, BASE_RESPONSE_CODE_ENUM.ACCOUNT_OR_PASSWORD_ERROR.getCode());
                    json.put(ExceptionHandlerAdvice.RESPONSE_MSG_NAME, BASE_RESPONSE_CODE_ENUM.ACCOUNT_OR_PASSWORD_ERROR.getMsg());
                } else if (e.equals("org.apache.shiro.authc.AuthenticationException")) {
                    // 账号认证失败
                    json.put(ExceptionHandlerAdvice.RESPONSE_CODE_NAME, BASE_RESPONSE_CODE_ENUM.ACCOUNT_AUTHENTICATION_FAILED.getCode());
                    json.put(ExceptionHandlerAdvice.RESPONSE_MSG_NAME, BASE_RESPONSE_CODE_ENUM.ACCOUNT_AUTHENTICATION_FAILED.getMsg());
                }
                out.write(json.toJSONString());
                out.flush();
                out.close();
                return false;
            } catch (IOException ex) {
                ex.printStackTrace();
                logger.error("\r\n ********* Shiro认证失败");
            }

        }
        return super.onLoginFailure(token, e, request, response);
    }

    /**
     * 请求是否被允许访问
     */
    @Override
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        // Always return true if the request's method is OPTIONSif (request instanceof HttpServletRequest)
        if (((HttpServletRequest) request).getMethod().toUpperCase().equals("OPTIONS")) {
            return true;
        }
        return super.isAccessAllowed(request, response, mappedValue);
    }

    /**
     * 获取密码
     *
     * @param servletRequest 请求对象
     * @return 密码参数
     */
    @Override
    protected String getPassword(ServletRequest servletRequest) {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        return request.getParameter(enPasswordParam);
    }

    /**
     * 判断是否为Ajax请求
     *
     * @param servletRequest 请求对象
     * @return true为ajax请求，false为非ajax请求
     */
    private boolean isAjaxReq(ServletRequest servletRequest) {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String requestType = request.getHeader("X-Requested-With");
        if (requestType != null && requestType.equalsIgnoreCase("XMLHttpRequest")) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public String getEnPasswordParam() {
        return enPasswordParam;
    }

    public void setEnPasswordParam(String enPasswordParam) {
        this.enPasswordParam = enPasswordParam;
    }

    public String getUsernameParam() {
        return usernameParam;
    }

    public String getCaptchaIdParam() {
        return captchaIdParam;
    }

    public void setCaptchaIdParam(String captchaIdParam) {
        this.captchaIdParam = captchaIdParam;
    }

    public String getCaptchaParam() {
        return captchaParam;
    }

    public void setCaptchaParam(String captchaParam) {
        this.captchaParam = captchaParam;
    }
}
