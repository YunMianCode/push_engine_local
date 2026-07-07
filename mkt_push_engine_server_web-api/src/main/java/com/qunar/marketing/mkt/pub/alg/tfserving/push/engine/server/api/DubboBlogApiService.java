package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api;

/**
 * 普通dubbo调用接口
 */
public interface DubboBlogApiService {
    Blog select(Integer blogId);
    AlgoInnerResponse predict(AlgoInnerRequest request);
}
