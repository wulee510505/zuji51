package com.wulee.administrator.zuji.entity;

import cn.bmob.v3.BmobObject;

/**
 * Created by wulee on 2017/10/10 14:56
 */

public class Notice extends BmobObject {

    private String content;
    private String imgUrl;

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
