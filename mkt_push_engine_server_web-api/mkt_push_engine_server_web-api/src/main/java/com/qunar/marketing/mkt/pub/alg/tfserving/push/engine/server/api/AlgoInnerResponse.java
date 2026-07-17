package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api;

import java.io.Serializable;
import java.util.Map;

public class AlgoInnerResponse implements Serializable {
    private long cpuTime;

    private Constants code;
    private String msg;

    double[][] scores;

    Map<String, float[][]> vectors;

    /**
     * 无参构造方法
     * <p>补充说明：创建一个空的响应对象，后续通过setter填充各字段。
     */
    public AlgoInnerResponse() {
    }

    /**
     * 构造包含CPU耗时、状态码与消息的响应对象
     * <p>补充说明：用于不含打分与向量的简单响应场景。
     * @param cupTime CPU耗时
     * @param code    状态码
     * @param msg     描述消息
     */
    public AlgoInnerResponse(long cupTime, Constants code, String msg) {
        this.cpuTime = cupTime;
        this.code = code;
        this.msg = msg;
    }

    /**
     * 构造包含CPU耗时、状态码、消息与打分矩阵的响应对象
     * <p>补充说明：在基础响应基础上额外携带打分结果数组。
     * @param cupTime CPU耗时
     * @param code    状态码
     * @param msg     描述消息
     * @param scores  打分矩阵
     */
    public AlgoInnerResponse(long cupTime, Constants code, String msg, double[][] scores) {
        this.cpuTime = cupTime;
        this.code = code;
        this.msg = msg;
        this.scores = scores;
    }

    /**
     * 构造包含CPU耗时、状态码、消息、打分矩阵与向量映射的完整响应对象
     * <p>补充说明：在带打分的构造方法基础上再附加向量集合，用于需要返回向量的完整算法响应场景。
     * @param cupTime CPU耗时
     * @param code    状态码
     * @param msg     描述消息
     * @param scores  打分矩阵
     * @param vectors 向量映射，键为标识、值为二维float数组
     */
    public AlgoInnerResponse(long cupTime, Constants code, String msg, double[][] scores, Map<String, float[][]> vectors) {
        this.cpuTime = cupTime;
        this.code = code;
        this.msg = msg;
        this.scores = scores;
        this.vectors = vectors;
    }

    public long getCpuTime() {
        return cpuTime;
    }
    public void setCpuTime(long cpuTime) {
        this.cpuTime = cpuTime;
    }
    public Constants getCode() {
        return code;
    }
    public void setCode(Constants code) {
        this.code = code;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
    public double[][] getScores() {
        return scores;
    }
    public void setScores(double[][] scores) {
        this.scores = scores;
    }

    public Map<String, float[][]> getVectors() {
        return this.vectors;
    }
    public void setVectors(Map<String, float[][]> vectors) {
        this.vectors = vectors;
    }
}
