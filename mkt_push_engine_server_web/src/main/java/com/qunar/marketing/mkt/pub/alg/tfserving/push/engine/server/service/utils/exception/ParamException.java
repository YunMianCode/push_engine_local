package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.exception;

public class ParamException extends RuntimeException {

    public ParamException(String message){
        super(message);
    }
    public ParamException(String message, Throwable cause){
        super(message,cause);
    }
}
