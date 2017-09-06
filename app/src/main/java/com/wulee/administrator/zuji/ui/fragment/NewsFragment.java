package com.wulee.administrator.zuji.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.jude.easyrecyclerview.EasyRecyclerView;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.adapter.NewsAdapter;
import com.wulee.administrator.zuji.entity.NewsInfo;
import com.wulee.administrator.zuji.utils.GsonUtil;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.finalteam.okhttpfinal.BaseHttpRequestCallback;
import cn.finalteam.okhttpfinal.HttpRequest;
import okhttp3.Headers;

/**
 * Created by wulee on 2017/9/6 09:52
 */
public class NewsFragment extends MainBaseFrag {

    @InjectView(R.id.rl_title)
    RelativeLayout rlTitle;
    @InjectView(R.id.recyclerview)
    EasyRecyclerView recyclerview;
    @InjectView(R.id.swipeLayout)
    SwipeRefreshLayout swipeLayout;
    @InjectView(R.id.progress_bar)
    ProgressBar progressBar;
    private View mRootView;
    private Context mContext;

    private boolean isRefresh = false;

    private NewsAdapter mAdapter;
    private  ArrayList<NewsInfo.NewsEntity> mDataList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.news_fragment, container, false);
        }
        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }
        ButterKnife.inject(this, mRootView);

        addListener();
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new NewsAdapter(R.layout.news_list_item,mDataList,mContext);
        recyclerview.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerview.setAdapter(mAdapter);
    }

    private void addListener() {
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefresh = true;
                getNews();
            }
        });

    }

    public void getNews() {
        String url = "http://api.tianapi.com/keji/?key=cbf5a516792c38950cf78e282369f453&num=10";
        HttpRequest.get(url,new BaseHttpRequestCallback() {
            //请求网络前
            @Override
            public void onStart() {
                if(!isRefresh)
                   progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(String response, Headers headers) {
                super.onResponse(response, headers);

                if(isRefresh)
                   swipeLayout.setRefreshing(false);

                NewsInfo newsInfo = GsonUtil.changeGsonToBean(response,NewsInfo.class);
                if(null != newsInfo){

                    mDataList.clear();
                    mDataList.addAll(newsInfo.getNewsEntityList());

                    mAdapter.setNewData(mDataList);
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

    @Override
    public void onFragmentFirstSelected() {
        getNews();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }


}
