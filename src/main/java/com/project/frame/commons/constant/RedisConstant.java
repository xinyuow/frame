package com.project.frame.commons.constant;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Redis常量配置
 *
 * @author mxy
 * @date 2019/12/19
 */
@Order(1)
@Component
public class RedisConstant implements InitializingBean {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.database}")
    private int databaseId;

    @Value("${spring.redis.timeout}")
    private int timeout;

    @Value("${spring.redis.jedis.pool.max-idle}")
    private int maxIdle;

    @Value("${spring.redis.jedis.pool.min-idle}")
    private int minIdle;

    @Value("${spring.redis.jedis.pool.max-active}")
    private int maxActive;

    @Value("${spring.redis.jedis.pool.max-wait}")
    private long maxWait;

    /**
     * Redis所在主机IP
     */
    public static String HOST;

    /**
     * Redis端口号
     */
    public static int PORT;

    /**
     * Redis密码
     */
    public static String PASSWORD;

    /**
     * 存储数据库
     */
    public static int DATABASE_ID;

    /**
     * 超时时间
     */
    public static int TIMEOUT;

    /**
     * 连接池中的最大空闲连接
     */
    public static int MAX_IDLE;

    /**
     * 连接池中的最小空闲连接
     */
    public static int MIN_IDLE;

    /**
     * 连接池最大连接数
     */
    public static int MAX_ACTIVE;

    /**
     * 连接池最大阻塞等待时间
     */
    public static long MAX_WAIT;

    @Override
    public void afterPropertiesSet() throws Exception {
        HOST = host;
        PORT = port;
        PASSWORD = password;
        DATABASE_ID = databaseId;
        TIMEOUT = timeout;
        MAX_IDLE = maxIdle;
        MIN_IDLE = minIdle;
        MAX_ACTIVE = maxActive;
        MAX_WAIT = maxWait;
    }
}
