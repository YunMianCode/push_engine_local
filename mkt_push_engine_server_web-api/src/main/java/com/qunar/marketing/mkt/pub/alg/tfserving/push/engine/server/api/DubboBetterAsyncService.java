package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api;

import qunar.tc.dubbo.async.Async;

/**
* @author tcdev
* @date 2021/9/23 12:00
* @description 使用qunar封装的dubbo异步，参考wiki：https://wiki.corp.qunar.com/confluence/display/devwiki/Better+Async
*/
@Async
public interface DubboBetterAsyncService {
    Blog longTaskSelect(Integer blogId);

    AlgoInnerResponse predict(AlgoInnerRequest request);
}
