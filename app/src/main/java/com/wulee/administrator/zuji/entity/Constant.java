package com.wulee.administrator.zuji.entity;

import com.wulee.administrator.zuji.utils.SDCardUtils;

/**
 * Created by wulee on 2017/5/22 16:08
 */

public interface Constant {

    String ROOT_PATH = String.format("%s%s", SDCardUtils.getESDString(), "/zuji/");// 根目录
    String LOG_PATH = String.format("%slog/", ROOT_PATH);// 日志目录
    String AVATAR_PATH = String.format("%savatar/", ROOT_PATH);// 头像目录
    String CRASH_PATH = String.format("%scrash/", ROOT_PATH);// 异常信息的目录
    String SAVE_PIC = String.format("%ssavepic/", ROOT_PATH);// 图片保存的目录
    String SAVE_AUDIO = String.format("%ssaveaudio/", ROOT_PATH);// 音频保存的目录


    String TEMP_FILE_PATH = String.format("%stemp/", ROOT_PATH);// 临时文件存放的目录

    String BOMB_APP_ID = "ac67374a92fdca635c75eb6388e217a4";

    String YOUMI_APP_ID = "403ee7dca08bf6fc";
    String YOUMI_SECRET = "0959c0f3e3f38b63";

    String KEY_SPLASH_PIC_URL = "key_splash_pic_url";
    int SPLASH_PIC_URL_SAVE_TIME =  60 * 60 * 1; //1小时


    String KEY_JOKE_TEXT_URL = "key_joke_text_url";
    String KEY_JOKE_PIC_URL = "key_joke_pic_url";

    int JOKE_TEXT_OR_PIC_URL_SAVE_TIME =  24 * 60 * 60 * 1000; //24小时


    String KEY_LAST_UPDATE_CURR_PERSONINFO_TIME = "key_last_update_curr_personinfo_time";
    long UPDATE_CURR_PERSONINFO_INTERVAL = 5 * 60 * 1000;// 5分钟


    String KEY_LAST_CHECK_UPDATE_TIME = "key_last_check_update_time";
    long CHECK_UPDATE_INTERVAL = 10 * 60 * 1000;// 10分钟


    String KEY_LAST_SHOW_NOTICE_TIME = "key_last_show_notice_time";
    long SHOW_NOTICE_INTERVAL =  2 * 60 * 60 * 1000;// 2小时

    String KEY_SIGN_DATE= "key_sign_date";

    String KEY_CURR_SERVER_TIME = "key_curr_server_time";

    // 日志过期时间，默认为10天
    int LOG_EXPIRED_TIME = 10;


    //是否是debug模式
    boolean DEBUG = true;
    //好友请求：未读-未添加->接收到别人发给我的好友添加请求，初始状态
    int STATUS_VERIFY_NONE = 0;
    //好友请求：已读-未添加->点击查看了新朋友，则都变成已读状态
    int STATUS_VERIFY_READED = 2;
    //好友请求：已添加
    int STATUS_VERIFIED = 1;
    //好友请求：拒绝
   int STATUS_VERIFY_REFUSE = 3;
    //好友请求：我发出的好友请求-暂未存储到本地数据库中
    int STATUS_VERIFY_ME_SEND = 4;
}
