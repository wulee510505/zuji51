package com.wulee.administrator.zuji.entity;

import java.io.Serializable;

/**
 * Created by wulee on 2017/8/22 14:01
 */

public class PublishPicture implements Serializable{

    private int id;
    private String path;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
