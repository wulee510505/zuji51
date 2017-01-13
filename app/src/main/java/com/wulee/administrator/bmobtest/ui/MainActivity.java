package com.wulee.administrator.bmobtest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.wulee.administrator.bmobtest.R;
import com.wulee.administrator.bmobtest.adapter.LocationAdapter;
import com.wulee.administrator.bmobtest.entity.LocationInfo;
import com.wulee.administrator.bmobtest.service.ScreenService;
import com.wulee.administrator.bmobtest.utils.LocationUtil;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.wulee.administrator.bmobtest.App.aCache;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private RecyclerView mRecyclerView;
    private LocationAdapter mAdapter;

    private ImageView ivSetting;
    private ProgressBar mProgressBar;

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
        query();
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

    }

    private void initView() {
        ivSetting = (ImageView) findViewById(R.id.iv_setting);
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerview);

        mAdapter = new LocationAdapter(R.layout.location_list_item,null);
        //mRecyclerView.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL, 2, ContextCompat.getColor(this,R.color.divider_color)));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        mProgressBar= (ProgressBar) findViewById(R.id.progressBar);
    }


    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        public void run () {
            query();
            mHandler.postDelayed(this,1000 * 60 * 2);
        }
    };


    private void query(){
        if(!TextUtils.equals("yes",aCache.getAsString("isUploadLocation"))){
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);
        BmobQuery<LocationInfo> query = new BmobQuery<LocationInfo>();
        query.addWhereEqualTo("uid", aCache.getAsString("uid") );
       //返回50条数据，如果不加上这条语句，默认返回10条数据
        query.setLimit(50);
       //执行查询方法
        query.findObjects(new FindListener<LocationInfo>() {
            @Override
            public void done(List<LocationInfo> dataList, BmobException e) {
                mProgressBar.setVisibility(View.GONE);
                if(e == null){
                    mAdapter.setNewData(dataList);
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
