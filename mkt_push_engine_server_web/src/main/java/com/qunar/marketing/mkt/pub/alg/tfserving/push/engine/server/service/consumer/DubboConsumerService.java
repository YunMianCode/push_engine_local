package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.consumer;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.Blog;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerResponse;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerRequest;

import java.util.concurrent.CompletableFuture;

public interface DubboConsumerService {
    Blog call(Integer blogId);

    Blog callBetterAsync1(Integer blogId);

    Blog callBetterAsync2(Integer blogId);

    CompletableFuture<Blog> apacheDubboFutureAsyncService(Integer blobId);

    AlgoInnerResponse callAlgo(AlgoInnerRequest request);

    AlgoInnerResponse callAlgoAsync1(AlgoInnerRequest request);

    AlgoInnerResponse callAlgoAsync2(AlgoInnerRequest request);

    CompletableFuture<AlgoInnerResponse> apacheDubboAlogFutureAsync(AlgoInnerRequest request);
}
