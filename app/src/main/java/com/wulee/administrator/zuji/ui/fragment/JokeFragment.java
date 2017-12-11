package com.wulee.administrator.zuji.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.common.LogUtil;
import com.huxq17.swipecardsview.SwipeCardsView;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.adapter.FunPicAdapter;
import com.wulee.administrator.zuji.adapter.JokeAdapter;
import com.wulee.administrator.zuji.entity.Constant;
import com.wulee.administrator.zuji.entity.FunPicInfo;
import com.wulee.administrator.zuji.entity.JokeInfo;
import com.wulee.administrator.zuji.entity.JokeUrl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.finalteam.okhttpfinal.BaseHttpRequestCallback;
import cn.finalteam.okhttpfinal.HttpRequest;
import okhttp3.Headers;

import static com.wulee.administrator.zuji.App.aCache;

/**
 * Created by wulee on 2017/9/6 09:52
 */
public class JokeFragment extends MainBaseFrag {

    @InjectView(R.id.title_left)
    TextView titleLeft;
    @InjectView(R.id.title_right)
    TextView titleRight;
    @InjectView(R.id.titlelayout)
    RelativeLayout titlelayout;
    @InjectView(R.id.swipCardsView)
    SwipeCardsView swipCardsView;
    @InjectView(R.id.progress_bar)
    ProgressBar progressBar;
    private View mRootView;
    private RelativeLayout emptyView;

    private Context mContext;

    private JokeAdapter mAdapter;
    private FunPicAdapter mPicAdapter;

    private List<JokeInfo> mJokecDatas = new ArrayList<>();
    private List<FunPicInfo> mJokePicDatas = new ArrayList<>();

