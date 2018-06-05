package com.wulee.administrator.zuji.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.wulee.administrator.zuji.R;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * create by  wulee   2018/5/9 09:56
 * desc:
 */

public class CustomScrollBar extends SurfaceView implements
        SurfaceHolder.Callback {

    private final String TAG = "CustomScrollBar";

    private SurfaceHolder holder;
    private Paint paint = null;// 画笔
    private boolean bStop = false; // 停止滚动

    private boolean clickEnable = false; // 可以点击
    private boolean isHorizontal = true; // 水平｜垂直
    private int speed = 1; // 滚动速度
    private String text = "";// 文本内容
    private float textSize = 15f; // 字号
    private int textColor = Color.BLACK; // 文字颜色
    private int times = Integer.MAX_VALUE; // 滚动次数

    private int viewWidth = 0;// 控件的长度
    private int viewHeight = 0; // 控件的高度
    private float textWidth = 0f;// 水平滚动时的文本长度
    private float textHeight = 0f; // 垂直滚动时的文本高度

    private float textX = 0f;// 文字的横坐标
    private float textY = 0f;// 文字的纵坐标
    private float viewWidth_plus_textLength = 0.0f;// 显示总长度
    private int time = 0; // 已滚动次数

    private ScheduledExecutorService scheduledExecutorService; // 执行滚动线程

    public CustomScrollBar(Context context) {
        super(context);
    }

    public CustomScrollBar(Context context, AttributeSet attrs) {
        //---------1
        super(context, attrs);
        holder = this.getHolder();
        holder.addCallback(this);
        paint = new Paint();

        //获取布局文件中的值
        TypedArray arr = getContext().obtainStyledAttributes(attrs,
                R.styleable.CustomScrollBar);
        clickEnable = arr.getBoolean(R.styleable.CustomScrollBar_clickEnable,
                clickEnable);
        isHorizontal = arr.getBoolean(R.styleable.CustomScrollBar_isHorizontal,
                isHorizontal);
        speed = arr.getInteger(R.styleable.CustomScrollBar_speed, speed);
        text = arr.getString(R.styleable.CustomScrollBar_text);
        textColor = arr.getColor(R.styleable.CustomScrollBar_textColor, textColor);
        textSize = arr.getDimension(R.styleable.CustomScrollBar_textSize, textSize);
        times = arr.getInteger(R.styleable.CustomScrollBar_times, times);

        time = times;
        paint.setColor(textColor);
        paint.setTextSize(textSize);

                   /*
                    * 下面两行代码配合draw()方法中的canvas.drawColor(Color.TRANSPARENT,Mode.CLEAR);
                    * 将画布填充为透明
                    */
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        setFocusable(true); // 设置焦点
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //----------2
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = View.MeasureSpec.getSize(widthMeasureSpec);//得到控件的宽度
        viewHeight = View.MeasureSpec.getSize(heightMeasureSpec);//得到控件的高度
        if (isHorizontal) { // 水平滚动
            textWidth = paint.measureText(text);// 获取text的长度
            viewWidth_plus_textLength = viewWidth + textWidth;
            textY = (viewHeight - getFontHeight(textSize)) / 2
                    + getPaddingTop() - getPaddingBottom() + 20;//向下偏移20
        } else { // 垂直滚动
            textHeight = getFontHeight(textSize) * text.length();// 获取text的长度
            viewWidth_plus_textLength = viewHeight + textHeight;
            textX = (viewWidth - textSize) / 2 + getPaddingLeft()
                    - getPaddingRight();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        //----------4
        Log.d(TAG, "surfaceChanged: ");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //-----------3
        bStop = false;
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new ScrollThread(), 1000,
                10, TimeUnit.MILLISECONDS);
        Log.d(TAG, "surfaceCreated: ");

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        bStop = true;
        scheduledExecutorService.shutdown();
        Log.d(TAG, "surfaceDestroyed: ");
    }

    // 获取字体高度
    private int getFontHeight(float fontSize) {
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) Math.ceil(fm.descent - fm.ascent);
    }

    private void setTimes(int times) {
        if (times <= 0) {
            this.times = Integer.MAX_VALUE;
        } else {
            this.times = times;
            time = times;
        }
    }

    private void setText(String text) {
        this.text = text;
    }

    private void setSpeed(int speed) {
        if (speed > 10 || speed < 0) {
            throw new IllegalArgumentException("Speed was invalid integer, it must between 0 and 10");
        } else {
            this.speed = speed;
        }
    }

    /**
     * 当屏幕被触摸时调用
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!clickEnable) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                bStop = !bStop;
                if (!bStop && time == 0) {
                    time = times;
                }
                break;
        }
        return true;
    }

    private synchronized void draw(float X, float Y) {
        Canvas canvas = holder.lockCanvas();//获取画布
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);// 通过清屏把画布填充为透明
        if (isHorizontal) { // 水平滚动
            canvas.drawText(text, X, Y, paint);
        } else { // 垂直滚动
            for (int i = 0; i < text.length(); i++) {
                canvas.drawText(text.charAt(i) + "", X, Y + (i + 1)
                        * getFontHeight(textSize), paint);
            }
        }
        holder.unlockCanvasAndPost(canvas);// 解锁画布，提交画好的图像
    }

    class ScrollThread implements Runnable {

        @Override
        public void run() {

            while (!bStop) {
                if (isHorizontal) {
                    draw(viewWidth - textX, textY);
                    textX += speed;// 速度设置：1-10
                    if (textX > viewWidth_plus_textLength) {
                        textX = 0;
                        --time;
                    }
                } else {
                    draw(textX, viewHeight - textY);
                    textY += speed;
                    if (textY > viewWidth_plus_textLength) {
                        textY = 0;
                        --time;
                    }
                }
                if (time <= 0) {
                    bStop = true;
                }
            }
        }
    }
}
