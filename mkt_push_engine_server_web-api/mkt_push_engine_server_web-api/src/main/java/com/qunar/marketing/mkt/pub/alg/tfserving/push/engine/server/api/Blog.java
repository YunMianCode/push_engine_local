package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api;

import java.io.Serializable;

public class Blog implements Serializable {

    private Integer id;
    private String title;
    private String content;

    /**
     * 构造指定ID与标题的Blog对象
     * <p>补充说明：仅初始化id与title，content字段需后续通过setContent设置。
     * @param id    Blog标识
     * @param title Blog标题
     */
    public Blog(Integer id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

}
