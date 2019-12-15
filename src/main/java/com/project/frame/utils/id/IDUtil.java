package com.project.frame.utils.id;

import org.springframework.stereotype.Component;

/**
 * 主键ID生成工具类
 *
 * @author mxy
 * @date 2019/12/15
 */
@Component
public class IDUtil {

    private static IDWorker instance = null;

    private IDUtil() {

    }

    public static long getId() {
        return getInstance().nextId();
    }

    private static IDWorker getInstance() {
        if (instance == null) {
            synchronized (IDUtil.class) {
                if (instance == null) {
                    instance = new IDWorker(0, 0);
                }
            }
        }
        return instance;
    }
}
