package com.wulee.administrator.zuji.ui.pushmsg;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.base.BaseActivity;
import com.wulee.administrator.zuji.database.DBHandler;
import com.wulee.administrator.zuji.database.bean.PushMessage;
import com.wulee.administrator.zuji.ui.MapActivity;
import com.wulee.administrator.zuji.utils.SortList;
import com.wulee.administrator.zuji.widget.BaseTitleLayout;
import com.wulee.administrator.zuji.widget.TitleLayoutClickListener;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

import static com.wulee.administrator.zuji.PushMsgReceiver.ACTION_HIDE_PUSH_MSG_NOTIFICATION;


/**
 * Created by wulee on 2017/2/28 21:15
 */

public class PushMsgListActivity extends BaseActivity {

    BaseTitleLayout titlelayout;
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
        mEmptyview =  findViewById(R.id.emptyview);

        mAdapter = new MsgListAdapter(R.layout.push_msg_list_item, msgList,this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            List<PushMessage> dataList = mAdapter.getData();
            if(null != dataList && dataList.size()>0){
                PushMessage msg = dataList.get(position);
                if(null != msg && !TextUtils.isEmpty(msg.getContent()) && msg.getTime()>0){
                    Intent intent = null;
                    if(TextUtils.equals(msg.getType(),PushMessage.MSG_TYPE_SYSTEM)){
                        intent = new Intent(PushMsgListActivity.this,MsgDetailActivity.class);
                        intent.putExtra("msg",msg);
                    }else if(TextUtils.equals(msg.getType(),PushMessage.MSG_TYPE_LOCATION)){
                        String[] location = msg.getContent().split("；");
                        if(location != null && location.length == 4){
                            String latStr = location[1];
                            String lonStr = location[2];
                            intent = new Intent(PushMsgListActivity.this,MapActivity.class);
                            intent.putExtra(MapActivity.INTENT_KEY_LATITUDE,latStr.split("：")[1]);
                            intent.putExtra(MapActivity.INTENT_KEY_LONTITUDE,lonStr.split("：")[1]);
                        }
                    }
                    if(intent != null)
                       startActivity(intent);
                }
            }
        });
    }

    private void addListener() {
        EventBus.getDefault().register(this);
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

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onPushMsgEvent(PushMessage pushMessage) {
        if(!msgList.contains(pushMessage)){
            if(mEmptyview.getVisibility() == View.VISIBLE){
                mEmptyview.setVisibility(View.GONE);
            }
            msgList.add(0,pushMessage);
            mAdapter.setNewData(msgList);
        }
    }

    private void initData() {
        sendBroadcast(new Intent(ACTION_HIDE_PUSH_MSG_NOTIFICATION));

        showProgressDialog(true);
        List<PushMessage> list = DBHandler.getAllPushMessage();

        SortList<PushMessage> sortList = new SortList<>();
        sortList.sortByMethod(list, "getTime", true);//按消息时间倒序
        msgList.clear();
        if(null != list && list.size()>0){
            mEmptyview.setVisibility(View.GONE);
            msgList.addAll(list);
        }else{
            mEmptyview.setVisibility(View.VISIBLE);
        }
        stopProgressDialog();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
