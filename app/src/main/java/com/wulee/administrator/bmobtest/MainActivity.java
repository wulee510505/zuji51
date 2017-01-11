package com.wulee.administrator.bmobtest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.wulee.administrator.bmobtest.adapter.LocationAdapter;
import com.wulee.administrator.bmobtest.entity.LocationInfo;
import com.wulee.administrator.bmobtest.service.ScreenService;
import com.wulee.administrator.bmobtest.utils.LocationUtil;
import com.wulee.administrator.bmobtest.utils.PhoneUtil;
import com.wulee.administrator.bmobtest.widget.RecycleViewDivider;

import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobConfig;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private RecyclerView mRecyclerView;
    private LocationAdapter mAdapter;

    private ProgressBar mProgressBar;

    public static ACache aCache;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_list_main);

        aCache = ACache.get(this);

        LocationUtil.getInstance().startGetLocation();

        initBmobSDK();
        initView();
        addListener();

        startService(new Intent(MainActivity.this,ScreenService.class));

        if(TextUtils.equals("yes",aCache.getAsString("isUploadLocation"))){
            query();
        }
    }

    private void initBmobSDK() {
        BmobConfig config = new BmobConfig.Builder(this)
                .setApplicationId("ac67374a92fdca635c75eb6388e217a4")  //设置appkey
                .setConnectTimeout(30)//请求超时时间（单位为秒）：默认15s
                .setUploadBlockSize(1024 * 1024)//文件分片上传时每片的大小（单位字节），默认512*1024
                .setFileExpiration(2500)//文件的过期时间(单位为秒)：默认1800s
                .build();
        Bmob.initialize(config);
    }

    private void addListener() {
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
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerview);

        mAdapter = new LocationAdapter(R.layout.location_list_item,null);
        mRecyclerView.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL, 2, ContextCompat.getColor(this,R.color.divider_color)));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        mProgressBar= (ProgressBar) findViewById(R.id.progressBar);
    }


    private void query(){
        mProgressBar.setVisibility(View.VISIBLE);
        BmobQuery<LocationInfo> query = new BmobQuery<LocationInfo>();
        //查询name叫“王五”的数据
        String currDeviceId = PhoneUtil.getDeviceId();
        query.addWhereEqualTo("deviceId", currDeviceId );
       //返回50条数据，如果不加上这条语句，默认返回10条数据
        query.setLimit(20);
       //执行查询方法
        query.findObjects(new FindListener<LocationInfo>() {
            @Override
            public void done(List<LocationInfo> dataList, BmobException e) {
                if(e==null){
                    mProgressBar.setVisibility(View.GONE);

                    Toast.makeText(MainActivity.this,"查询成功：共"+dataList.size()+"条数据",Toast.LENGTH_SHORT).show();
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

        }
    }
}
