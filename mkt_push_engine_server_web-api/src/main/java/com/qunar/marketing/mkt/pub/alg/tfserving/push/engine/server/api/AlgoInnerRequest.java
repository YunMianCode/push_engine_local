package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api;

import java.io.Serializable;
import java.util.List;

public class AlgoInnerRequest implements Serializable {
    private int cupTimeFlag;
    private String modelKey;
    private String userId;
    private String deviceId;
    private List<String> itemIds;
    private int batchSize;


    public boolean isSetCpuTimeFlag() {
        return cupTimeFlag != 0;
    }
    public void setCpuTimeFlag(int cupTimeFlag) {
        this.cupTimeFlag = cupTimeFlag;
    }
    public int getCpuTimeFlag() {
        return cupTimeFlag;
    }
    public void setModelKey(String modelKey) {
        this.modelKey = modelKey;
    }
    public String getModelKey() {
        return modelKey;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getUserId() {
        return userId;
    }
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    public String getDeviceId() {
        return deviceId;
    }
    public void setItemIds(List<String> itemIds) {
        this.itemIds = itemIds;
    }
    public List<String> getItemIds() {
        return itemIds;
    }
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
    public int getBatchSize(){
        return batchSize;
    }
}
