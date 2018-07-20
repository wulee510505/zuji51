package com.wulee.administrator.zuji.ui;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.stetho.common.LogUtil;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.base.BaseActivity;
import com.wulee.administrator.zuji.database.bean.PersonInfo;
import com.wulee.administrator.zuji.entity.Constant;
import com.wulee.administrator.zuji.entity.SplashPic;
import com.wulee.administrator.zuji.utils.ImageUtil;
import com.wulee.administrator.zuji.utils.OtherUtil;
import com.wulee.administrator.zuji.widget.FadeInTextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import awty.enr.pweu.AdManager;
import awty.enr.pweu.nm.cm.ErrorCode;
import awty.enr.pweu.nm.sp.SplashViewSettings;
import awty.enr.pweu.nm.sp.SpotListener;
import awty.enr.pweu.nm.sp.SpotManager;
import awty.enr.pweu.nm.sp.SpotRequestListener;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.listener.ConnectListener;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;

import static com.wulee.administrator.zuji.App.aCache;


/**
 * Created by wulee on 2016/8/17.
 */

public class SplashActivity extends BaseActivity implements View.OnClickListener{

    private FadeInTextView mFadeInTextView;
    private View startView = null;
    private ImageView ivSplash;
    private AlphaAnimation loadAlphaAnimation=null;
    private ScaleAnimation loadScaleAnimation = null;
    private TextView btnSkip;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        startView = View.inflate(this, R.layout.splash, null);
        setContentView(startView);

        ivSplash = (ImageView) findViewById(R.id.iv_splash_bg);
        btnSkip= (TextView) findViewById(R.id.tv_skip);
        btnSkip.setOnClickListener(this);
        mFadeInTextView = (FadeInTextView) findViewById(R.id.fadeInTextView);
        mFadeInTextView.setDuration(150);

