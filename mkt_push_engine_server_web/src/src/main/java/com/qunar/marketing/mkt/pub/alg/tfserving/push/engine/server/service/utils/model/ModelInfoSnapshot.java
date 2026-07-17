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
    /**
     * 拼接模型名称与版本为带版本标识的字符串
     * @param modelName 模型名称
     * @param version 模型版本号
     * @return 格式为 模型名_version_版本号 的字符串
     */
    public static String modelNameWithVersion(String modelName, int version) {
        return modelName + "_version_" + version;
    }

    /**
     * 获取当前实例的模型名称加版本标识字符串
     * <p>补充说明：委托给静态方法 modelNameWithVersion 完成拼接
     * @return 格式为 模型名_version_版本号 的字符串
     */
    public String modelNameWithVersion() {
        return modelNameWithVersion(modelName, version);
    }
    /**
     * 获取版本号的字符串形式
     * @return 版本号对应的字符串
     */
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

    /**
     * 将当前对象序列化为格式化的 JSON 字符串
     * <p>补充说明：使用 Gson 并开启 prettyPrinting，输出带缩进的 JSON
     * @return 格式化后的 JSON 字符串
     */
    public String toJsonString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}
