package com.project.frame.utils.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.project.frame.commons.constant.RedisConstant;
import com.project.frame.utils.ByteUtil;
import com.project.frame.utils.JacksonUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.io.IOException;
import java.util.*;

/**
 * Redis客户端工具类
 */
@Component
public class RedisClient {
    private Logger logger = LoggerFactory.getLogger(RedisClient.class);

    @Autowired
    private volatile JedisPool jedisPool;

    @Autowired
    private JedisPoolConfig jedisPoolConfig;

    public static final String NOT_FOUND = "nil";

    private Object lock = new Object();

    // 重试次数
    private int resetNum = 3;

    /**
     * 获取Jedis对象
     *
     * @return Jedis对象
     */
    public Jedis getJedis() {
        return getJedis(RedisConstant.DATABASE_ID);
    }

    /**
     * 获取Jedis对象
     *
     * @param dbId 数据库ID
     * @return Jedis对象
     */
    public Jedis getJedis(int dbId) {
        Jedis jedis = null;
        int tryCount = 0;
        while (jedis == null && tryCount < resetNum) {
            tryCount++;
            try {
                if (checkConnectionPool() != null) {
                    jedis = jedisPool.getResource();
                    jedis.select(dbId);
                }
            } catch (Exception e) {
                jedisPool = null;
                logger.error("\r\n ********* 获取Jedis对象失败" + ExceptionUtils.getFullStackTrace(e));
            }
        }
        return jedis;
    }

    /**
     * 校验JedisPool池连接
     *
     * @return JedisPool
     */
    public JedisPool checkConnectionPool() {
        if (jedisPool == null) {
            synchronized (lock) {
                if (jedisPool == null) {
                    try {
                        jedisPool = new JedisPool(jedisPoolConfig, RedisConstant.HOST, RedisConstant.PORT,
                                RedisConstant.TIMEOUT, RedisConstant.PASSWORD);
                    } catch (Exception e) {
                        logger.error("\r\n ********* [校验JedisPoll连接池失败]" + ExceptionUtils.getFullStackTrace(e));
                    }
                }
            }
        }
        return jedisPool;
    }

    /**
     * 释放Jedis实例
     *
     * @param jedis
     */
    public void releaseJedisInstance(Jedis jedis) {
        try {
            if (jedis != null) {
                jedis.close();
            }
        } catch (Exception e) {
            logger.error("\r\n ********* [释放Jedis实例失败]" + ExceptionUtils.getFullStackTrace(e));
        }

    }

