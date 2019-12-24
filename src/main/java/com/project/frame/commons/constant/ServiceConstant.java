package com.project.frame.commons.constant;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 服务常量配置
 *
 * @author mxy
 * @date 2019/12/16
 */
@Component
public class ServiceConstant implements InitializingBean {

    @Value("${system.service.workerID}")
    private long workerId;

    @Value("${system.service.dataID}")
    private long dataId;

    /**
     * 工作中心ID
     */
    public static long WORK_ID;

    /**
     * 数据中心ID
     */
    public static long DATA_ID;

    @Override
    public void afterPropertiesSet() throws Exception {
        WORK_ID = workerId;
        DATA_ID = dataId;
    }
}
