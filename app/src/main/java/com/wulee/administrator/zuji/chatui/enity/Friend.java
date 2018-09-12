package com.wulee.administrator.zuji.chatui.enity;

import com.wulee.administrator.zuji.database.bean.PersonInfo;

import cn.bmob.v3.BmobObject;

/**
 * create by  wulee   2018/8/31 16:30
 * desc:好友
 */
public class Friend extends BmobObject {
    //用户
    private PersonInfo user;
    //好友
    private PersonInfo friendUser;

    public PersonInfo getUser() {
        return user;
    }

    public void setUser(PersonInfo user) {
        this.user = user;
    }

    public PersonInfo getFriendUser() {
        return friendUser;
    }

    public void setFriendUser(PersonInfo friendUser) {
        this.friendUser = friendUser;
    }
}
