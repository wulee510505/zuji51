package com.wulee.administrator.zuji.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.app.hubert.guide.NewbieGuide;
import com.app.hubert.guide.model.GuidePage;
import com.facebook.stetho.common.LogUtil;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.adapter.TodayInHistoryAdapter;
import com.wulee.administrator.zuji.base.BaseActivity;
import com.wulee.administrator.zuji.entity.HistoryInfo;
import com.wulee.administrator.zuji.utils.CountDownHelper;
import com.wulee.administrator.zuji.utils.SortList;
import com.wulee.administrator.zuji.widget.BaseTitleLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.finalteam.okhttpfinal.BaseHttpRequestCallback;
import cn.finalteam.okhttpfinal.HttpRequest;
import okhttp3.Headers;

/**
 * Created by wulee on 2018/1/18 15:56
 * 历史的今天
 */

public class TodayInHistoryActivity extends BaseActivity {


    @InjectView(R.id.titlelayout)
    BaseTitleLayout titlelayout;
    @InjectView(R.id.recyclerview)
    EasyRecyclerView recyclerview;
    @InjectView(R.id.tv_count_down)
    TextView mTvCountDown;

    private TodayInHistoryAdapter mAdapter;

    private ArrayList<HistoryInfo> historyInfoList = new ArrayList<>();


    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.today_in_history_list_main);
        ButterKnife.inject(this);


        mAdapter = new TodayInHistoryAdapter(R.layout.today_in_history_list_item, historyInfoList);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        recyclerview.setAdapter(mAdapter);

        String url = "http://api.tianapi.com/txapi/lishi/?key=cbf5a516792c38950cf78e282369f453";
        HttpRequest.get(url, new BaseHttpRequestCallback() {
            //请求网络前
            @Override
            public void onStart() {
                showProgressDialog(false);
            }

            @Override
            public void onResponse(String response, Headers headers) {
                super.onResponse(response, headers);
                if (TextUtils.isEmpty(response)) {
                    return;
                }
                historyInfoList.clear();
                historyInfoList.addAll(jsonParse(response));
                mAdapter.setNewData(historyInfoList);
            }

            //请求失败（服务返回非法JSON、服务器异常、网络异常）
            @Override
            public void onFailure(int errorCode, String msg) {
                Toast.makeText(TodayInHistoryActivity.this, "网络异常~，请检查你的网络是否连接后再试", Toast.LENGTH_SHORT).show();
            }

            //请求网络结束
            @Override
            public void onFinish() {
                stopProgressDialog();
            }
        });

        CountDownHelper helper = new CountDownHelper(20, 1);
        helper.setCountDownListener(new CountDownHelper.OnCountDownListener() {
            @Override
            public void tick(int second) {
                mTvCountDown.setText(second + "s");
            }

            @Override
            public void finish() {
                TodayInHistoryActivity.this.finish();
                overridePendingTransition(R.anim.push_bottom_in, R.anim.push_bottom_out);
            }
        });
        helper.start();

        NewbieGuide.with(this)
                .setLabel("guide1")
                .addGuidePage(GuidePage.newInstance()
                        .addHighLight(mTvCountDown)
                        .setLayoutRes(R.layout.newguide_today_in_history))
                .show();
    }

    private List<HistoryInfo> jsonParse(String json) {
        try {
            List<HistoryInfo> historyInfoList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(json);
            int code = jsonObject.getInt("code");
            if (code == 200) {
                JSONArray jsonArray = jsonObject.getJSONArray("newslist");
                for (int i = 0; i < jsonArray.length(); i++) {
                    HistoryInfo historyInfo = new HistoryInfo();
                    JSONObject picData = jsonArray.getJSONObject(i);
                    String title = picData.getString("title");
                    String date = picData.optString("lsdate");

                    historyInfo.setTitle(title);
                    historyInfo.setLsdate(date);
                    historyInfoList.add(historyInfo);
                }
                SortList<HistoryInfo> sortList = new SortList<>();
                sortList.sortByMethod(historyInfoList, "getLsdate", true);//按消息时间倒序
                return historyInfoList;
            } else {
                Toast.makeText(TodayInHistoryActivity.this, "获取数据失败", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtil.e("TodayInHistoryActivity", "json解析出现了问题");
        }
        return new ArrayList<>();
    }

    @OnClick(R.id.tv_count_down)
    public void onViewClicked() {
        TodayInHistoryActivity.this.finish();
        overridePendingTransition(R.anim.push_bottom_in, R.anim.push_bottom_out);
    }
}
