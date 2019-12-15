package com.project.frame.commons.config;

import com.project.frame.utils.exception.BASE_RESPONSE_CODE_ENUM;
import com.project.frame.utils.exception.BaseCustomException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理
 *
 * @author mxy
 * @date 2019/12/15
 */
@RestControllerAdvice
public class ExceptionHandlerAdvice {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    // 响应码key
    public static final String RESPONSE_CODE_NAME = "code";

    // 响应信息key
    public static final String RESPONSE_MSG_NAME = "msg";

    // 响应传输信息key
    public static final String RESPONSE_BODY_NAME = "data";

    /**
     * 捕获全局异常，处理所有不可知的异常
     *
     * @param exception 异常对象
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception exception) {
        logger.error("\r\n ************** 操作出现异常：{}", ExceptionUtils.getStackTrace(exception));

        Class eClass = exception.getClass();
        Map<String, Object> map = new HashMap<>();

        if (eClass.equals(HttpRequestMethodNotSupportedException.class)) {
            addResCodeToMap(BASE_RESPONSE_CODE_ENUM.METHOD_NOT_SUPPORTED, map);
        } else if (eClass.equals(HttpMediaTypeNotAcceptableException.class)) {
            addResCodeToMap(BASE_RESPONSE_CODE_ENUM.MEDIA_TYPE_NOT_ACCEPT, map);
        } else if (eClass.equals(HttpMediaTypeNotSupportedException.class)) {
            addResCodeToMap(BASE_RESPONSE_CODE_ENUM.MEDIA_TYPE_NOT_SUPPORTED, map);
        } else if (eClass.equals(ConversionNotSupportedException.class)) {
            addResCodeToMap(BASE_RESPONSE_CODE_ENUM.SERVER_ERROR, map);
        } else if (eClass.equals(HttpMessageNotWritableException.class)) {
            addResCodeToMap(BASE_RESPONSE_CODE_ENUM.SERVER_ERROR, map);
        } else {
            addResCodeToMap(BASE_RESPONSE_CODE_ENUM.SERVER_ERROR, map);
        }
        return map;
    }

    /**
     * 捕获业务异常
     *
     * @param exception 自定义接口异常类
     * @return 返回的异常信息map
     */
    @ExceptionHandler(BaseCustomException.class)
    public Map<String, Object> handleInterfaceException(BaseCustomException exception) {
        Map<String, Object> map = new HashMap<>();
        map.put(RESPONSE_CODE_NAME, exception.getErrorCode());
        map.put(RESPONSE_MSG_NAME, exception.getErrorMsg());
        return map;
    }

    /**
     * 添加异常信息到map中
     *
     * @param baseResponseCodeEnum 错误响应编码枚举类对象
     * @param map                  响应对象
     */
    private void addResCodeToMap(BASE_RESPONSE_CODE_ENUM baseResponseCodeEnum, Map<String, Object> map) {
        map.put(RESPONSE_CODE_NAME, baseResponseCodeEnum.getCode());
        map.put(RESPONSE_MSG_NAME, baseResponseCodeEnum.getMsg());
    }
}
