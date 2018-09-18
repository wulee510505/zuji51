package com.wulee.administrator.zuji.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.facebook.stetho.common.LogUtil;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.base.BaseActivity;
import com.wulee.administrator.zuji.utils.OtherUtil;

import awty.enr.pweu.nm.cm.ErrorCode;
import awty.enr.pweu.nm.vdo.VideoAdListener;
import awty.enr.pweu.nm.vdo.VideoAdManager;
import awty.enr.pweu.nm.vdo.VideoAdRequestListener;
import awty.enr.pweu.nm.vdo.VideoAdSettings;

/**
 * 原生视频广告演示窗口
 *
 * @author Alian Lee
 * @since 2016-11-25
 */
public class NativeVideoAdActivity extends BaseActivity {
	
	/**
	 * 原生视频广告控件容器
	 */
	private RelativeLayout mNativeVideoAdLayout;
	
	/**
	 * 展示原生视频广告按钮
	 */
	private Button mBtnShowNativeVideoAd;

	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_native_video);

		mContext = NativeVideoAdActivity.this;

		// 初始化视图
		initView();
		// 设置原生视频广告
		setupNativeVideoAd();
	}
	
	/**
	 * 初始化视图
	 */
	private void initView() {
		// 原生视频广告控件容器
		mNativeVideoAdLayout = (RelativeLayout) findViewById(R.id.rl_native_video_ad);
		// 展示原生视频广告按钮
		mBtnShowNativeVideoAd = (Button) findViewById(R.id.btn_show_native_video);
	}
	
	/**
	 * 设置原生视频广告
	 */
	private void setupNativeVideoAd() {
		// 设置视频广告
		final VideoAdSettings videoAdSettings = new VideoAdSettings();
		// 只需要调用一次，由于在主页窗口中已经调用了一次，所以此处无需调用
		VideoAdManager.getInstance(mContext).requestVideoAd(mContext, new VideoAdRequestListener() {
			@Override
			public void onRequestSuccess() {
				LogUtil.d("请求视频广告成功");
			}
			@Override
			public void onRequestFailed(int errorCode) {
				LogUtil.e("请求视频广告失败，errorCode: %s", errorCode);
				switch (errorCode){
					case ErrorCode.NON_NETWORK:
						OtherUtil.showToastText("网络异常");
						break;
					case ErrorCode.NON_AD:
						OtherUtil.showToastText("暂无视频广告");
						break;
					default:
						OtherUtil.showToastText("请稍后再试");
						break;
				}
			}
		});
		mBtnShowNativeVideoAd.setOnClickListener(v -> {
            // 注意：请在UI线程调用该方法
            // 获取原生视频控件
            View nativeVideoAdView = VideoAdManager.getInstance(mContext)
                                                            .getNativeVideoAdView(mContext,
                                                                    videoAdSettings,
                                                                    new VideoAdListener() {
                                                                        @Override
                                                                        public void onPlayStarted() {
                                                                            LogUtil.e("开始播放视频");
                                                                            // 由于多窗口模式下，屏幕较小，所以开始播放时先隐藏展示按钮
                                                                            if (Build.VERSION.SDK_INT >= 24) {
                                                                                if (isInMultiWindowMode()) {
                                                                                    hideShowNativeVideoButton();
                                                                                }
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onPlayInterrupted() {
                                                                            OtherUtil.showToastText("播放视频被中断");
                                                                            // 中断播放时恢复展示原生视频广告按钮
                                                                            showShowNativeVideoButton();
                                                                            // 移除原生视频控件
                                                                            if (mNativeVideoAdLayout != null) {
                                                                                mNativeVideoAdLayout.removeAllViews();
                                                                                mNativeVideoAdLayout.setVisibility(View
                                                                                        .GONE);
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onPlayFailed(int errorCode) {
                                                                            LogUtil.e("视频播放失败");
                                                                            switch (errorCode) {
                                                                            case ErrorCode.NON_NETWORK:
																				OtherUtil.showToastText("网络异常");
                                                                                break;
                                                                            case ErrorCode.NON_AD:
																				OtherUtil.showToastText("原生视频暂无广告");
                                                                                break;
                                                                            case ErrorCode.RESOURCE_NOT_READY:
																				OtherUtil.showToastText("原生视频资源还没准备好");
                                                                                break;
                                                                            case ErrorCode.SHOW_INTERVAL_LIMITED:
																				OtherUtil.showToastText("请勿频繁展示");
                                                                                break;
                                                                            case ErrorCode
                                                                                    .WIDGET_NOT_IN_VISIBILITY_STATE:
																				OtherUtil.showToastText("原生视频控件处在不可见状态");
                                                                                break;
                                                                            default:
                                                                                LogUtil.e("请稍后再试");
                                                                                break;
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onPlayCompleted() {
																			OtherUtil.showToastText("视频播放成功");
                                                                            // 播放完成时恢复展示原生视频广告按钮
                                                                            showShowNativeVideoButton();
                                                                            // 移除原生视频控件
                                                                            if (mNativeVideoAdLayout != null) {
                                                                                mNativeVideoAdLayout.removeAllViews();
                                                                                mNativeVideoAdLayout.setVisibility(View
                                                                                        .GONE);
                                                                            }
                                                                        }

                                                                    }
                                                            );
            if (mNativeVideoAdLayout != null) {
                final RelativeLayout.LayoutParams params =
                        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                if (nativeVideoAdView != null) {
                    mNativeVideoAdLayout.removeAllViews();
                    // 添加原生视频广告
                    mNativeVideoAdLayout.addView(nativeVideoAdView, params);
                    mNativeVideoAdLayout.setVisibility(View.VISIBLE);
                }
            }
        });
	}
	
	@Override
	public void onBackPressed() {
		//原生控件点击后退关闭
		if (mNativeVideoAdLayout != null && mNativeVideoAdLayout.getVisibility() != View.GONE) {
			mNativeVideoAdLayout.removeAllViews();
			mNativeVideoAdLayout.setVisibility(View.GONE);
			return;
		}
		super.onBackPressed();
	}
	
	//-----------------------必须调用以下全部生命周期方法-------------------------------
	
	@Override
	protected void onStart() {
		super.onStart();
		// 原生视频广告
		VideoAdManager.getInstance(mContext).onStart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// 原生视频广告
		VideoAdManager.getInstance(mContext).onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		// 原生视频广告
		VideoAdManager.getInstance(mContext).onPause();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		// 原生视频广告
		VideoAdManager.getInstance(mContext).onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 原生视频广告
		VideoAdManager.getInstance(mContext).onDestroy();
	}
	
	//-----------------------必须调用以上全部生命周期方法-------------------------------
	
	/**
	 * 隐藏展示原生视频广告按钮
	 */
	private void hideShowNativeVideoButton() {
		if (mBtnShowNativeVideoAd != null && mBtnShowNativeVideoAd.getVisibility() != View.GONE) {
			mBtnShowNativeVideoAd.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 展示展示原生视频广告按钮
	 */
	private void showShowNativeVideoButton() {
		if (mBtnShowNativeVideoAd != null && mBtnShowNativeVideoAd.getVisibility() != View.VISIBLE) {
			mBtnShowNativeVideoAd.setVisibility(View.VISIBLE);
		}
	}

}
