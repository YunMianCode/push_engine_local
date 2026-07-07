package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.TimeUnit;

public class ThreadMXBeanUtils {
    private final ThreadMXBean threadMXBean;

    /**
     * 直接初始化ThreadMXBean
     */
    public static final ThreadMXBeanUtils INSTANCE = new ThreadMXBeanUtils();

    private ThreadMXBeanUtils() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        this.threadMXBean = threadMXBean.isCurrentThreadCpuTimeSupported() ? threadMXBean : null;
    }

    /**
     * 转换成微秒：μs（使用这个，统一用μs单位）
     * @return
     */
    public long currentThreadCpuTimeMicros() {
        return TimeUnit.NANOSECONDS.toMicros(currentThreadCpuTime());
    }

    /**
     * 单位：ns
     * @return
     */
    private long currentThreadCpuTime() {
        if (threadMXBean == null) {
            return 0;
        }
        return threadMXBean.getCurrentThreadCpuTime();
    }
}
