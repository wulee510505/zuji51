package com.wulee.administrator.zuji.chatui.enity;

import android.text.TextUtils;

import com.app.hubert.guide.util.LogUtil;

import org.json.JSONObject;

import cn.bmob.newim.bean.BmobIMExtraMessage;
import cn.bmob.newim.bean.BmobIMMessage;

/**
 * create by  wulee   2018/8/31 16:36
 * desc: 同意添加好友请求-仅仅只用于发送同意添加好友的消息
 */
public class AgreeAddFriendMessage extends BmobIMExtraMessage {

    public static final String AGREE = "agree";

    //以下均是从extra里面抽离出来的字段，方便获取
    private String uid;//最初的发送方
    private Long time;
    private String msg;//用于通知栏显示的内容

    @Override
    public String getMsgType() {
        return "agree";
    }

    @Override
    public boolean isTransient() {
        //如果需要在对方的会话表中新增一条该类型的消息，则设置为false，表明是非暂态会话
        //此处将同意添加好友的请求设置为false，为了演示怎样向会话表和消息表中新增一个类型
        return false;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public AgreeAddFriendMessage(){}

    /**
     * 继承BmobIMMessage的属性
     * @param msg
     */
    private AgreeAddFriendMessage(BmobIMMessage msg){
        super.parse(msg);
    }

    /**将BmobIMMessage转成AgreeAddFriendMessage
     * @param msg 消息
     * @return
     */
    public static AgreeAddFriendMessage convert(BmobIMMessage msg){
        AgreeAddFriendMessage agree =new AgreeAddFriendMessage(msg);
        try {
            String extra = msg.getExtra();
            if(!TextUtils.isEmpty(extra)){
                JSONObject json =new JSONObject(extra);
                Long time = json.getLong("time");
                String uid =json.getString("uid");
                String m =json.getString("msg");
                agree.setMsg(m);
                agree.setUid(uid);
                agree.setTime(time);
            }else{
                LogUtil.i("AgreeAddFriendMessage的extra为空");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return agree;
    }

}
