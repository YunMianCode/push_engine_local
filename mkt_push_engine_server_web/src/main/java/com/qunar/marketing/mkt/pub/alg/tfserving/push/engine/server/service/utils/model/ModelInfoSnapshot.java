package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ModelInfoSnapshot {
    private String modelName;

    private int version;

    //默认false，即model和xml都需要更新!!!
    private boolean isOnlyXmlUpdateFlag = false;

    public String getModelName() {
        return modelName;
    }
    public static String modelNameWithVersion(String modelName, int version) {
        return modelName + "_version_" + version;
    }

    public String getModelNameWithVersion() {
        return modelName + "_version_" + version;
    }

    public String modelNameWithVersion() {
        return modelNameWithVersion(modelName, version);
    }
    public String getVersionString() {
        return version + "";
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isOnlyXmlUpdateFlag() {
        return isOnlyXmlUpdateFlag;
    }

    public void setOnlyXmlUpdateFlag(boolean onlyXmlUpdateFlag) {
        isOnlyXmlUpdateFlag = onlyXmlUpdateFlag;
    }

    public String toJsonString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}
