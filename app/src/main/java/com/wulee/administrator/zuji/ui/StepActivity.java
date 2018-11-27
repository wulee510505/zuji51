package com.wulee.administrator.zuji.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.stetho.common.LogUtil;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.adapter.StepRankingAdapter;
import com.wulee.administrator.zuji.base.BaseActivity;
import com.wulee.administrator.zuji.database.bean.PersonInfo;
import com.wulee.administrator.zuji.entity.StepInfo;
import com.wulee.administrator.zuji.utils.DateTimeUtils;
import com.wulee.administrator.zuji.utils.Pedometer;
import com.wulee.administrator.zuji.utils.SortList;
import com.wulee.administrator.zuji.widget.ProgressWheel;
import com.wulee.administrator.zuji.widget.RecycleViewDivider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;


public class StepActivity extends BaseActivity {
    public static final String ACTION_ON_STEP_COUNT_CHANGE = "action_on_step_count_change";
    @InjectView(R.id.iv_back)
    ImageView ivBack;
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.tv_ranking)
    TextView tvRanking;
    @InjectView(R.id.tv_line)
    View tvLine;
    @InjectView(R.id.recyclerview)
    EasyRecyclerView recyclerview;
    @InjectView(R.id.swipeLayout)
    SwipeRefreshLayout swipeLayout;
    @InjectView(R.id.iv_history)
    ImageView ivHistory;
    @InjectView(R.id.progress_step)
    ProgressWheel progressStep;

    private Pedometer pedometer;
    private OnStepCountChangeReceiver mReceiver;

    private StepRankingAdapter mAdapter;
    private List<StepInfo> mDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step);
        ButterKnife.inject(this);

        initData();
        addListener();

        mReceiver = new OnStepCountChangeReceiver();
        IntentFilter filter = new IntentFilter(ACTION_ON_STEP_COUNT_CHANGE);
        registerReceiver(mReceiver, filter);

        pedometer = new Pedometer(this);
    }


    private void initData() {
        title.setText("今日步数");

        swipeLayout.setColorSchemeResources(R.color.left_menu_bg,R.color.colorAccent);
        mAdapter = new StepRankingAdapter(this, R.layout.step_rank_list_item, mDataList);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        recyclerview.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL));
        recyclerview.setAdapter(mAdapter);

        deleteStepInfo();

        queryStepRankList(true);
    }

    private void addListener() {
        swipeLayout.setOnRefreshListener(() -> queryStepRankList(false));
    }

    private void queryStepRankList(boolean showProgressBar) {
        BmobQuery<StepInfo> query = new BmobQuery<StepInfo>();
        query.include("personInfo");// 希望在查询计步信息的同时也把当前用户的信息查询出来
        String currdate = DateTimeUtils.formatTime(new Date());

        List<BmobQuery<StepInfo>> and = new ArrayList<>();
        //大于00：00：00
        BmobQuery<StepInfo> q1 = new BmobQuery<>();
        String start = currdate+" 00:00:00" ;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date  = null;
        try {
            date = sdf.parse(start);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        q1.addWhereGreaterThanOrEqualTo("updatedAt",new BmobDate(date));
        and.add(q1);
        //小于23：59：59
        BmobQuery<StepInfo> q2 = new BmobQuery<>();
        String end = currdate+" 23:59:59";
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1  = null;
        try {
            date1 = sdf1.parse(end);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        q2.addWhereLessThanOrEqualTo("updatedAt",new BmobDate(date1));
        and.add(q2);
        //添加复合与查询
        query.and(and);

        if(showProgressBar)
           showProgressDialog(true);
        query.findObjects(new FindListener<StepInfo>() {
            @Override
            public void done(List<StepInfo> dataList, BmobException e) {
                swipeLayout.setRefreshing(false);
                stopProgressDialog();
                if (e == null) {
                    if (null != dataList && dataList.size() > 0) {
                        //数据重复问题，暂未想到解决的好办法
                        mDataList.clear();
                        mDataList.addAll(processReturnList(dataList));
                        mAdapter.setNewData(mDataList);
                    }
                } else {
                    LogUtil.d("查询StepInfo失败"+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
    }

    private List<StepInfo> processReturnList(List<StepInfo> dataList) {
        ArrayList<StepInfo> newDataList = new ArrayList<>();
        Iterator<StepInfo> it = dataList.iterator();
        while(it.hasNext()) {
            StepInfo obj = it.next();
            if(!newDataList.contains(obj)) {
                newDataList.add(obj);
            }
        }
        SortList<StepInfo> msList = new SortList<>();
        msList.sortByMethod(newDataList, "getCount", true);

        PersonInfo piInfo = BmobUser.getCurrentUser(PersonInfo.class);
        if (null != piInfo) {
            for (int i = 0; i < newDataList.size(); i++) {
                StepInfo step = newDataList.get(i);
                if (null != step) {
                    if (TextUtils.equals(step.personInfo.getObjectId(), piInfo.getObjectId())) {
                        String text = "第<font color='#FF4081'><big><big> " + (i + 1) + " </big></big></font>名";
                        tvRanking.setText(Html.fromHtml(text));
                    }
                }
            }
        }
        return newDataList;
    }


    @Override
    protected void onResume() {
        super.onResume();
        pedometer.register();
    }


    @OnClick({R.id.iv_back, R.id.iv_history})
    public void onViewClicked(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_history:
                startActivity(new Intent(StepActivity.this, StepHistoryActivity.class));
                break;
        }
    }


    class OnStepCountChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(ACTION_ON_STEP_COUNT_CHANGE, intent.getAction())) {
                // 支付宝步数统计就是依据了此原理
                progressStep.setStepCountText(pedometer.getStepCount() + "");
                progressStep.setPercentage(pedometer.getStepCount());

                uploadStepInfo(pedometer.getStepCount());
            }
        }
    }



    /**
     * 删除服务器上已有的计步信息
     */
    private void deleteStepInfo() {
        final PersonInfo piInfo = BmobUser.getCurrentUser(PersonInfo.class);
        BmobQuery<StepInfo> bmobQuery = new BmobQuery<>();
        bmobQuery.addWhereEqualTo("personInfo", piInfo);
        bmobQuery.findObjects(new FindListener<StepInfo>() {
            @Override
            public void done(List<StepInfo> list, BmobException e) {
                if (e == null) {
                    if (list != null && list.size() > 0) {
                        List<String> idList = new ArrayList<>();
                        for(StepInfo stepInfo: list){
                            idList.add(stepInfo.getObjectId());
                        }
                        for (int i = 0; i < idList.size(); i++) {
                            String id = idList.get(i);
                            StepInfo step = new StepInfo();
                            step.setObjectId(id);
                            step.delete(new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if (e == null) {
                                        System.out.println("—— 删除成功 ——");
                                    } else {
                                        System.out.println("—— 删除失败 ——");
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }


    /**
     * 上传计步信息
     */
    private void uploadStepInfo(final int stepcount) {
        final PersonInfo piInfo = BmobUser.getCurrentUser(PersonInfo.class);
        if(piInfo == null)
            return;
        final StepInfo stepInfo = new StepInfo();
        stepInfo.setCount(stepcount);
        //添加一对一关联
        stepInfo.personInfo = piInfo;
        stepInfo.save(new SaveListener<String>() {
            @Override
            public void done(String objId, BmobException e) {
                if (e == null) {
                    System.out.println("—— 步数同步成功 ——");
                } else {
                    System.out.println("—— 步数同步失败 ——");
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mReceiver != null){
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        if(pedometer != null){
            pedometer.unRegister();
            pedometer = null;
        }
    }
}