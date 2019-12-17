package com.project.frame.commons.config;

import com.project.frame.shiro.AuthenticationFilter;
import com.project.frame.shiro.AuthenticationRealm;
import com.project.frame.shiro.AuthorizationFilter;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
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
     * <p>
     * 定义拦截URL权限，优先级从上到下
     * 1). anon  : 匿名访问，无需登录
     * 2). authc : 登录后才能访问
     * 3). logout: 登出
     * 4). perms : 自定义的过滤器
     * <p>
     * URL 匹配风格
     * 1). ?：匹配一个字符，如 /admin? 将匹配 /admin1，但不匹配 /admin 或 /admin/；
     * 2). *：匹配零个或多个字符串，如 /admin* 将匹配 /admin 或/admin123，但不匹配 /admin/1；
     * 3). **：匹配路径中的零个或多个路径，如 /admin/** 将匹配 /admin/a 或 /admin/a/b
     * <p>
     * 配置身份验证成功，失败的跳转路径
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager) {
        logger.info("\r\n ********* 进入Shiro过滤器");

        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // 设置securityManager
        shiroFilterFactoryBean.setSecurityManager(securityManager);

        // 自定义的过滤器
        Map<String, Filter> filterMap = new HashMap<>();
        // map里面key值要为过滤器的名称，value为过滤器对象
        filterMap.put("authc", authenticationFilter());
        filterMap.put("perms", authorizationFilter());
        // 将自定义的过滤器加入到过滤器集合中
        shiroFilterFactoryBean.setFilters(filterMap);

        // 设置拦截器有序集合
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
        filterChainDefinitionMap.put("/admin/login", "anon");           // 登录请求
        filterChainDefinitionMap.put("/admin/logout", "logout");        // 用户退出，只需配置logout即可实现该功能
        filterChainDefinitionMap.put("/api/v1/anon/**", "anon");        // 无权限请求
        filterChainDefinitionMap.put("/api/v1/auth/**", "authc,perms"); // 其他路径均需要身份认证，一般位于最下面，优先级最低

        // 设置拦截器
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);

        // 设置登录页面地址，此处仅返回Json数据。
        // shiroFilterFactoryBean.setLoginUrl("/admin/login");
        // 设置验证失败后跳转的路径，此处仅返回Json数据。
        // shiroFilterFactoryBean.setUnauthorizedUrl("/admin/common/unauthorized.jhtml");
        logger.info("\r\n ********* Shiro过滤器配置完成");
        return shiroFilterFactoryBean;
    }

    /**
     * 开启Shiro的AOP注解支持
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor() {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        //设置安全管理器
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager());
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
     * 自定义认证过滤器
     */
    @Bean
    public AuthenticationFilter authenticationFilter() {
        return new AuthenticationFilter();
    }

    /**
     * 自定义授权过滤器
     */
    @Bean
    public AuthorizationFilter authorizationFilter() {
        return new AuthorizationFilter();
    }

}
