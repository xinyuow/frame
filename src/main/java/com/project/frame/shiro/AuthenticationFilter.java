package com.project.frame.shiro;

import com.alibaba.fastjson.JSONObject;
import com.project.frame.commons.config.ExceptionHandlerAdvice;
import com.project.frame.utils.exception.BASE_RESPONSE_CODE_ENUM;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * 自定义认证过滤器
 *
 * @author mxy
 * @date 2019/12/16
 */
@Data
@EqualsAndHashCode(callSuper = true)
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
     * 如果是OPTIONS预请求，则无条件放行。其他请求正常走。
     */
    @Override
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if (((HttpServletRequest) request).getMethod().toUpperCase().equals("OPTIONS")) {
            return true;
        }
        return super.isAccessAllowed(request, response, mappedValue);
    }

    /**
     * 拒绝访问，判断是否为登录请求
     *
     * @param servletRequest  请求对象
     * @param servletResponse 响应对象
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        // 判断是否为登录请求
        if (this.isLoginRequest(servletRequest, response)) {
            return executeLogin(servletRequest, response);
        } else {
            // 非登录请求则返回登录失败
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
    }

    /**
     * 重写登录成功方法，直接返回成功响应
     * 如果不重写该方法，则默认会重定向到页面
     * <p>
     * 不用重写登录失败方法，原因是 AuthenticationRealm 类中的
     * doGetAuthenticationInfo() 抛出的所有异常均在 LoginController 类中的
     * login() 中处理了。
     *
     * @param token           Token令牌
     * @param subject         主体
     * @param servletRequest  请求对象
     * @param servletResponse 响应对象
     */
    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JSONObject json = new JSONObject();
        json.put(ExceptionHandlerAdvice.RESPONSE_CODE_NAME, BASE_RESPONSE_CODE_ENUM.LOGIN_SUCCESS.getCode());
        json.put(ExceptionHandlerAdvice.RESPONSE_MSG_NAME, BASE_RESPONSE_CODE_ENUM.LOGIN_SUCCESS.getMsg());
        out.println(json);
        out.flush();
        out.close();
        return true;
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
}
