package com.wulee.administrator.zuji.utils;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wulee.administrator.zuji.App;
import com.wulee.administrator.zuji.R;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.wulee.administrator.zuji.App.aCache;

/**
 * Created by wulee on 2017/4/25 10:40
 */

public class OtherUtil {
    /**
     * 密码校验
     * 要求6-16位数字和英文字母组合
     * @param pwd
     * @return
     */
    public static boolean isPassword(String pwd) {
        /**
         * ^ 匹配一行的开头位置(?![0-9]+$) 预测该位置后面不全是数字
         * (?![a-zA-Z]+$) 预测该位置后面不全是字母
         * [0-9A-Za-z] {6,16} 由6-16位数字或这字母组成
         */
        return validation("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$", pwd);
    }

    /**
     * 手机号校验 注：1.支持最新170手机号码 2.支持+86校验
     * @param phoneNum
     * 手机号码
     * @return 验证通过返回true
     */
    public static boolean isMobile(String phoneNum) {
        if (phoneNum == null)
            return false;
        // 如果手机中有+86则会自动替换掉
        return validation("^[1][3,4,5,7,8][0-9]{9}$",
                phoneNum.replace("+86", ""));
    }

    /**
     * 正则校验
     * @param str
     * 需要校验的字符串
     * @return 验证通过返回true
     */
    public static boolean validation(String pattern, String str) {
        if (str == null)
            return false;
        return Pattern.compile(pattern).matcher(str).matches();
    }

    /**
     * 判断两个double类型值是否相等
     * @param num1
     * @param num2
     * @return
     */
    public static boolean equal(double num1,double num2) {
        if((num1-num2 >-0.000001)&&(num1-num2)<0.000001)
            return true;
        else
            return false;
    }

    /**
     * 判断是否登录
     * @return
     */
    public static boolean hasLogin(){
        boolean isLogin = false;
        if(TextUtils.equals("yes",aCache.getAsString("has_login"))){
            isLogin = true;
        }else{
            isLogin = false;
        }
        return isLogin;
    }


    public static void showToastText(String msg) {
        if (App.context != null) {
            showNewToast(App.context, 0, msg, Toast.LENGTH_LONG);
        }
    }

    private static Toast mToast;

    /**
     * 弹出Toast
     *
     * @param context
     * @param resId
     * @param text     提示内容
     * @param duration
     */
    private static void showNewToast(Context context, int resId, CharSequence text, int duration) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.custom_toast, null);
        ImageView imageView = (ImageView) layout.findViewById(R.id.iv_icon);
        TextView textView = (TextView) layout.findViewById(R.id.tv_content);
        if (resId > 0) {
            imageView.setVisibility(View.VISIBLE);
            imageView.setBackgroundResource(resId);
        } else {
            imageView.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        } else {
            textView.setText("");
        }
        if (null == mToast) {
            mToast = new Toast(context);
        }
        mToast.setView(layout);
        mToast.setDuration(duration);
        mToast.show();
    }

    /**
     * 判断字符串是否为null或全为空格
     * @param s 待校验字符串
     * @return {@code true}: null或全空格<br> {@code false}: 不为null且不全空格
     */
    public static boolean isSpace(String s) {
        return (s == null || s.trim().length() == 0);
    }

    /**
     * 关闭IO
     *
     * @param closeables closeables
     */
    public static void closeIO(Closeable... closeables) {
        if (closeables == null) return;
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 实现文本复制功能
     * @param content
     */
    public static  void copy(String content, Context context){
        // 得到剪贴板管理器
        ClipboardManager cmb = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content.trim());
    }


    /**
     * 分享功能
     *
     * @param context
     *            上下文
     * @param msgTitle
     *            消息标题
     * @param msgText
     *            消息内容
     * @param imgPath
     *            图片路径，不分享图片则传null
     */
    public static void shareTextAndImage(Context context, String msgTitle, String msgText, String imgPath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (imgPath == null || imgPath.equals("")) {
            intent.setType("text/plain"); // 纯文本
        } else {
            File f = new File(imgPath);
            if (f != null && f.exists() && f.isFile()) {
                intent.setType("image/jpg");
                Uri u = FileProvider7.getUriForFile(context,f);
                intent.putExtra(Intent.EXTRA_STREAM, u);
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, msgText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(intent, "分享到"));
    }

    /**
     * @param filename 文件全名，包括后缀哦
     */
    public static void updateGallery(Context context,String filename) {
        MediaScannerConnection.scanFile(context, new String[] { filename }, null,
                (path, uri) -> {
                    Log.i("ExternalStorage", "Scanned " + path + ":");
                    Log.i("ExternalStorage", "-> uri=" + uri);
                });
    }

    /**
     * 应用内多语言切换
     * @param context
     * @param language
     */
    public static void changeSystemLanguage(Context context, String language) {
        if (context == null || TextUtils.isEmpty(language)) {
            return;
        }
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        if (Locale.SIMPLIFIED_CHINESE.getLanguage().equals(language)) {
            config.locale = Locale.SIMPLIFIED_CHINESE;
        } else {
            config.locale = new Locale(language);
        }
        resources.updateConfiguration(config, null);
    }
}
