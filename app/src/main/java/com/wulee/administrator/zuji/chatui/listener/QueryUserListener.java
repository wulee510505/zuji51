package com.wulee.administrator.zuji.chatui.listener;

import com.wulee.administrator.zuji.database.bean.PersonInfo;

import cn.bmob.newim.listener.BmobListener1;
import cn.bmob.v3.exception.BmobException;

/**
 * @author :smile
 * @project:QueryUserListener
 * @date :2016-02-01-16:23
 */
public abstract class QueryUserListener extends BmobListener1<PersonInfo> {

    public abstract void done(PersonInfo s, BmobException e);

    @Override
    protected void postDone(PersonInfo o, BmobException e) {
        done(o, e);
    }
}
