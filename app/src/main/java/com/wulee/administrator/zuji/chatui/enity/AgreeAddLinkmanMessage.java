package com.wulee.administrator.zuji.chatui.enity;

import android.text.TextUtils;

import com.app.hubert.guide.util.LogUtil;

import org.json.JSONObject;

import cn.bmob.newim.bean.BmobIMExtraMessage;
import cn.bmob.newim.bean.BmobIMMessage;

/**
 * create by  wulee   2018/11/20 10:36
 * desc: 同意添加紧急联系人请求
 */
public class AgreeAddLinkmanMessage extends BmobIMExtraMessage {

    public static final String AGREE_LINKMAN = "agree_linkman";

    //以下均是从extra里面抽离出来的字段，方便获取
    private String uid;//最初的发送方
    private Long time;
    private String msg;//用于通知栏显示的内容

    @Override
    public String getMsgType() {
        return AGREE_LINKMAN;
    }

    @Override
    public boolean isTransient() {
        //如果需要在对方的会话表中新增一条该类型的消息，则设置为false，表明是非暂态会话
        //此处将同意添加好友的请求设置为false，为了演示怎样向会话表和消息表中新增一个类型
        return true;
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

    public AgreeAddLinkmanMessage(){}

    /**
     * 继承BmobIMMessage的属性
     * @param msg
     */
    private AgreeAddLinkmanMessage(BmobIMMessage msg){
        super.parse(msg);
    }

    /**将BmobIMMessage转成AgreeAddFriendMessage
     * @param msg 消息
     * @return
     */
    public static AgreeAddLinkmanMessage convert(BmobIMMessage msg){
        AgreeAddLinkmanMessage agree =new AgreeAddLinkmanMessage(msg);
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
                LogUtil.i("AgreeAddLinkmanMessage的extra为空");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return agree;
    }

}
