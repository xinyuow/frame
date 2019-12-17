package com.project.frame.shiro;

import java.io.Serializable;

/**
 * 自定义主体 - 认证和授权时使用
 * 包括用户ID和登录名称
 *
 * @author mxy
 * @date 2019/12/16
 */
public class Principal implements Serializable {

    private static final long serialVersionUID = 598764316789461315L;

    public Long id;

    public String loginName;

    public Principal(Long id, String loginName) {
        this.id = id;
        this.loginName = loginName;
    }

    public Principal() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

}
