package com.project.frame.shiro.redis;

import com.project.frame.commons.constant.RedisConstant;
import com.project.frame.utils.RedisClient;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ShiroRedisCacheManager
 * 此类由ShiroConfig注入到Spring
 *
 * @author mxy
 * @date 2019/12/20
 */
public class ShiroRedisCacheManager implements CacheManager {
    private static final Logger logger = LoggerFactory.getLogger(ShiroRedisCacheManager.class);

    private final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap<String, Cache>();

    // 注入redisClient实例
    @Resource(name = "redisClient")
    private RedisClient redisClient;

    /**
     * 获取权限缓存
     */
    @Override
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {
        logger.debug("\r\n ********* 获取名称为:【{}】的RedisCache实例", name);
        Cache cache = caches.get(RedisConstant.ADMIN_SHIRO_REALM_KEY + name);
        if (cache == null) {
            cache = new ShiroRedisCache<K, V>(redisClient, RedisConstant.ADMIN_SHIRO_REALM_KEY);
            caches.put(RedisConstant.ADMIN_SHIRO_REALM_KEY + name, cache);
        }
        return cache;
    }
}
