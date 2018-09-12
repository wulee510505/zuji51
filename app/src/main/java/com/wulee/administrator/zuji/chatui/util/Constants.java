package com.wulee.administrator.zuji.chatui.util;

/**
 * 作者：Rance on 2016/12/20 16:51
 * 邮箱：rance935@163.com
 */
public class Constants {
    public static final String TAG = "rance";
    /** 0x001-接受消息  0x002-发送消息**/
    public static final int CHAT_ITEM_TYPE_LEFT = 0x001;
    public static final int CHAT_ITEM_TYPE_RIGHT = 0x002;
    /** 0x003-发送中  0x004-发送失败  0x005-发送成功**/
    public static final int CHAT_ITEM_SENDING = 0x003;
    public static final int CHAT_ITEM_SEND_ERROR = 0x004;
    public static final int CHAT_ITEM_SEND_SUCCESS = 0x005;

    //好友请求：未读-未添加->接收到别人发给我的好友添加请求，初始状态
    public static final int STATUS_VERIFY_NONE = 0;
    //好友请求：已读-未添加->点击查看了新朋友，则都变成已读状态
    public static final int STATUS_VERIFY_READED = 2;
    //好友请求：已添加
    public static final int STATUS_VERIFIED = 1;
    //好友请求：拒绝
    public static final int STATUS_VERIFY_REFUSE = 3;
    //好友请求：我发出的好友请求-暂未存储到本地数据库中
    public static final int STATUS_VERIFY_ME_SEND = 4;
}
