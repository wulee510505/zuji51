package com.wulee.administrator.zuji.base;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.gyf.barlibrary.ImmersionBar;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.utils.OtherUtil;
import com.wulee.administrator.zuji.widget.BaseTitleLayout;
import com.wulee.administrator.zuji.widget.TitleLayoutClickListener;


public class ComWebActivity extends AppCompatActivity implements WebFragment.OnWebViewChangeListener,View.OnClickListener {

    private BaseTitleLayout titlelayout;

    private WebFragment mWebFragment;
    private ProgressBar mProgressBar;

    private int mBgTitleColorRes;
    private String url;
    private String title;

    protected ImmersionBar mImmersionBar;

    /**
     * 启动 Web 容器页面
     * @param from
     * @param url  URL 链接
     */
    public static void launch(@NonNull Activity from, @NonNull String url, String title, int bgTitleColorRes) {
        Intent intent = new Intent(from, ComWebActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("url", url);
        intent.putExtra("bgTitleColorRes", bgTitleColorRes);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        initView();
        initData();
        initImmersionBar(mBgTitleColorRes);
    }

    protected void initImmersionBar(int statusBarColor) {
        //在BaseActivity里初始化
        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.statusBarColor(statusBarColor);
        mImmersionBar.init();
    }


    private void initView() {
        titlelayout = (BaseTitleLayout) findViewById(R.id.titlelayout);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_web);

        titlelayout.setOnTitleClickListener(new TitleLayoutClickListener() {
            @Override
            public void onLeftClickListener() {
               finish();
            }
            @Override
            public void onRightImg1ClickListener() {
                OtherUtil.shareTextAndImage(ComWebActivity.this,title,title +"\n"+ url, null);
            }
        });
    }


    private void initData() {
        url = getIntent().getStringExtra("url");
        title = getIntent().getStringExtra("title");
        mBgTitleColorRes = getIntent().getIntExtra("bgTitleColorRes",-1);

        setTitle(title);
        titlelayout.setCenterText(title);

        mWebFragment = WebFragment.newInstance(url);
        mWebFragment.setListener(this);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.activity_web, mWebFragment);
        transaction.commit();
    }


    @Override
    public void onWebViewTitleChanged(String title) {
        setTitle(title);
    }

    @Override
    public void onWebViewProgressChanged(int newProgress) {
        if (newProgress >= 100) {
            mProgressBar.setVisibility(View.GONE);
        } else {
            if (mProgressBar.getVisibility() == View.GONE) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
            mProgressBar.setProgress(newProgress);
        }
    }

    @Override
    public void onBackPressed() {
        if (!mWebFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.iv_back) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mImmersionBar != null){
            //必须调用该方法，防止内存泄漏，不调用该方法，如果界面bar发生改变，在不关闭app的情况下，退出此界面再进入将记忆最后一次bar改变的状态
            mImmersionBar.destroy();
        }
    }
}
