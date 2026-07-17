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

    /**
     * 私有构造方法初始化 ThreadMXBean
     * <p>补充说明：从 ManagementFactory 获取 ThreadMXBean，仅当支持当前线程 CPU 时间统计时保留实例，否则置为 null 以便后续方法降级返回 0
     */
    private ThreadMXBeanUtils() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        this.threadMXBean = threadMXBean.isCurrentThreadCpuTimeSupported() ? threadMXBean : null;
    }

    /**
     * 获取当前线程 CPU 时间并转换为微秒
     * <p>补充说明：统一用 μs 单位，内部委托 currentThreadCpuTime 获取纳秒值后用 TimeUnit 转换
     * @return 当前线程 CPU 时间（微秒）；不支持统计时返回 0
     */
    public long currentThreadCpuTimeMicros() {
        return TimeUnit.NANOSECONDS.toMicros(currentThreadCpuTime());
    }

    /**
     * 获取当前线程 CPU 时间（纳秒）
     * <p>补充说明：当 ThreadMXBean 为 null（不支持 CPU 时间统计）时降级返回 0，否则直接读取原生纳秒值
     * @return 当前线程 CPU 时间（纳秒）；不支持时返回 0
     */
    private long currentThreadCpuTime() {
        if (threadMXBean == null) {
            return 0;
        }
        return threadMXBean.getCurrentThreadCpuTime();
    }
}
