package com.wulee.administrator.bmobtest;

import android.app.Application;
import android.content.Context;

/**
 * Created by wulee on 2016/12/8 09:37
 */

public class App extends Application {

   public static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
}