    /**
     * 存储byte类型的KEY和VALUE
     *
     * @param key
     * @param value
     * @return boolean
     */
    public boolean set(Object key, Object value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.set(ByteUtil.objectToBytes(key), ByteUtil.objectToBytes(value));
            return true;
        } catch (Exception e) {
            logger.error("\r\n ********* [Redis数据存储失败]" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return false;
    }

    /**
     * 设置byte类型的KEY和VALUE的有效时间
     *
     * @param key
     * @param value
     * @param expire
     * @return boolean
     */
    public boolean setAndExpire(Object key, Object value, int expire) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.setex(ByteUtil.objectToBytes(key), expire, ByteUtil.objectToBytes(value));
            return true;
        } catch (Exception e) {
            logger.error("\r\n ********* [设置byte类型的KEY有效时间出错]" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return false;
    }

    /**
     * 获取byte类型KEY的value值
     *
     * @param key
     * @return object
     */
    public Object get(Object key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            byte[] obj = jedis.get(ByteUtil.objectToBytes(key));
            return obj == null ? null : ByteUtil.bytesToObject(obj);
        } catch (Exception e) {
            logger.error("\r\n ********* [获取byte类型key的value值出错]" + ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * 根据表达式获取keys
     *
     * @param key
     */
    public Set<byte[]> keys(Object key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.keys(ByteUtil.objectToBytes(key));
        } catch (Exception e) {
            logger.error("\r\n ********* [获取byte类型key的value值出错]" + ExceptionUtils.getFullStackTrace(e));
            return new HashSet<byte[]>();
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * 设置byte类型的KEY缓存有效时间
     *
     * @param key
     * @param expire
     * @return boolean
     */
    public boolean expire(Object key, int expire) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.expire(ByteUtil.objectToBytes(key), expire);
            return true;
        } catch (Exception e) {
            logger.error("\r\n ********* [设置byte类型的KEY缓存有效时间失败]" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return false;
    }

    /**
     * 删除byte类型的key
     *
     * @param key
     * @return boolean
     */
    public boolean del(Object key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.del(ByteUtil.objectToBytes(key));
            return true;
        } catch (Exception e) {
            logger.error("\r\n ********* [删除byte类型的key失败]" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return false;
    }

    /**
     * 删除字节数组key
     *
     * @param keys
     * @return
     */
    public boolean del(Object... keys) {
        if (keys == null || keys.length == 0) {
            return true;
        }
        Jedis jedis = null;
        try {
            jedis = getJedis();
            byte[] keysByte = new byte[keys.length];
            for (int i = 0; i < keys.length; i++) {
                keysByte = ByteUtil.objectToBytes(keys[i]);
            }
            jedis.del(keysByte);
            return true;
        } catch (Exception e) {
            logger.error("\r\n ********* [删除byte类型的KEY失败]" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return false;
    }

    /**
     * 查询字节类型KEY是否存在
     *
     * @param key
     * @return boolean
     */
    public boolean exists(Object key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.exists(ByteUtil.objectToBytes(key));
        } catch (IOException e) {
            logger.error("\r\n ********* [判断缓存是否存在出错]" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return false;
    }

    /**
     * redis存储String类型的KEY，JSON类型的Value
     *
     * @param key
     * @param o
     */
    public void setStringForObject(String key, Object o) {
        Jedis jedis = null;
        try {
            String s = JacksonUtil.toJsonStr(o);
            jedis = getJedis();
            jedis.set(key, s);
        } catch (Exception e) {
            logger.error("\r\n ********* [存储String类型的KEY，JSON类型的Value失败]" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * 存储String类型的KEY，String类型的Value
     *
     * @param key
     * @param value
     * @return 返回存储成功条数
     */
    public String setString(String key, String value) {
        String result = "";
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.set(key, value);
        } catch (Exception e) {
            logger.error("\r\n ********* [存储String类型的KEY，String类型的Value失败]" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }

    /**
     * 设置String类型的KEY，JSON类型的VALUE的 有效时间
     *
     * @param key
     * @param o
     * @param expire
     */
    /*public void setAndExpire(String key, Object o, int expire) {
        Jedis jedis = null;
        try {
            String s = JacksonUtil.toJsonStr(o);
            jedis = getJedis();
            jedis.set(key, s);
            jedis.expire(key, expire);
        } catch (Exception e) {
            logger.error("[redis存储失败]" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
    }*/

    /**
     * 设置String类型的KEY，String类型的VALUE
     *
     * @param key    String类型的key
     * @param v      String类型的value
     * @param expire 超时时间
     */
    public void setAndExpireString(String key, String v, int expire) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.setex(key, expire, v);
        } catch (Exception e) {
            logger.error("\r\n ********* [设置String类型的KEY，String类型的VALUE]" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * 获取string类型key的锁
     *
     * @param key
     * @param value
     * @return
     */
    public long setnx(String key, String value) {
        Jedis jedis = null;

        long result = -1;

        try {
            jedis = getJedis();
            result = jedis.setnx(key, value);
        } catch (Exception e) {
            logger.error("\r\n ********* [Redis setnx 调用失败]" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }

    /**
     * redis缓存有效时间数据
     *
     * @param key
     * @param value
     * @param expire
     * @return
     */
    public long setnxAndExpireString(String key, String value, int expire) {
        Jedis jedis = null;
        long result = -1;

        try {
            jedis = getJedis();
            result = jedis.setnx(key, value);
            if (result == 1) {
                jedis.expire(key, expire);
            }
        } catch (Exception e) {
            logger.error("\r\n ********* [Redis缓存有效时间数据失败]" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }

    /**
     * 根据KEY获取特定类型的缓存值
     *
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T get(String key, TypeReference<T> clazz) {
        String json = null;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            json = jedis.get(key);
        } catch (Exception e) {
            logger.error("\r\n ********* [根据KEY获取特定类型的缓存值]" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        if (json == null) {
            return null;
        } else {
            return JacksonUtil.readValue(json, clazz);
        }
    }

    /**
     * 根据KEY获取指定类型的缓存值
     *
     * @param key
     * @param clazz
     * @param <T>
     * @return VALUE
     */
    public <T> T get(String key, Class<T> clazz) {
        String json = null;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            json = jedis.get(key);
        } catch (Exception e) {
            logger.error("\r\n ********* [根据KEY获取指定类型的缓存值失败]" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        if (json == null) {
            return null;
        } else {
            return JacksonUtil.readValue(json, clazz);
        }
    }

    /**
     * @param key
     * @return
     */
    public String getString(String key) {
        String result = null;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            String str = jedis.get(key);
            if (!NOT_FOUND.equals(str)) {
                result = str;
            }
        } catch (Exception e) {
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }

    /**
     * @param key
     * @return
     */
    public byte[] get(byte[] key) {
        Jedis jedis = null;
        byte[] o = null;

        try {
            jedis = getJedis();
            o = jedis.get(key);

        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return o;
    }

    /**
     * @param pattern
     * @return
     */
    public List<String> keysString(String pattern) {
        List<String> result = new ArrayList<String>();
        Set<String> set = new HashSet<String>();
        Jedis jedis = null;
        try {
            jedis = getJedis();
            set = jedis.keys(pattern);
            if (set != null && !set.isEmpty()) {
                final Iterator<String> ite = set.iterator();
                while (ite.hasNext()) {
                    result.add((String) ite.next());
                }
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }

    /**
     * @param key
     * @param seconds
     * @return
     */
    public Long expireString(String key, int seconds) {
        Long result = 0L;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.expire(key, seconds);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }

    /**
     * @param keys
     * @return
     */
    public Long delStrings(String... keys) {
        Long result = 0L;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.del(keys);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }

    /**
     * @param key
     * @return
     */
    public boolean existsString(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.exists(key);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return false;
    }


    /**
     * @param key
     * @param o
     */
    public void setByte(byte[] key, byte[] o) {
        Jedis jedis = null;

        try {
            jedis = getJedis();
            jedis.set(key, o);

        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * @param key
     * @param s
     * @param expire
     */
    public void setAndExpireByte(byte[] key, byte[] s, int expire) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.setex(key, expire, s);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * @param key
     * @return
     */
    public byte[] getByte(byte[] key) {
        Jedis jedis = null;
        byte[] o = null;

        try {
            jedis = getJedis();
            o = jedis.get(key);

        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return o;
    }


    /**
     * logger.error(ExceptionUtils.getFullStackTrace(e));
     *
     * @param key
     * @return
     */
    public Long delByte(byte[] key) {
        Long result = 0L;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.del(key);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }

    public Long delBytes(byte[]... keys) {
        Long result = 0L;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.del(keys);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }


    /**
     * @param keys
     * @return
     */
    public List<String> mget(final String... keys) {
        List<String> result = null;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.mget(keys);
            if (result != null && !result.isEmpty()) {
                result.remove(NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {

            releaseJedisInstance(jedis);
        }
        return result;
    }

    /**
     * @param key
     * @return
     */
    public Long hlen(String key) {
        Long len = 0L;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            len = jedis.hlen(key);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return len;
    }

    /**
     * @param timeout
     * @param key
     * @return
     */
    public List<String> blpop(int timeout, String... key) {
        Jedis jedis = null;
        List<String> result = null;
        try {
            jedis = getJedis();
            result = jedis.blpop(timeout, key);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }


    /**
     * @param key
     * @param field
     * @param value
     * @return
     */
    public Long hset(String key, String field, String value) {
        Long result = 0L;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.hset(key, field, value);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }


    /**
     * @param key
     * @param field
     * @param o
     * @return
     */
    public Long hset(String key, String field, Object o) {
        Long result = 0L;
        Jedis jedis = null;
        try {
            String s = JacksonUtil.toJsonStr(o);
            jedis = getJedis();
            result = jedis.hset(key, field, s);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }


    /**
     * @param key
     * @param field
     * @param o
     * @return
     */
    public Long hsetnx(String key, String field, Object o) {
        Long result = -1L;
        Jedis jedis = null;
        try {
            String s = JacksonUtil.toJsonStr(o);
            jedis = getJedis();
            result = jedis.hsetnx(key, field, s);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }

    public String hget(String key, String field) {
        String result = null;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            String str = jedis.hget(key, field);
            if (!NOT_FOUND.equals(str)) {
                result = str;
            }
        } catch (Exception e) {

            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }


    /**
     * @param key
     * @param field
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T hget(String key, String field, Class<T> clazz) {
        String result = null;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            String str = jedis.hget(key, field);
            if (!NOT_FOUND.equals(str)) {
                result = str;
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }

        return result == null ? null : JacksonUtil.readValue(result, clazz);
    }


    /**
     * @param key
     * @param field
     * @param tr
     * @param <T>
     * @return
     */
    public <T> T hget(String key, String field, TypeReference<T> tr) {
        String result = null;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            String str = jedis.hget(key, field);
            if (!NOT_FOUND.equals(str)) {
                result = str;
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }

        return result == null ? null : JacksonUtil.readValue(result, tr);
    }


    /**
     * @param key
     * @param hash
     * @return
     */
    public String hmset(String key, Map<String, String> hash) {
        String result = null;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.hmset(key, hash);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }


    /**
     * @param key
     * @param fields
     * @return
     */
    public List<String> hmget(final String key, final String... fields) {
        List<String> result = null;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.hmget(key, fields);
            if (result != null && !result.isEmpty()) {
                result.remove(NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }


    /**
     * @param key
     * @param clazz
     * @param fields
     * @param <T>
     * @return
     */
    public <T> List<T> hmget(final String key, final Class<T> clazz, final String... fields) {
        List<String> jsons = null;
        List<T> results = null;
        Jedis jedis = null;

        try {
            jedis = getJedis();

            if (fields.length > 0) {
                results = new ArrayList<T>();
            }

            jsons = jedis.hmget(key, fields);
            if (jsons != null && !jsons.isEmpty()) {
                jsons.remove(NOT_FOUND);
            }

            for (String json : jsons) {
                if (json != null) {
                    results.add(JacksonUtil.readValue(json, clazz));
                }
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }

        return results;
    }


    /**
     * @param key
     * @return
     */
    public Map<String, String> hgetAll(String key) {
        Map<String, String> result = new HashMap<String, String>();
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.hgetAll(key);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }


    /**
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> Map<String, T> hgetAll(String key, final Class<T> clazz) {
        Map<String, T> result = null;
        Map<String, String> jsonsMaps = new HashMap<String, String>();
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jsonsMaps = jedis.hgetAll(key);

            if (jsonsMaps != null) {
                result = new HashMap<String, T>();

                Set<String> jsonKeys = jsonsMaps.keySet();
                for (String jsonKey : jsonKeys) {
                    String jsonValue = (String) jsonsMaps.get(jsonKey);

                    if (jsonValue != null) {
                        result.put(jsonKey, JacksonUtil.readValue(jsonValue, clazz));
                    }
                }
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }

        return result;
    }


    /**
     * @param key
     * @param field
     * @return
     */
    public Boolean hexists(String key, String field) {
        Boolean result = false;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.hexists(key, field);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }


    /**
     * @param key
     * @param string
     * @return
     */
    public Long rpush(String key, String string) {
        Long result = 0L;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.rpush(key, string);
        } catch (Exception e) {
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }

    /**
     * @param key
     * @param o
     * @return
     */
    public Long rpush(String key, Object o) {
        Long result = 0L;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            String string = JacksonUtil.toJsonStr(o);

            result = jedis.rpush(key, string);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }


    /**
     * @param key
     * @param string
     * @return
     */
    public Long lpush(String key, String string) {
        Long result = 0L;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.lpush(key, string);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }


    /**
     * @param key
     * @param o
     * @return
     */
    public Long lpush(String key, Object o) {
        Long result = 0L;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            String string = JacksonUtil.toJsonStr(o);

            result = jedis.lpush(key, string);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }


    /**
     * @param key
     * @param oList
     * @param <T>
     */
    public <T> void lpush(String key, List<T> oList) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            for (T t : oList) {
                String string = JacksonUtil.toJsonStr(t);

                jedis.lpush(key, string);
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
    }


    /**
     * @param key
     * @return
     */
    public Long llen(String key) {
        Long result = 0L;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.llen(key);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }


    /**
     * @param key
     * @param start
     * @param end
     * @return
     */
    public List<String> lrange(String key, int start, int end) {
        List<String> result = new ArrayList<String>();
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.lrange(key, start, end);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }


    /**
     * @param key
     * @param clazz
     * @param start
     * @param end
     * @param <T>
     * @return
     */
    public <T> List<T> lrange(String key, Class<T> clazz, int start, int end) {
        List<T> result = new ArrayList<T>();
        Jedis jedis = null;
        try {
            jedis = getJedis();
            List<String> strings = jedis.lrange(key, start, end);

            if (strings != null && !strings.isEmpty()) {
                for (final String string : strings) {
                    result.add(JacksonUtil.readValue(string, clazz));
                }
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }


    /**
     * @param key
     * @param field
     * @return
     */
    public Long hdel(String key, String field) {
        Long result = 0L;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.hdel(key, field);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }

    /**
     * @param key
     * @param oList
     * @param <T>
     */
    public <T> void rpush(String key, List<T> oList) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            for (Object o : oList) {
                String s = JacksonUtil.toJsonStr(o);
                jedis.rpush(key, s);
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
    }


    /**
     * @param key
     * @param start
     * @param end
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T lrange(String key, int start, int end, TypeReference<T> clazz) {
        List<String> jsonList = null;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jsonList = jedis.lrange(key, start, end);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            if (jedis != null) {
                releaseJedisInstance(jedis);
            }
        }
        if (jsonList == null || jsonList.isEmpty()) {
            return null;
        } else {
            return JacksonUtil.readValue(jsonList.toString(), clazz);
        }

    }


    /**
     * @param key
     * @param clazz
     * @param size
     * @param <T>
     * @return
     */
    public <T> List<T> lpop(String key, Class<T> clazz, int size) {
        String json = null;
        Jedis jedis = null;
        int count = 0;
        List<T> results = null;

        try {
            jedis = getJedis();
            long exists = jedis.llen(key);
            count = (int) (exists > size ? size : exists);

            if (count > 0) {
                results = new ArrayList<T>();
            }

            for (int i = 0; i < count; i++) {
                json = jedis.lpop(key);

                if (json != null) {
                    results.add(JacksonUtil.readValue(json, clazz));
                }
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }

        return results;
    }

    /**
     * @param key
     * @param clazz
     * @param size
     * @param <T>
     * @return
     */
    public <T> List<T> rpop(String key, Class<T> clazz, int size) {
        String json = null;
        Jedis jedis = null;
        int count = 0;
        List<T> results = null;

        try {
            jedis = getJedis();
            long exists = jedis.llen(key);
            count = (int) (exists > size ? size : exists);

            if (count > 0) {
                results = new ArrayList<T>();
            }

            for (int i = 0; i < count; i++) {
                json = jedis.rpop(key);

                if (json != null) {
                    results.add(JacksonUtil.readValue(json, clazz));
                }
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }

        return results;
    }

    /**
     * 删除制定key的List数据结构
     *
     * @param key 键
     * @return 返回列表中的第一个值
     */
    public String lpop(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.lpop(key);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }


    /**
     * @param key
     * @return
     */
    public String rpop(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.rpop(key);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }


    /**
     * @param key
     * @param count
     * @param value
     * @return
     */
    public Long lrem(String key, int count, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.lrem(key, count, value);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * @param key
     * @param index
     * @return
     */
    public String lindex(String key, int index) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.lindex(key, index);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * @param key
     * @param index
     * @param value
     * @return
     */
    public String lset(String key, int index, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.lset(key, index, value);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * @param key
     * @param member
     */
    public void sadd(String key, String member) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.sadd(key, member);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
    }


    /**
     * @param key
     * @param members
     */
    public void saddAll(String key, Set<String> members) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            for (final String member : members) {
                jedis.sadd(key, member);
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
    }


    /**
     * 返回集合中的所有的成员
     *
     * @param key
     * @return
     */
    public Set<String> smembers(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.smembers(key);
        } catch (Exception e) {
            logger.error("\r\n ********* [返回集合中的所有的成员失败]" + ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }


    /**
     * 移除集合中的一个或多个成员元素，不存在的成员元素会被忽略
     *
     * @param key
     * @param member
     * @return
     */
    public Long srem(String key, String member) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.srem(key, member);
        } catch (Exception e) {
            logger.error("\r\n ********* [移除集合中的一个或多个成员元素，不存在的成员元素会被忽略失败]" + ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * 移除并返回集合中的一个随机元素
     *
     * @param key
     * @return
     */
    public String spop(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.spop(key);
        } catch (Exception e) {
            logger.error("\r\n ********* [移除并返回集合中的一个随机元素失败]" + ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * 返回集合中一个或多个随机元素
     *
     * @param key
     * @param count
     * @return
     */
    public List<String> srandmember(String key, int count) {
        Jedis jedis = null;
        try {
            jedis = getJedis();

            return jedis.srandmember(key, count);
        } catch (Exception e) {
            logger.error("\r\n ********* [返回集合中一个或多个随机元素]" + ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * 移除集合中一个或多个成员
     *
     * @param key
     * @param members
     * @return
     */
    public Long srem(String key, List<String> members) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            for (String member : members) {
                jedis.srem(key, member);
            }
            return Long.parseLong(String.valueOf(members.size()));
        } catch (Exception e) {
            logger.error("\r\n ********* [移除集合中一个或多个成员出错：]" + ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * 移除集合中一个或多个成员
     *
     * @param key
     * @param members
     * @return
     */
    public Long srem(String key, String... members) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.srem(key, members);
        } catch (Exception e) {
            logger.error("\r\n ********* [移除集合中一个或多个成员出错：]" + ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * 判断member元素是否集合key的成员
     *
     * @param key
     * @param member
     * @return
     */
    public Boolean sismember(String key, String member) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sismember(key, member);
        } catch (Exception e) {
            logger.error("\r\n ********* [判断 member 元素是否集合 key 的成员失败]" + ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * 获取存储在集合中的元素的数量
     *
     * @param key
     * @return
     */
    public Long scard(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.scard(key);
        } catch (Exception e) {
            logger.error("\r\n ********* [获取存储在集合中的元素的数量失败]" + ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * 获取键到期的剩余时间(秒)
     *
     * @param key
     * @return
     */
    public Long ttl(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.ttl(key);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }


    /**
     * @param channel
     * @param message
     * @return
     */
    public Long publish(String channel, String message) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.publish(channel, message);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * @param jedisPubSub
     * @param channels
     */
    public void subscribe(JedisPubSub jedisPubSub, String... channels) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.subscribe(jedisPubSub, channels);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * @param jedisPubSub
     * @param partterns
     */
    public void psubscribe(JedisPubSub jedisPubSub, String... partterns) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.psubscribe(jedisPubSub, partterns);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * @param key
     * @return
     */
    public Long incr(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.incr(key);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * 将key中储存的数字加上指定的增量值
     *
     * @param key
     * @param integer
     * @return
     */
    public Long incrBy(String key, long integer) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.incrBy(key, integer);
        } catch (Exception e) {
            logger.error("\r\n ********* [ key 中储存的数字加上指定的增量值失败]" + ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * 用于增加存储在字段中存储由增量键哈希的数量
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    public Long hincrBy(String key, String field, long value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.hincrBy(key, field, value);
        } catch (Exception e) {
            logger.error("\r\n ********* [用于增加存储在字段中存储由增量键哈希的数量失败]" + ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * @param key
     * @return
     */
    public Long decr(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.decr(key);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * 减小存储在由指定的值的key的数量
     *
     * @param key
     * @param num
     * @return
     */
    public Long decrBy(String key, long num) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.decrBy(key, num);
        } catch (Exception e) {
            logger.error("\r\n ********* [减小存储在由指定的值的key的数量失败]" + ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * 返回在指定的键存储在集合中的元素的数量
     *
     * @param key
     * @return
     */
    public Long zcard(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zcard(key);
        } catch (Exception e) {
            logger.error("\r\n ********* [返回在指定的键存储在集合中的元素的数量失败]" + ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * 返回存储在关键的排序元素集合在指定的范围
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<String> zrange(String key, int start, int end) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zrange(key, start, end);
        } catch (Exception e) {
            logger.error("\r\n ********* [返回存储在关键的排序元素集合在指定的范围失败]" + ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }


    /**
     * 返回存储在关键的排序元素集合在指定的范围
     *
     * @param key
     * @param clazz
     * @param start
     * @param end
     * @param <T>
     * @return
     */
    public <T> Set<T> zrange(String key, Class<T> clazz, int start, int end) {
        Set<T> result = new TreeSet<T>();
        Jedis jedis = null;
        Iterator<String> it = null;
        try {
            jedis = getJedis();
            Set<String> zset = jedis.zrange(key, start, end);

            if (zset != null && !zset.isEmpty()) {
                it = zset.iterator();
                while (it.hasNext()) {
                    result.add(JacksonUtil.readValue(it.next(), clazz));
                }
            }
        } catch (Exception e) {
            logger.error("\r\n ********* [返回存储在关键的排序元素集合在指定的范围失败]" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }

    public Set<String> zrevrange(String key, int start, int end) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zrevrange(key, start, end);
        } catch (Exception e) {
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * 返回存储在键的排序元素集合在指定的范围
     *
     * @param key
     * @param clazz
     * @param start
     * @param end
     * @param <T>
     * @return
     */
    public <T> Set<T> zrevrange(String key, Class<T> clazz, int start, int end) {
        Set<T> result = new TreeSet<T>();
        Jedis jedis = null;
        Iterator<String> it = null;
        String json = null;
        try {
            jedis = getJedis();
            Set<String> zset = jedis.zrevrange(key, start, end);

            if (zset != null && !zset.isEmpty()) {
                it = zset.iterator();
                while (it.hasNext()) {
                    json = (String) it.next();
                    result.add(JacksonUtil.readValue(json, clazz));
                }
            }
        } catch (Exception e) {
            logger.error("\r\n ********* [返回存储在键的排序元素集合在指定的范围失败]" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
        return result;
    }


    /**
     * 返回有序集合在最小值和最大值(包括得分等于最小或最大元素)之间的分数键中的所有元素
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Set<String> zrangeByScore(String key, Long min, Long max) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zrangeByScore(key, min, max);
        } catch (Exception e) {
            logger.error("\r\n ********* [返回的有序集合在最小值和最大值(包括得分等于最小或最大元素)之间的分数键中的所有元素失败]" + ExceptionUtils.getFullStackTrace(e));
            return null;
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * 有序集合存储在键删除指定成员
     *
     * @param key
     * @param member
     */
    public void zrem(String key, String member) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.zrem(key, member);
        } catch (Exception e) {
            logger.error("\r\n ********* [有序集合存储在键删除指定成员失败]" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * 添加所有指定的成员指定的分数存放在键的有序集合
     *
     * @param key
     * @param score
     * @param member
     */
    public void zadd(String key, Long score, String member) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.zadd(key, score, member);
        } catch (Exception e) {
            logger.error("\r\n ********* [添加所有指定的成员指定的分数存放在键的有序集合失败]" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * 加单位成员的有序集合存储增量键比分
     *
     * @param key
     * @param score
     * @param member
     */
    public void zincrBy(String key, Long score, String member) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.zincrby(key, score, member);
        } catch (Exception e) {
            logger.error("\r\n ********* [加单位成员的有序集合存储增量键比分失败]" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * 加单位成员的有序集合存储增量键比分
     *
     * @param key
     * @param score
     * @param o
     */
    public void zincrBy(String key, Long score, Object o) {
        Jedis jedis = null;
        try {
            String member = JacksonUtil.toJsonStr(o);
            jedis = getJedis();
            jedis.zincrby(key, score, member);
        } catch (Exception e) {
            logger.error("\r\n ********* [加单位成员的有序集合存储增量键比分失败]" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * 删除的元素数量
     *
     * @param key
     * @param start
     * @param end
     */
    public void zremrangeByRank(String key, int start, int end) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.zremrangeByRank(key, start, end);
        } catch (Exception e) {
            logger.error("\r\n ********* [删除的元素数量失败]" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /***
     * 删除的有序集合保存在key的最小值和最大值(含)之间的分数的所有元素数量。
     *
     * @param key
     * @param start
     * @param end
     */
    public void zremrangeByScore(String key, Long start, Long end) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.zremrangeByScore(key, start, end);
        } catch (Exception e) {
            logger.error("\r\n ********* [删除的有序集合保存在key的最小值和最大值(含)之间的分数的所有元素数量失败]" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
    }


    /**
     * 返回成员的有序集合在键比分
     *
     * @param key
     * @param member
     * @return
     */
    public int zscore(String key, String member) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            Double score = jedis.zscore(key, member);
            if (score != null) {
                return score.intValue();
            } else {
                return 0;
            }
        } catch (Exception e) {
            logger.error("\r\n ********* [返回成员的有序集合在键比分失败]" + ExceptionUtils.getFullStackTrace(e));
            return 0;
        } finally {
            releaseJedisInstance(jedis);
        }
    }


    /**
     * 返回成员的有序集合在键比分
     *
     * @param key
     * @param o
     * @return
     */
    public int zscore(String key, Object o) {
        Jedis jedis = null;
        try {
            String member = JacksonUtil.toJsonStr(o);
            jedis = getJedis();
            Double score = jedis.zscore(key, member);
            if (score != null) {
                return score.intValue();
            } else {
                return 0;
            }
        } catch (Exception e) {
            logger.error("\r\n ********* [返回成员的有序集合键值比分失败]" + ExceptionUtils.getFullStackTrace(e));
            return 0;
        } finally {
            releaseJedisInstance(jedis);
        }
    }

    /**
     * 删除Redis中的所有key
     *
     * @throws Exception
     */
    public void flushAll() {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.flushAll();
        } catch (Exception e) {
            logger.error("\r\n ********* [Cache清空失败]" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            releaseJedisInstance(jedis);
        }
    }
}
