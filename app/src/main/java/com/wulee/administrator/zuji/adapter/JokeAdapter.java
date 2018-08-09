package com.wulee.administrator.zuji.adapter;

import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huxq17.swipecardsview.BaseCardAdapter;
import com.liangmayong.text2speech.OnText2SpeechListener;
import com.liangmayong.text2speech.Text2Speech;
import com.nineoldandroids.animation.ValueAnimator;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.entity.JokeInfo;
import com.wulee.administrator.zuji.utils.NoFastClickUtils;
import com.wulee.administrator.zuji.utils.OtherUtil;

import java.util.List;

/**
 * Created by wulee on 2017/9/4 16:23
 */

public class JokeAdapter extends BaseCardAdapter {
    private List<JokeInfo> datas;
    private Context context;
    private OnText2SpeechListener mSpeechListener;

    public JokeAdapter(List<JokeInfo> datas, Context context) {
        this.datas = datas;
        this.context = context;
    }

    public void setData(List<JokeInfo> datas) {
        this.datas = datas;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public int getCardLayoutId() {
        return R.layout.joke_item;
    }

    @Override
    public void onBindData(final int position, View cardview) {
        if (datas == null || datas.size() == 0) {
            return;
        }
        final TextView tvJoke =  cardview.findViewById(R.id.tv_joke);
        TextView tvCpoy =  cardview.findViewById(R.id.tv_copy);
        TextView tvShare =  cardview.findViewById(R.id.tv_share);
        ImageView ivPlay =  cardview.findViewById(R.id.iv_play_joke);

        final JokeInfo joke = datas.get(position);
        tvJoke.setText(joke.getContent());

        ivPlay.setOnClickListener(view -> {
            if(NoFastClickUtils.isFastClick()) {
                return;
            }
            if(Text2Speech.isSpeeching()){
                Text2Speech.pause(context);
            }
            final ScaleAnimation animation = new ScaleAnimation(1f, 1.2f, 1f, 1.2f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);
            animation.setDuration(500);
            animation.setRepeatCount(ValueAnimator.INFINITE);
            animation.setRepeatMode(ValueAnimator.INFINITE);
            animation.setInterpolator(new AccelerateInterpolator());

            Text2Speech.speech(context, joke.getContent(), true);
            if(mSpeechListener == null){
                mSpeechListener = new OnText2SpeechListener() {
                    @Override
                    public void onCompletion() {
                        animation.cancel();
                    }
                    @Override
                    public void onPrepared() {
                        ivPlay.startAnimation(animation);
                    }
                    @Override
                    public void onError(Exception e, String s) {
                        animation.cancel();
                    }

                    @Override
                    public void onStart() {
                    }
                    @Override
                    public void onLoadProgress(int i, int i1) {
                    }
                    @Override
                    public void onPlayProgress(int i, int i1) {

                    }
                };
            }
            Text2Speech.setOnText2SpeechListener(mSpeechListener);
        });

        tvCpoy.setOnClickListener(view -> {
            OtherUtil.copy(joke.getContent(),context);
            Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show();
        });
        tvShare.setOnClickListener(v -> {
            OtherUtil.shareTextAndImage(context,"",joke.getContent(),null);
        });

    }

    /**
     * 如果可见的卡片数是3，则可以不用实现这个方法
     * @return
     */
    @Override
    public int getVisibleCardCount() {
        return super.getVisibleCardCount();
    }


    public  void stopText2Speech(){
        if(Text2Speech.isSpeeching()){
            Text2Speech.shutUp(context);
        }
        Text2Speech.removeText2SpeechListener(mSpeechListener);
        mSpeechListener = null;
    }
}
