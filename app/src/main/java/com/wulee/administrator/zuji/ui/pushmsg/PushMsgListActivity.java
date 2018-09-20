package com.wulee.administrator.zuji.ui.pushmsg;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;

import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.base.BaseActivity;
import com.wulee.administrator.zuji.database.DBHandler;
import com.wulee.administrator.zuji.database.bean.PushMessage;
import com.wulee.administrator.zuji.widget.BaseTitleLayout;
import com.wulee.administrator.zuji.widget.TitleLayoutClickListener;

import java.util.ArrayList;
import java.util.List;

import static com.wulee.administrator.zuji.PushMsgReceiver.ACTION_HIDE_PUSH_MSG_NOTIFICATION;


/**
 * Created by wulee on 2017/2/28 21:15
 */

public class PushMsgListActivity extends BaseActivity {

    BaseTitleLayout titlelayout;
    private ProgressBar mPb;
    private View mEmptyview;
    private RecyclerView mRecyclerView;
    private MsgListAdapter mAdapter;

    ArrayList<PushMessage> msgList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.push_msg_list_main);

        initView();
        initData();
        addListener();
    }


    private void initView() {
        titlelayout= findViewById(R.id.titlelayout);
        mRecyclerView = findViewById(R.id.recyclerview);
        mPb =  findViewById(R.id.progress_bar);
        mEmptyview =  findViewById(R.id.emptyview);

        mAdapter = new MsgListAdapter(R.layout.push_msg_list_item, msgList);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            List<PushMessage> dataList = mAdapter.getData();
            if(null != dataList && dataList.size()>0){
                PushMessage msg = dataList.get(position);
                if(null != msg && !TextUtils.isEmpty(msg.getContent()) && msg.getTime()>0){
                    Intent intent = new Intent(PushMsgListActivity.this,MsgDetailActivity.class);
                    intent.putExtra("msg",msg);
                    startActivity(intent);
                }
            }
        });
    }

    private void addListener() {
        titlelayout.setOnTitleClickListener(new TitleLayoutClickListener() {
            @Override
            public void onLeftClickListener() {
                myfinsh();
            }
            @Override
            public void onRightTextClickListener() {
                List<PushMessage> list = DBHandler.getAllPushMessage();
                if(list != null && list.size()>0){
                    DBHandler.delAllPushMessage();
                    initData();
                }
            }
        });
    }



    private void initData() {
        sendBroadcast(new Intent(ACTION_HIDE_PUSH_MSG_NOTIFICATION));

        mPb.setVisibility(View.VISIBLE);
        List<PushMessage> list = DBHandler.getAllPushMessage();
        msgList.clear();
        if(null != list && list.size()>0){
            mPb.setVisibility(View.GONE);
            mEmptyview.setVisibility(View.GONE);
            msgList.addAll(list);
        }else{
            mPb.setVisibility(View.GONE);
            mEmptyview.setVisibility(View.VISIBLE);
        }
        mAdapter.setNewData(msgList);
    }


    @Override
    public void onBackPressed() {
        myfinsh();
    }

    private void myfinsh() {
        Intent intent = getIntent();
        setResult(RESULT_OK,intent);
        finish();
    }
}
