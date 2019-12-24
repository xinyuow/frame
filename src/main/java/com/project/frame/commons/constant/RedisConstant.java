package com.project.frame.commons.constant;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Redis缓存常量配置
 *
 * @author mxy
 * @date 2019/12/20
 */
@Component
public class RedisConstant implements InitializingBean {

    @Value("${frame.redis.admin.shiro.session.key.name}")
    private String adminShiroSessionKey;

    @Value("${frame.redis.admin.shiro.session.expire}")
    private int adminShiroSessionExpire;

    @Value("${frame.redis.admin.shiro.redis.realm.key.name}")
    private String adminShiroRealmKey;

    @Value("${frame.redis.admin.shiro.redis.realm.expire}")
    private int adminShiroRealmExpire;

    /**
     * ShiroSession的redis-key
     */
    public static String ADMIN_SHIRO_SESSION_KEY;

    /**
     * ShiroSession的过期时间，单位：秒
     */
    public static int ADMIN_SHIRO_SESSION_EXPIRE;

    /**
     * ShiroRealm的redis-key
     */
    public static String ADMIN_SHIRO_REALM_KEY;

    /**
     * ShiroRealm的超时时间，单位：秒
     */
    public static int ADMIN_SHIRO_REALM_EXPIRE;

    @Override
    public void afterPropertiesSet() throws Exception {
        ADMIN_SHIRO_SESSION_KEY = adminShiroSessionKey;
        ADMIN_SHIRO_SESSION_EXPIRE = adminShiroSessionExpire;
        ADMIN_SHIRO_REALM_KEY = adminShiroRealmKey;
        ADMIN_SHIRO_REALM_EXPIRE = adminShiroRealmExpire;
    }
}