    private  final int TYPE_JOKE_TEXT = 2;
    private  final int TYPE_JOKE_PIC= 3;
    private  int jokeType = TYPE_JOKE_TEXT;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.joke_main, container, false);
        }
        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }
        ButterKnife.inject(this, mRootView);
        initView(mRootView);
        return mRootView;
    }

    private void initView(View view) {
        emptyView =  view.findViewById(R.id.emptyview);
        swipCardsView.retainLastCard(true);
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            HttpRequest.get((String) msg.obj, new BaseHttpRequestCallback() {
                //请求网络前
                @Override
                public void onStart() {
                    if(progressBar != null)
                       progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onResponse(String response, Headers headers) {
                    super.onResponse(response, headers);
                    if(TextUtils.isEmpty(response)){
                        return;
                    }
                    if(jokeType == TYPE_JOKE_TEXT){
                        mJokecDatas.clear();
                        mJokecDatas.addAll(jsonParse(response));
                        mAdapter = new JokeAdapter(mJokecDatas, mContext);
                        swipCardsView.setAdapter(mAdapter);
                    }else{
                        mJokePicDatas.clear();
                        mJokePicDatas.addAll(processJokePicInfo(jsonParse(response)));
                        mPicAdapter = new FunPicAdapter(mJokePicDatas, mContext);
                        swipCardsView.setAdapter(mPicAdapter);
                    }
                }

                //请求失败（服务返回非法JSON、服务器异常、网络异常）
                @Override
                public void onFailure(int errorCode, String msg) {
                    Toast.makeText(mContext, "网络异常~，请检查你的网络是否连接后再试", Toast.LENGTH_SHORT).show();
                }

                //请求网络结束
                @Override
                public void onFinish() {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    };


    private void  getJokeText() {
        final Message msg = mHandler.obtainMessage();

        final String[] jokeTextUrl = {""};

        String saveUrl = aCache.getAsString(Constant.KEY_JOKE_TEXT_URL);
        if(!TextUtils.isEmpty(saveUrl)){
            jokeTextUrl[0] =  saveUrl;

            msg.obj = jokeTextUrl[0];
            mHandler.sendMessage(msg);
        }else{
            BmobQuery<JokeUrl> query = new BmobQuery<>();
            query.findObjects(new FindListener<JokeUrl>() {
                @Override
                public void done(List<JokeUrl> list, BmobException e) {
                    if (e == null) {
                        if (list != null && list.size() > 0) {
                            String url = list.get(0).getUrl();
                            if (!TextUtils.isEmpty(url)){
                                jokeTextUrl[0] = url;
                                aCache.put(Constant.KEY_JOKE_TEXT_URL,url,Constant.JOKE_TEXT_OR_PIC_URL_SAVE_TIME);

                                msg.obj = jokeTextUrl[0];
                                mHandler.sendMessage(msg);
                            }
                        }
                    }
                }
            });
        }
    }

    private void  getJokePic() {
        final Message msg = mHandler.obtainMessage();

        final String[] jokePicUrl = {""};
        String saveUrl = aCache.getAsString(Constant.KEY_JOKE_PIC_URL);
        if(!TextUtils.isEmpty(saveUrl)){
            jokePicUrl[0] =  saveUrl;

            msg.obj = jokePicUrl[0];
            mHandler.sendMessage(msg);
        }else{
            BmobQuery<JokeUrl> query = new BmobQuery<>();
            query.findObjects(new FindListener<JokeUrl>() {
                @Override
                public void done(List<JokeUrl> list, BmobException e) {
                    if (e == null) {
                        if (list != null && list.size() > 0) {
                            String url = list.get(1).getUrl();
                            if (!TextUtils.isEmpty(url)){
                                jokePicUrl[0] = url;
                                aCache.put(Constant.KEY_JOKE_PIC_URL,url,Constant.JOKE_TEXT_OR_PIC_URL_SAVE_TIME);

                                msg.obj = jokePicUrl[0];
                                mHandler.sendMessage(msg);
                            }
                        }
                    }
                }
            });
        }
    }


    /**
     * 从网络中获取JSON字符串，然后解析
     *
     * @param json
     * @return
     */
    private List<JokeInfo> jsonParse(String json) {
        if(jokeType == TYPE_JOKE_PIC){
            try {
                List<JokeInfo> jokelist = new ArrayList<>();
                JSONObject jsonObject = new JSONObject(json);
                int errorCode = jsonObject.getInt("error_code");
                if (errorCode == 0) {
                    JSONArray jsonArray = jsonObject.getJSONArray("result");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        emptyView.setVisibility(View.GONE);
                        JokeInfo joke = new JokeInfo();
                        JSONObject picData = jsonArray.getJSONObject(i);
                        String id = picData.getString("hashId");
                        String content = picData.getString("content");
                        String url = picData.optString("url");

                        joke.setHashId(id);
                        joke.setContent(content);
                        joke.setUrl(url);
                        jokelist.add(joke);
                    }
                    return jokelist;
                } else {
                    emptyView.setVisibility(View.VISIBLE);
                    Toast.makeText(mContext, "获取数据失败", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                emptyView.setVisibility(View.VISIBLE);
                e.printStackTrace();
                LogUtil.e("JsonParseActivity", "json解析出现了问题");
            }
        }else  if(jokeType == TYPE_JOKE_TEXT){
            try {
                List<JokeInfo> jokelist = new ArrayList<>();
                JSONObject jsonObject = new JSONObject(json);
                int errorCode = jsonObject.getInt("error_code");
                if (errorCode == 0) {
                    JSONArray jsonArray = jsonObject.getJSONArray("result");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        emptyView.setVisibility(View.GONE);
                        JokeInfo joke = new JokeInfo();
                        JSONObject picData = jsonArray.getJSONObject(i);
                        String id = picData.getString("hashId");
                        String content = picData.getString("content");

                        joke.setHashId(id);
                        joke.setContent(content);
                        jokelist.add(joke);
                    }
                    return jokelist;
                } else {
                    emptyView.setVisibility(View.VISIBLE);
                    Toast.makeText(mContext, "获取数据失败", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                emptyView.setVisibility(View.VISIBLE);
                e.printStackTrace();
                LogUtil.e("JsonParseActivity", "json解析出现了问题");
            }
        }

        return new ArrayList<>();
    }

    private List<FunPicInfo>  processJokePicInfo(List<JokeInfo> list){
        List<FunPicInfo> jokePicDatas = new ArrayList<>();
        if(list != null && list.size()>0){
            for (int i = 0; i < list.size(); i++) {
                JokeInfo joke  = list.get(i);

                FunPicInfo picInfo = new FunPicInfo();
                picInfo.setUrl(joke.getUrl());
                picInfo.set_id(joke.getHashId());

                jokePicDatas.add(picInfo);
            }
        }
        return  jokePicDatas;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @OnClick({R.id.title_left, R.id.title_right})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.title_left:
                jokeType = TYPE_JOKE_TEXT;
                getJokeText();
                break;
            case R.id.title_right:
                jokeType = TYPE_JOKE_PIC;
                getJokePic();
                break;
        }
    }

    @Override
    public void onFragmentFirstSelected() {
          getJokeText();
    }
}
