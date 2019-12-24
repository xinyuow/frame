package com.project.frame.shiro.redis;

import com.project.frame.commons.constant.RedisConstant;
import com.project.frame.utils.ByteUtil;
import com.project.frame.utils.RedisClient;
import lombok.Data;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * ShiroRedisCache
 * 此类由ShiroConfig注入到Spring
 *
 * @author mxy
 * @date 2019/12/20
 */
@Data
public class ShiroRedisCache<K, V> implements Cache<K, V> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // redis客户端
    private RedisClient redisClient;

    // shiroSession的key值前缀
    private String keyPrefix;

    // 通过redisClient实例和prefix参数构造redisCache
    public ShiroRedisCache(RedisClient redisClient, String prefix) {
        if (redisClient == null) {
            throw new IllegalArgumentException("shiroRedisCahe初始化时，redisClient参数不能为空");
        }
        this.redisClient = redisClient;
        this.keyPrefix = prefix;
    }

    /**
     * 获得String类型的key
     */
    private String getPreStringKey(K key) {
        String preKey = null;
        if (key instanceof String) {
            preKey = this.keyPrefix + key;
            return preKey;
        } else {
            try {
                preKey = keyPrefix + ByteUtil.bytesToHexString(ByteUtil.objectToBytes(key));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return preKey;
        }
    }

    /**
     * 从Redis中根据指定key获取value
     */
    @Override
    public V get(K key) throws CacheException {
        logger.debug("\r\n ********* 根据key获取key:【{}】对应的对象*********", key);
        try {
            if (key == null) {
                return null;
            } else {
                V value = (V) redisClient.get(getPreStringKey(key));
                if (value == null) {
                    return null;
                }
                return value;
            }
        } catch (Throwable t) {
            throw new CacheException(t);
        }

    }

    /**
     * 向Redis中存储指定数据
     * 此处设定权限存储在Redis的超时时间
     */
    @Override
    public V put(K key, V value) throws CacheException {
        logger.debug("\r\n ********* 根据key存储key:【{}】，value:【{}】*********", key, value);
        try {
            redisClient.set(getPreStringKey(key), value);
            redisClient.setAndExpire(getPreStringKey(key), value, RedisConstant.ADMIN_SHIRO_REALM_EXPIRE);
            return value;
        } catch (Throwable t) {
            throw new CacheException(t);
        }
    }

    /**
     * 从Redis中移除指定数据
     */
    @Override
    public V remove(K key) throws CacheException {
        logger.debug("\r\n ********* 从redis中删除key:【{}】*********", key);
        try {
            V previous = get(key);
            redisClient.del(getPreStringKey(key));
            return previous;
        } catch (Throwable t) {
            throw new CacheException(t);
        }
    }

    /**
     * 从Redis中删除所有元素
     */
    @Override
    public void clear() throws CacheException {
        logger.debug("\r\n ********* 从redis中删除所有元素 *********");
        try {
            redisClient.flushAll();
        } catch (Throwable t) {
            throw new CacheException(t);
        }
    }

    /**
     * 从Redis中获取所有数据量
     */
    @Override
    public int size() {
        logger.debug("\r\n ********* 从redis中获取所有数据量 *********");
        try {
            Long longSize = new Long(redisClient.dbSize());
            return longSize.intValue();
        } catch (Throwable t) {
            throw new CacheException(t);
        }
    }

    /**
     * 从Redis中获取所有key集合
     */
    @Override
    public Set<K> keys() {
        logger.debug("\r\n ********* 从redis中获取所有key集合 *********");
        try {
            Set<byte[]> keys = redisClient.keys(ByteUtil.objectToBytes(this.keyPrefix + "*"));
            if (CollectionUtils.isEmpty(keys)) {
                return Collections.emptySet();
            } else {
                Set<K> newKeys = new HashSet<K>();
                for (byte[] key : keys) {
                    newKeys.add((K) key);
                }
                return newKeys;
            }
        } catch (Throwable t) {
            throw new CacheException(t);
        }
    }

    /**
     * 从Redis中获取所有value集合
     */
    @Override
    public Collection<V> values() {
        logger.debug("\r\n ********* 从redis中获取所有value集合 *********");
        try {
            Set<byte[]> keys = redisClient.keys(ByteUtil.objectToBytes(this.keyPrefix + "*"));
            if (!CollectionUtils.isEmpty(keys)) {
                List<V> values = new ArrayList<V>(keys.size());
                for (byte[] key : keys) {
                    @SuppressWarnings("unchecked")
                    V value = get((K) key);
                    if (value != null) {
                        values.add(value);
                    }
                }
                return Collections.unmodifiableList(values);
            } else {
                return Collections.emptyList();
            }
        } catch (Throwable t) {
            throw new CacheException(t);
        }
    }
}
