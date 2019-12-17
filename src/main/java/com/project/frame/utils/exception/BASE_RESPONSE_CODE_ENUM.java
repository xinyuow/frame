package com.project.frame.utils.exception;

/**
 * 响应码枚举
 *
 * @author mxy
 * @date 2019/12/15
 */
public enum BASE_RESPONSE_CODE_ENUM {
    /********************* 操作成功 **********************/
    REQUEST_SUCCESS("200", "操作成功"),
    /********************* 操作失败 **********************/
    REQUEST_FAIL("1100", "操作失败"),

    /********************* 系统异常 **********************/
    MIS_REQ_PARAM("400", "请求参数丢失"),
    UNAUTHORIZED_REQUEST("401", "未认证"),
    FORBIDDEN_REQUEST("403", "未授权"),
    RESOURCE_NOT_FOUND("404", "请求的资源不存在"),
    METHOD_NOT_SUPPORTED("405", "不支持的请求方法"),
    MEDIA_TYPE_NOT_ACCEPT("406", "无法接受请求中的媒体类型"),
    MEDIA_TYPE_NOT_SUPPORTED("415", "不支持的媒体类型"),
    SERVER_ERROR("500", "获取数据异常"),

    /********************* 业务自定义异常 *****************/
    NOT_LOGIN_ERROR("1001", "重新登录"),
    CAPTCHA_ERROR("1002", "验证码错误"),
    ACCOUNT_OR_PASSWORD_ERROR("1003", "账号或密码错误"),
    ACCOUNT_HAS_BEEN_DISABLED("1004", "此账号已被禁用"),
    ACCOUNT_IS_LOCKED("1005", "此账号已被锁定"),
    ACCOUNT_AUTHENTICATION_FAILED("1006", "账号认证失败");

    /**
     * 错误编码
     */
    public String code;

    /**
     * 错误编码信息
     */
    public String msg;

    /**
     * 构造函数
     *
     * @param code 编码
     * @param msg  编码信息
     */
    BASE_RESPONSE_CODE_ENUM(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 获取编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置编码
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取编码信息
     */
    public String getMsg() {
        return msg;
    }

    /**
     * 设置编码信息
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }
}
