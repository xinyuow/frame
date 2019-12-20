package com.project.frame.commons.config;

import com.project.frame.commons.constant.RedisConstant;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis配置类
 *
 * @author mxy
 * @date 2019/12/19
 */
@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {

    /**
     * 获取Jedis连接池
     */
    @Bean
    public JedisPool jedisPool() {
        return new JedisPool(jedisPoolConfig(), RedisConstant.HOST, RedisConstant.PORT,
                RedisConstant.TIMEOUT, RedisConstant.PASSWORD);
    }

    /**
     * 获取Jedis连接池配置信息
     */
    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(RedisConstant.MAX_IDLE);         // 最大空闲连接
        jedisPoolConfig.setMinIdle(RedisConstant.MIN_IDLE);         // 最小空闲连接
        jedisPoolConfig.setMaxTotal(RedisConstant.MAX_ACTIVE);      // 最大连接数
        jedisPoolConfig.setMaxWaitMillis(RedisConstant.MAX_WAIT);   // 最大阻塞时间
        // 连接耗尽时是否阻塞, false报异常, true阻塞直到超时, 默认true
        jedisPoolConfig.setBlockWhenExhausted(true);
        // 是否启用pool的jmx管理功能, 默认true
        jedisPoolConfig.setJmxEnabled(true);
        return jedisPoolConfig;
    }
}
