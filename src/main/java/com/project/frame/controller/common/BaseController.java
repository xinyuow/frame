package com.project.frame.controller.common;

import com.project.frame.commons.config.ExceptionHandlerAdvice;
import com.project.frame.utils.exception.BASE_RESPONSE_CODE_ENUM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 基础控制器
 *
 * @author mxy
 * @date 2019/12/15
 */
public class BaseController implements Serializable {
    private static final long serialVersionUID = -5768209470955252228L;
    protected static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    /**
     * 添加数据到结果对象中
     *
     * @param obj 封装接口集合参数
     * @return map
     */
    protected Map<String, Object> getResult(Object obj) {
        Map<String, Object> map = new HashMap<>();
        map.put(ExceptionHandlerAdvice.RESPONSE_CODE_NAME, BASE_RESPONSE_CODE_ENUM.REQUEST_SUCCESS.getCode());
        map.put(ExceptionHandlerAdvice.RESPONSE_MSG_NAME, BASE_RESPONSE_CODE_ENUM.REQUEST_SUCCESS.getMsg());
        map.put(ExceptionHandlerAdvice.RESPONSE_BODY_NAME, obj);
        return map;
    }

    /**
     * 返回成功
     *
     * @return map
     */
    protected Map<String, Object> getSuccessResult() {
        Map<String, Object> map = new HashMap<>();
        map.put(ExceptionHandlerAdvice.RESPONSE_CODE_NAME, BASE_RESPONSE_CODE_ENUM.REQUEST_SUCCESS.getCode());
        map.put(ExceptionHandlerAdvice.RESPONSE_MSG_NAME, BASE_RESPONSE_CODE_ENUM.REQUEST_SUCCESS.getMsg());
        return map;
    }

    /**
     * 返回失败
     *
     * @param code 状态码
     * @param msg  失败原因
     * @return map
     */
    protected Map<String, Object> getFailResult(String code, String msg) {
        Map<String, Object> map = new HashMap<>();
        map.put(ExceptionHandlerAdvice.RESPONSE_CODE_NAME, code);
        map.put(ExceptionHandlerAdvice.RESPONSE_MSG_NAME, msg);
        return map;
    }
}
