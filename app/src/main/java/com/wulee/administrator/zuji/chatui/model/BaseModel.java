package com.wulee.administrator.zuji.chatui.model;

import android.content.Context;

import com.wulee.administrator.zuji.App;

/**
 * @author :smile
 * @project:BaseModel
 * @date :2016-01-23-10:37
 */
public abstract class BaseModel {

    public int CODE_NULL=1000;
    public static int CODE_NOT_EQUAL=1001;

    public static final int DEFAULT_LIMIT=20;

    public Context getContext(){
        return App.INSTANCE();
    }
}
