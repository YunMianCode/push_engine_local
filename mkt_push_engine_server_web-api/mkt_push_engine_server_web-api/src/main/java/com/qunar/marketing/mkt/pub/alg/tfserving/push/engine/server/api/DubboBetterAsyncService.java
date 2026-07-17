package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api;

import qunar.tc.dubbo.async.Async;

/**
* @author tcdev
* @date 2021/9/23 12:00
* @description 使用qunar封装的dubbo异步，参考wiki：https://wiki.corp.qunar.com/confluence/display/devwiki/Better+Async
*/
@Async
public interface DubboBetterAsyncService {
    /**
     * 根据BlogID执行长耗时查询
     * <p>补充说明：Qunar封装的Dubbo异步接口，配合@Async注解在编译期生成异步版本；同步返回Blog结果。
     * @param blogId Blog标识
     * @return Blog对象
     */
    Blog longTaskSelect(Integer blogId);

    /**
     * 根据算法请求执行预测
     * <p>补充说明：Qunar封装的Dubbo异步接口，配合@Async注解在编译期生成异步版本；同步返回算法预测响应。
     * @param request 算法内部请求对象
     * @return 算法内部响应对象
     */
    AlgoInnerResponse predict(AlgoInnerRequest request);
}
