package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.exception;

public class ParamException extends RuntimeException {

    /**
     * 构造一个仅包含错误信息的参数异常
     * @param message 异常错误信息
     */
    public ParamException(String message){
        super(message);
    }

    /**
     * 构造一个包含错误信息及原因链的参数异常
     * @param message 异常错误信息
     * @param cause 导致该异常的原始Throwable
     */
    public ParamException(String message, Throwable cause){
        super(message,cause);
    }
}