        //initData();
        syncServerDate();
        runApp();
    }

    private void runApp() {
        //初始化SDK
        AdManager.getInstance(this).init(Constant.YOUMI_APP_ID, Constant.YOUMI_SECRET, true);
        preloadAd();
        setupSplashAd(); // 如果需要首次展示开屏，请注释掉本句代码
    }
    /**
     * 预加载广告
     */
    private void preloadAd() {
        // 注意：不必每次展示插播广告前都请求，只需在应用启动时请求一次
        SpotManager.getInstance(this).requestSpot(new SpotRequestListener() {
            @Override
            public void onRequestSuccess() {
                LogUtil.d("请求插播广告成功");
                // 应用安装后首次展示开屏会因为本地没有数据而跳过
                // 如果开发者需要在首次也能展示开屏，可以在请求广告成功之前展示应用的logo，请求成功后再加载开屏
                setupSplashAd();
            }

            @Override
            public void onRequestFailed(int errorCode) {
                LogUtil.e("请求插播广告失败，errorCode: %s", errorCode);
                switch (errorCode) {
                    case ErrorCode.NON_NETWORK:
                        LogUtil.e("网络异常");
                        break;
                    case ErrorCode.NON_AD:
                        LogUtil.e("暂无视频广告");
                        break;
                    default:
                        LogUtil.e("请稍后再试");
                        break;
                }
            }
        });
    }

    /**
     * 设置开屏广告
     */
    private void setupSplashAd() {
        // 创建开屏容器
        final RelativeLayout splashLayout = (RelativeLayout) findViewById(R.id.rl_splash);
        // 对开屏进行设置
        SplashViewSettings splashViewSettings = new SplashViewSettings();
        // 设置是否展示失败自动跳转，默认自动跳转
        //splashViewSettings.setAutoJumpToTargetWhenShowFailed(false);
        // 设置跳转的窗口类
        if(OtherUtil.hasLogin()){
            splashViewSettings.setTargetClass(MainNewActivity.class);
        } else{
            splashViewSettings.setTargetClass(LoginActivity.class);
        }
        // 设置开屏的容器
        splashViewSettings.setSplashViewContainer(splashLayout);

        // 展示开屏广告
        SpotManager.getInstance(this).showSplash(this, splashViewSettings, new SpotListener() {
                    @Override
                    public void onShowSuccess() {
                        LogUtil.d("开屏展示成功");
                    }

                    @Override
                    public void onShowFailed(int errorCode) {
                        LogUtil.e("开屏展示失败");
                        switch (errorCode) {
                            case ErrorCode.NON_NETWORK:
                                LogUtil.e("网络异常");
                                break;
                            case ErrorCode.NON_AD:
                                LogUtil.e("暂无开屏广告");
                                break;
                            case ErrorCode.RESOURCE_NOT_READY:
                                LogUtil.e("开屏资源还没准备好");
                                break;
                            case ErrorCode.SHOW_INTERVAL_LIMITED:
                                LogUtil.e("开屏展示间隔限制");
                                break;
                            case ErrorCode.WIDGET_NOT_IN_VISIBILITY_STATE:
                                LogUtil.e("开屏控件处在不可见状态");
                                break;
                            default:
                                LogUtil.e("errorCode: %d", errorCode);
                                break;
                        }
                    }
                    @Override
                    public void onSpotClosed() {
                        LogUtil.d("开屏被关闭");
                    }
                    @Override
                    public void onSpotClicked(boolean isWebPage) {
                        LogUtil.d("开屏被点击");
                        LogUtil.d("是否是网页广告？%s", isWebPage ? "是" : "不是");
                    }
                });
    }

    private void syncServerDate() {
        Bmob.getServerTime(new QueryListener<Long>() {
            @Override
            public void done(Long time, BmobException e) {
                if(e == null){
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    final String date = formatter.format(new Date(time * 1000L));
                    LogUtil.i("bmob","当前服务器时间为:" + date);
                    aCache.put(Constant.KEY_CURR_SERVER_TIME,date);
                }
            }
        });
    }

    private void initData() {
        loadPage();

        String url = aCache.getAsString(Constant.KEY_SPLASH_PIC_URL);
        if(!TextUtils.isEmpty(url)){
            ImageUtil.setDefaultImageView(ivSplash,url,R.mipmap.bg_wellcome,SplashActivity.this);
        }else{
            BmobQuery<SplashPic>  query  = new BmobQuery<>();
            query.findObjects(new FindListener<SplashPic>() {
                @Override
                public void done(List<SplashPic> list, BmobException e) {
                    if(e == null){
                        if(list != null && list.size()>0){
                            int index = 3/*(int)( Math.random()* (4))*/ ; //生成 0、1、2 、3 随机数
                            SplashPic splashPic = list.get(index);
                            if(null != splashPic && !TextUtils.isEmpty(splashPic.getUrl())){
                                ImageUtil.setDefaultImageView(ivSplash,splashPic.getUrl(),R.mipmap.bg_wellcome,SplashActivity.this);
                                aCache.put(Constant.KEY_SPLASH_PIC_URL,splashPic.getUrl(),Constant.SPLASH_PIC_URL_SAVE_TIME);
                            }
                        }
                    }
                }
            });
        }
    }

    private void loadPage() {
        AnimationSet animationSet =new AnimationSet(true);

        loadAlphaAnimation = new AlphaAnimation(0.3f, 1.0f);
        loadScaleAnimation =  new ScaleAnimation(1f, 1.2f, 1f, 1.2f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        loadAlphaAnimation.setDuration(3000);
        loadScaleAnimation.setDuration(6000);
        animationSet.addAnimation(loadAlphaAnimation);
        //animationSet.addAnimation(loadScaleAnimation);
        startView.setAnimation(animationSet);
        animationSet.setInterpolator(new AccelerateInterpolator());
        animationSet.startNow();

        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                mFadeInTextView
                        .setTextString("一款可以记录并查看出行轨迹的工具类软件")
                        .setTextAnimationListener(() -> startActivity());
                mFadeInTextView.startFadeInAnimation();
            }
        });
    }

    private void startActivity() {
        final Intent intent;
        if(OtherUtil.hasLogin()){
             intent = new Intent(SplashActivity.this, MainNewActivity.class);
             //connectIMServer();
        } else{
             intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_skip:
                mFadeInTextView.stopFadeInAnimation();
                startActivity();
             break;
        }
    }

    private void connectIMServer() {
        PersonInfo piInfo = BmobUser.getCurrentUser(PersonInfo.class);
        if (!TextUtils.isEmpty(piInfo.getObjectId())) {
            BmobIM.connect(piInfo.getObjectId(), new ConnectListener() {
                @Override
                public void done(String uid, BmobException e) {
                    if (e == null) {
                        //连接成功
                        LogUtil.i("连接成功");
                    } else {
                        //连接失败
                        LogUtil.i("连接失败-----》"+ e.getMessage());
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 开屏展示界面的 onDestroy() 回调方法中调用
        SpotManager.getInstance(this).onDestroy();
    }
}
