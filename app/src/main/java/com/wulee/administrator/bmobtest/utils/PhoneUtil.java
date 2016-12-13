package com.wulee.administrator.bmobtest.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.wulee.administrator.bmobtest.App;

import java.util.UUID;

/**
 * Created by wulee on 2016/12/8 09:37
 */

public class PhoneUtil {
    /**
     * Role:获取当前设置的电话号码
     */
    public static String getNativePhoneNumber() {
        TelephonyManager telephonyManager  = (TelephonyManager) App.context.getSystemService(Context.TELEPHONY_SERVICE);
        String NativePhoneNumber = telephonyManager.getLine1Number();
        return NativePhoneNumber;
    }

    /**
     * 获取设备的串号
     */
    public static String getDeviceId() {
        TelephonyManager tm = (TelephonyManager)App.context.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        return deviceId;
    }


    /**
     * 获取手机型号
     */
    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机厂商
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 获得一个UUID
     * @return String UUID
     */
    public static String getUUID(){
        String s = UUID.randomUUID().toString();
        //去掉“-”符号
        return s.substring(0,8)+s.substring(9,13)+s.substring(14,18)+s.substring(19,23)+s.substring(24);
    }
}
