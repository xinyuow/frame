package com.project.frame.shiro.redis;

import com.project.frame.commons.constant.RedisConstant;
import com.project.frame.utils.ByteUtil;
import com.project.frame.utils.RedisClient;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * ShiroRedisSessionDAO
 * 此类由ShiroConfig注入到Spring
 *
 * @author mxy
 * @date 2019/12/20
 */
public class ShiroRedisSessionDAO extends AbstractSessionDAO {
    private static Logger logger = LoggerFactory.getLogger(ShiroRedisSessionDAO.class);

    // 注入redisClient实例
    @Resource(name = "redisClient")
    private RedisClient redisClient;

    /**
     * 获得String类型的sessionId
     */
    private String getPreStringKey(Serializable sessionId) {
        return RedisConstant.ADMIN_SHIRO_SESSION_KEY + sessionId;
    }

    /**
     * 更新session
     */
    @Override
    public void update(Session session) throws UnknownSessionException {
        this.saveSession(session);
    }

    /**
     * 保存session
     */
    private void saveSession(Session session) throws UnknownSessionException {
        if (session == null || session.getId() == null) {
            logger.error("session or session id is null");
            return;
        }
        this.redisClient.setAndExpire(this.getPreStringKey(session.getId()), session, RedisConstant.ADMIN_SHIRO_SESSION_EXPIRE);
    }

    /**
     * 删除session
     */
    @Override
    public void delete(Session session) {
        if (session == null || session.getId() == null) {
            logger.error("session or session id is null");
            return;
        }
        redisClient.del(getPreStringKey(session.getId()));

    }

    @Override
    public Collection<Session> getActiveSessions() {
        Set<Session> sessions = new HashSet<Session>();

        Set<byte[]> keys = null;
        try {
            keys = redisClient.keys(ByteUtil.objectToBytes(RedisConstant.ADMIN_SHIRO_SESSION_KEY + "*"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (keys != null && keys.size() > 0) {
            for (byte[] key : keys) {
                Session s = null;
                try {
                    s = (Session) ByteUtil.bytesToObject(redisClient.get(key));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                sessions.add(s);
            }
        }

        return sessions;
    }

    @Override
    protected Serializable doCreate(Session session) {
        Serializable sessionId = this.generateSessionId(session);
        this.assignSessionId(session, sessionId);
        this.saveSession(session);
        return sessionId;
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        if (sessionId == null) {
            logger.error("session id is null");
            return null;
        }

        return (Session) redisClient.get(this.getPreStringKey(sessionId));
    }
}
