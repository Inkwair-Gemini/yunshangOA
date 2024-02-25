package com.Ink.exception;

import com.Ink.result.ResultCodeEnum;
import lombok.Data;

@Data
public class SpecialException extends RuntimeException {
    private Integer code;
    private String msg;

    /**
     * 通过状态码和错误消息创建异常对象
     *
     * @param code
     * @param msg
     */
    public SpecialException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    /**
     * 接收枚举类型对象
     *
     * @param resultCodeEnum
     */
    public SpecialException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
        this.msg = resultCodeEnum.getMessage();
    }

    @Override
    public String toString() {
        return "SpecialException{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }

    public String getErrorMsg() {
        return msg;
    }
}
