package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api;

import java.io.Serializable;

public class Blog implements Serializable {

    private Integer id;
    private String title;
    private String content;

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
