package com.wulee.administrator.zuji.entity;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.wulee.administrator.zuji.App;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.database.NewFriendManager;
import com.wulee.administrator.zuji.database.bean.NewFriendInfo;

/**
 * 新朋友会话
 * Created by Administrator on 2016/5/25.
 */
public class NewFriendConversation extends Conversation{

    NewFriendInfo lastFriend;

    public NewFriendConversation(NewFriendInfo friend){
        this.lastFriend=friend;
        this.cName="新朋友";
    }

    @Override
    public String getLastMessageContent() {
        if(lastFriend!=null){
            Integer status =lastFriend.getStatus();
            String name = lastFriend.getName();
            if(TextUtils.isEmpty(name)){
                name = lastFriend.getUid();
            }
            //目前的好友请求都是别人发给我的
            if(status==null || status== Constant.STATUS_VERIFY_NONE||status ==Constant.STATUS_VERIFY_READED){
                return name+"请求添加好友";
            }else{
                return "我已添加"+name;
            }
        }else{
            return "";
        }
    }

    @Override
    public long getLastMessageTime() {
        if(lastFriend!=null){
            return lastFriend.getTime();
        }else{
            return 0;
        }
    }

    @Override
    public Object getAvatar() {
        return R.mipmap.ic_launcher;
    }

    @Override
    public int getUnReadCount() {
        return NewFriendManager.getInstance(App.context).getNewInvitationCount();
    }

    @Override
    public void readAllMessages() {
        //批量更新未读未认证的消息为已读状态
        NewFriendManager.getInstance(App.context).updateBatchStatus();
    }

    @Override
    public void onClick(Context context) {
        Intent intent = new Intent();
        //intent.setClass(context, NewFriendActivity.class);
        //context.startActivity(intent);
    }

    @Override
    public void onLongClick(Context context) {
        NewFriendManager.getInstance(context).deleteNewFriend(lastFriend);
    }
}
