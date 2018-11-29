package com.wulee.administrator.zuji;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * create by  wulee   2018/11/23 15:22
 */
public abstract  class Command<T> {
    // 只有一个线程的线程池
    private static ExecutorService sExecutorService = Executors.newSingleThreadExecutor();


    // 主线程消息队列的 Handler
    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    // 执行数据库操作
    public final void execute() {
        sExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    postResult(doInBackground());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 将结果投递到 UI 线程
    private void postResult(final T result) {
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                onPostExecute(result);
            }
        });
    }

    // 在后台执行的操作
    protected abstract T doInBackground();

    // 在 UI 线程执行的操作
    protected void onPostExecute(T result) {

    }
}
