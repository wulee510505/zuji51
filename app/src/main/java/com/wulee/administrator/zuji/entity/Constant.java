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
    String SAVE_PIC = String.format("%ssavepic/", ROOT_PATH);// 异常信息的目录

    String TEMP_FILE_PATH = String.format("%stemp/", ROOT_PATH);// 临时文件存放的目录

    String BOMB_APP_ID = "ac67374a92fdca635c75eb6388e217a4";


    String KEY_SPLASH_PIC_URL = "key_splash_pic_url";
    int SPLASH_PIC_URL_SAVE_TIME =  60 * 60 * 1; //1小时


    String KEY_JOKE_TEXT_URL = "key_joke_text_url";
    String KEY_JOKE_PIC_URL = "key_joke_pic_url";

    int JOKE_TEXT_OR_PIC_URL_SAVE_TIME =  /*60 * 60 * 24 **/ 15; //1天

    // 日志过期时间，默认为10天
    int LOG_EXPIRED_TIME = 10;
}
