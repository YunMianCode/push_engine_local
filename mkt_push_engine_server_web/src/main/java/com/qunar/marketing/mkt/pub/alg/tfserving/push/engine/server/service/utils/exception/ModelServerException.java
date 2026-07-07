package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.exception;

public class ModelServerException extends RuntimeException {
    public ModelServerException(String message){
        super(message);
    }
    public ModelServerException(String message, Throwable cause){
        super(message, cause);
    }
}
