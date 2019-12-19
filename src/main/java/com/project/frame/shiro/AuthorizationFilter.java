package com.project.frame.shiro;

import com.alibaba.fastjson.JSONObject;
import com.project.frame.commons.config.ExceptionHandlerAdvice;
import com.project.frame.utils.exception.BASE_RESPONSE_CODE_ENUM;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 自定义授权过滤器
 *
 * @author mxy
 * @date 2019/12/16
 */
public class AuthorizationFilter extends PermissionsAuthorizationFilter {

    /**
     * 拒绝访问
     *
     * @param servletRequest  请求对象
     * @param servletResponse 响应对象
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        if (null == SecurityUtils.getSubject().getPrincipals()) {
            // session过期，需要重新登录
            httpServletResponse.setContentType("application/json");
            httpServletResponse.setCharacterEncoding("UTF-8");
            PrintWriter out = httpServletResponse.getWriter();
            JSONObject json = new JSONObject();
            json.put(ExceptionHandlerAdvice.RESPONSE_CODE_NAME, BASE_RESPONSE_CODE_ENUM.NOT_LOGIN_ERROR.getCode());
            json.put(ExceptionHandlerAdvice.RESPONSE_MSG_NAME, BASE_RESPONSE_CODE_ENUM.NOT_LOGIN_ERROR.getMsg());
            out.write(json.toJSONString());
            out.flush();
            out.close();
            return false;
        } else {
            // 权限不足
            httpServletResponse.setContentType("application/json");
            httpServletResponse.setCharacterEncoding("UTF-8");
            PrintWriter out = httpServletResponse.getWriter();
            JSONObject json = new JSONObject();
            json.put(ExceptionHandlerAdvice.RESPONSE_CODE_NAME, BASE_RESPONSE_CODE_ENUM.UNAUTHORIZED.getCode());
            json.put(ExceptionHandlerAdvice.RESPONSE_MSG_NAME, BASE_RESPONSE_CODE_ENUM.UNAUTHORIZED.getMsg());
            out.write(json.toJSONString());
            out.flush();
            out.close();
            return false;
        }
    }
}
