package com.canteen.common.result;

public enum ResultCode {
    SUCCESS(200, "操作成功"),
    UNAUTHORIZED(401, "未登录或 Token 已过期"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    BUSINESS_ERROR(422, "业务逻辑错误"),
    PARAM_ERROR(400, "参数校验失败"),
    SERVER_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
