package com.project.frame.shiro;

import com.alibaba.fastjson.JSONObject;
import com.project.frame.commons.config.ExceptionHandlerAdvice;
import com.project.frame.utils.exception.BASE_RESPONSE_CODE_ENUM;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
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
     * Shiro认证perms资源失败后回调方法
     *
     * @param servletRequest  请求对象
     * @param servletResponse 响应对象
     * @return true继续往下执行。false该filter过滤器已经处理，不继续执行其他过滤器
     * @throws IOException IO异常
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        String requestedWith = httpServletRequest.getHeader("X-Requested-With");

        if (StringUtils.isNotEmpty(requestedWith) && StringUtils.equals(requestedWith, "XMLHttpRequest")) {
            // 如果是ajax返回指定格式数据
            httpServletResponse.setContentType("application/json");
            httpServletResponse.setCharacterEncoding("UTF-8");
            PrintWriter out = httpServletResponse.getWriter();
            JSONObject json = new JSONObject();
            json.put(ExceptionHandlerAdvice.RESPONSE_CODE_NAME, BASE_RESPONSE_CODE_ENUM.REQUEST_SUCCESS.getCode());
            json.put(ExceptionHandlerAdvice.RESPONSE_MSG_NAME, BASE_RESPONSE_CODE_ENUM.REQUEST_SUCCESS.getMsg());
            out.write(json.toJSONString());
            out.flush();
            out.close();
        } else {
            // 如果是普通请求进行重定向
            httpServletResponse.sendRedirect("/403");
        }
        return false;
    }
}
