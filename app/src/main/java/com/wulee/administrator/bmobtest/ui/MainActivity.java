package com.wulee.administrator.bmobtest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.wulee.administrator.bmobtest.R;
import com.wulee.administrator.bmobtest.adapter.LocationAdapter;
import com.wulee.administrator.bmobtest.base.BaseActivity;
import com.wulee.administrator.bmobtest.entity.LocationInfo;
import com.wulee.administrator.bmobtest.entity.PersonalInfo;
import com.wulee.administrator.bmobtest.service.ScreenService;
import com.wulee.administrator.bmobtest.utils.LocationUtil;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.wulee.administrator.bmobtest.App.aCache;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private SwipeRefreshLayout swipeLayout;
    private RecyclerView mRecyclerView;
    private LocationAdapter mAdapter;

    private ImageView ivSetting;

    private static final int STATE_REFRESH = 0;// 下拉刷新
    private static final int STATE_MORE = 1;// 加载更多
    private int PAGE_SIZE = 10;
    private int curPage = 0;
    private boolean isRefresh = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_list_main);

        LocationUtil.getInstance().startGetLocation();

        initView();
        addListener();

        startService(new Intent(MainActivity.this,ScreenService.class));

        mHandler.postDelayed(mRunnable,1000);
    }


    @Override
    protected void onResume() {
        super.onResume();
        isRefresh = true;
        query(0, STATE_REFRESH);
    }

    private void addListener() {
        ivSetting.setOnClickListener(this);
        mAdapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                List<LocationInfo> locationInfoList = mAdapter.getData();
                if(null != locationInfoList && locationInfoList.size()>0){
                    LocationInfo location = locationInfoList.get(pos);
                    if(null != location){
                        Intent intent = new Intent(MainActivity.this,MapActivity.class);
                        intent.putExtra(MapActivity.INTENT_KEY_LATITUDE,location.latitude);
                        intent.putExtra(MapActivity.INTENT_KEY_LONTITUDE,location.lontitude);
                        startActivity(intent);
                    }
                }
            }
        });
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefresh = true;
                curPage = 0;
                query(curPage, STATE_REFRESH);
        }
        });
        //加载更多
        mAdapter.openLoadMore(PAGE_SIZE, true);
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener(){
            @Override
            public void onLoadMoreRequested() {
                query(curPage, STATE_MORE);
            }
        });
    }

    private void initView() {
        ivSetting = (ImageView) findViewById(R.id.iv_setting);
        swipeLayout = (SwipeRefreshLayout)findViewById(R.id.swipeLayout);
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerview);

        mAdapter = new LocationAdapter(R.layout.location_list_item,null);
        //mRecyclerView.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL, 2, ContextCompat.getColor(this,R.color.divider_color)));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
    }


    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        public void run () {
            isRefresh = true;
            query(0, STATE_REFRESH);
            mHandler.postDelayed(this,1000 * 60 * 2);
        }
    };


    /**
     * 分页获取数据
     */
    private void query(final int page, final int actionType){
        if(!TextUtils.equals("yes",aCache.getAsString("isUploadLocation"))){
            return;
        }
        PersonalInfo piInfo = BmobUser.getCurrentUser(PersonalInfo.class);
        BmobQuery<LocationInfo> query = new BmobQuery<LocationInfo>();
        query.addWhereEqualTo("piInfo", piInfo);    // 查询当前用户的所有位置信息
        query.include("piInfo");// 希望在查询位置信息的同时也把当前用户的信息查询出来
        query.order("-createdAt");
        // 如果是加载更多
        if(actionType == STATE_MORE){
            // 跳过之前页数并去掉重复数据
            query.setSkip(page * PAGE_SIZE + 1);
        }else{
            query.setSkip(0);
        }
        // 设置每页数据个数
        query.setLimit(PAGE_SIZE);
        query.findObjects(new FindListener<LocationInfo>() {
            @Override
            public void done(List<LocationInfo> dataList, BmobException e) {
                swipeLayout.setRefreshing(false);
                if(e == null){
                    curPage++;
                    if (isRefresh){//下拉刷新需清理缓存
                        mAdapter.setNewData(dataList);
                        isRefresh = false;
                    }else {//正常请求 或 上拉加载更多时处理流程
                        if (dataList.size() > 0) {
                            mAdapter.notifyDataChangedAfterLoadMore(dataList, true);
                        }else {
                            mAdapter.notifyDataChangedAfterLoadMore(false);
                        }
                        if (mAdapter.getData().size() == 0) {
                            mAdapter.setEmptyView(LayoutInflater.from(MainActivity.this).inflate(R.layout.empty_view, (ViewGroup) mRecyclerView.getParent(), false));
                        }
                    }
                }else{
                    Toast.makeText(MainActivity.this,"查询失败"+e.getMessage()+","+e.getErrorCode(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_setting:
                startActivity(new Intent(this,SettingActivity.class));
            break;

        }
    }

    @Override
    public void onBackPressed() {
        //do nothing
    }
}
