package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api;

import java.io.Serializable;
import java.util.Map;

public class AlgoInnerResponse implements Serializable {
    private long cpuTime;

    private Constants code;
    private String msg;

    double[][] scores;

    Map<String, float[][]> vectors;

    public AlgoInnerResponse() {
    }

    public AlgoInnerResponse(long cupTime, Constants code, String msg) {
        this.cpuTime = cupTime;
        this.code = code;
        this.msg = msg;
    }

    public AlgoInnerResponse(long cupTime, Constants code, String msg, double[][] scores) {
        this.cpuTime = cupTime;
        this.code = code;
        this.msg = msg;
        this.scores = scores;
    }

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
