package com.project.frame.commons.config;

import com.project.frame.shiro.*;
import com.project.frame.shiro.redis.ShiroRedisCacheManager;
import com.project.frame.shiro.redis.ShiroRedisSessionDAO;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shiro配置类
 *
 * @author mxy
 * @date 2019/12/16
 */
@Configuration
public class ShiroConfig {
    private static final transient Logger logger = LoggerFactory.getLogger(ShiroConfig.class);

    /**
     * SecurityManager 安全管理器 - Shiro的核心
     */
    @Bean
    public DefaultWebSecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 自定义的认证Realm
        securityManager.setRealm(authenticationShiroRealm());
        // 不设置【记住我】
        securityManager.setRememberMeManager(null);
        // 会话管理
        securityManager.setSessionManager(sessionManager());
        // 缓存管理
        securityManager.setCacheManager(shiroRedisCacheManager());
        return securityManager;
    }

    /**
     * 认证Realm - 自定义的Realm，可以多个
     */
    @Bean
    public AuthenticationRealm authenticationShiroRealm() {
        return new AuthenticationRealm();
    }

    /**
     * 配置Shiro的Web过滤器，拦截浏览器请求并交给SecurityManager处理
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager) {
        logger.info("\r\n ********* Shiro过滤器配置开始 *********");

        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // 设置securityManager
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // 自定义的过滤器
        Map<String, Filter> filterMap = new HashMap<>();
        // map里面key值要为过滤器的名称，value为过滤器对象
        filterMap.put("authc", new AuthenticationFilter());
        filterMap.put("perms", new AuthorizationFilter());
        // 将自定义的过滤器加入到过滤器集合中
        shiroFilterFactoryBean.setFilters(filterMap);

        // 设置拦截器有序集合
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        filterChainDefinitionMap.put("/admin/login", "anon");
        filterChainDefinitionMap.put("/admin/**", "anon");
        filterChainDefinitionMap.put("/api/v1/anon/**", "anon");
        filterChainDefinitionMap.put("/static/**", "anon"); // 放行静态资源，这里有一个坑。只配置这里是无效的。需要在 WebMvcConfiguration 类中的 addResourceHandlers() 中再配置一下
        filterChainDefinitionMap.put("/admin/logout", "logout");
        filterChainDefinitionMap.put("/**", "authc,perms");
        // 设置拦截器
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);

        // 设置登录请求。此处如果不设置则使用默认的/login.jsp。
        shiroFilterFactoryBean.setLoginUrl("/admin/login");
        // 设置权限不足的请求路径。
//        shiroFilterFactoryBean.setUnauthorizedUrl("/admin/unauthorized");

        logger.info("\r\n ********* Shiro过滤器配置完成 *********");
        return shiroFilterFactoryBean;
    }

    /**
     * 开启Shiro的AOP注解支持
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(org.apache.shiro.mgt.SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        // 设置安全管理器
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    /**
     * 自动创建代理类，若不添加，Shiro的注解可能不会生效。
     * 扫描上下文，获取所有的Advisor(通知器)。并将这些Advisor应用到所有符合切入点的Bean中。
     *
     * @DependsOn 保证创建DefaultAdvisorAutoProxyCreator 之前先创建 LifecycleBeanPostProcessor
     */
    @Bean
    @DependsOn({"lifecycleBeanPostProcessor"})
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        advisorAutoProxyCreator.setProxyTargetClass(true);
        return advisorAutoProxyCreator;
    }

    /**
     * 配置Shiro生命周期处理器
     */
    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    /**
     * redis缓存管理
     */
    @Bean
    @DependsOn("redisConstant")
    public ShiroRedisCacheManager shiroRedisCacheManager() {
        return new ShiroRedisCacheManager();
    }

    /**
     * 设置session会话管理者
     */
    @Bean
    public SessionManager sessionManager() {
        FrameSessionManager sessionManager = new FrameSessionManager();
        sessionManager.setSessionIdCookie(simpleCookie());  // 指定sessionID
        sessionManager.setSessionDAO(shiroRedisSessionDAO());
        sessionManager.setCacheManager(shiroRedisCacheManager());
        sessionManager.setSessionIdUrlRewritingEnabled(false);  // 去除登录时携带的sessionID
        return sessionManager;
    }

    /**
     * session管理
     * 作用是为session提供CRUD并存入Redis中的一个Shiro组件
     */
    @Bean
    public ShiroRedisSessionDAO shiroRedisSessionDAO() {
        return new ShiroRedisSessionDAO();
    }

    /**
     * 这里需要设置一个cookie的名称  原因就是会跟原来的session的id值重复的
     */
    @Bean
    public SimpleCookie simpleCookie() {
        SimpleCookie simpleCookie = new SimpleCookie();
        simpleCookie.setHttpOnly(true);
        simpleCookie.setName("SHARE_JSESSIONID");
        return simpleCookie;
    }
}
