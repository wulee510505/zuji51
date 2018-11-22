package com.wulee.administrator.zuji.chatui.enity;

import android.text.TextUtils;

import com.app.hubert.guide.util.LogUtil;
import com.wulee.administrator.zuji.entity.Constant;
import com.wulee.administrator.zuji.entity.LinkmanInfo;

import org.json.JSONObject;

import cn.bmob.newim.bean.BmobIMExtraMessage;
import cn.bmob.newim.bean.BmobIMMessage;

/**
 * create by  wulee   2018/11/20 09:35
 * desc:添加紧急联系人-自定义消息类型
 */
public class AddLinkmanMessage extends BmobIMExtraMessage {

    public static final String ADD_LINKMAN = "add_linkman";

    public AddLinkmanMessage() {
    }

    /**
     * 将BmobIMMessage转成LinkmanInfo
     * @param msg 消息
     * @return
     */
    public static LinkmanInfo convert(BmobIMMessage msg) {
        LinkmanInfo add = new LinkmanInfo();
        String content = msg.getContent();
        add.setMsg(content);
        add.setTime(msg.getCreateTime());
        add.setStatus(Constant.STATUS_VERIFY_NONE);
        try {
            String extra = msg.getExtra();
            if (!TextUtils.isEmpty(extra)) {
                JSONObject json = new JSONObject(extra);
                String name = json.getString("name");
                add.setName(name);
                String avatar = json.getString("avatar");
                add.setAvatar(avatar);
                add.setUid(json.getString("uid"));
            } else {
                LogUtil.i("AddLinkmanMessage的extra为空");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return add;
    }


    @Override
    public String getMsgType() {
        return ADD_LINKMAN;
    }

    @Override
    public boolean isTransient() {
        //设置为true,表明为暂态消息，那么这条消息并不会保存到本地db中，SDK只负责发送出去
        //设置为false,则会保存到指定会话的数据库中
        return true;
    }

}
