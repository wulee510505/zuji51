package com.wulee.administrator.zuji.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.base.BaseActivity;
import com.wulee.administrator.zuji.database.bean.LocationInfo;
import com.wulee.administrator.zuji.database.bean.PersonInfo;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.wulee.administrator.zuji.App.aCache;


/**
 * Created by wulee on 2017/3/15 11:47
 */

public class ZuJiMapActivity extends BaseActivity {


    public static final String ACTION_LOCATION_CHANGE = "action_location_change";

    private MapView mapView;
    private BaiduMap mBaiduMap;

    private List<LocationInfo> zujiList;
    private final int MSG_QUERY_ZUJI_DATA_OK = 1000;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_QUERY_ZUJI_DATA_OK:
                    List<LocationInfo> list = (List<LocationInfo>) msg.obj;
                    addLocation(list);
                 break;
            }
        }
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zuji_map);


        initView();
        queryData();
    }

    private void initView() {
        mapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mapView.getMap();
    }

    /**
     * 查询数据
     */
    private void queryData(){
        if(!TextUtils.equals("yes",aCache.getAsString("isUploadLocation"))){
            return;
        }
        PersonInfo piInfo = BmobUser.getCurrentUser(PersonInfo.class);
        BmobQuery<LocationInfo> query = new BmobQuery<LocationInfo>();
        query.addWhereEqualTo("piInfo", piInfo);    // 查询当前用户的所有位置信息
        query.include("piInfo");// 希望在查询位置信息的同时也把当前用户的信息查询出来
        query.order("-createdAt");
        // 设置每页数据个数
        query.setLimit(50);
        query.findObjects(new FindListener<LocationInfo>() {
            @Override
            public void done(List<LocationInfo> dataList, BmobException e) {
                if(e == null){
                    if(dataList != null && dataList.size()>0){
                      Message msg = new Message();
                      msg.what = MSG_QUERY_ZUJI_DATA_OK;
                      msg.obj =   dataList;
                      mHandler.sendMessage(msg);
                    }
                }else{
                    Toast.makeText(ZuJiMapActivity.this,"查询失败"+e.getMessage()+","+e.getErrorCode(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addLocation(List<LocationInfo> dataList) {
        LatLng lastLocation = null;
        for (int i = 0; i < dataList.size(); i++) {
            LocationInfo location = dataList.get(i);
            //定义Maker坐标点
            LatLng point = new LatLng(Double.parseDouble(location.getLatitude()), Double.parseDouble(location.getLontitude()));
            //构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.icon_mark);
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions()
                    .position(point)
                    .icon(bitmap);
            //在地图上添加Marker，并显示
            mBaiduMap.addOverlay(option);

            if(i == 0){
                lastLocation = new LatLng(Double.parseDouble(location.getLatitude()), Double.parseDouble(location.getLontitude()));
            }
        }
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(lastLocation).zoom(18.0f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }



    public class LocationChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(intent.getAction())) {
                return;
            }
            if (action.equals(ACTION_LOCATION_CHANGE)) {
                String currLatitude = aCache.getAsString("lat");
                String currLontitude = aCache.getAsString("lon");
                if(!TextUtils.isEmpty(currLatitude) && !TextUtils.isEmpty(currLontitude)){

                    BDLocation currLocation = new BDLocation();
                    currLocation.setLatitude(Double.parseDouble(currLatitude));
                    currLocation.setLongitude(Double.parseDouble(currLontitude));
                    // 开启定位图层
                    mBaiduMap.setMyLocationEnabled(true);
                    // 构造定位数据
                    MyLocationData locData = new MyLocationData.Builder()
                            .accuracy(currLocation.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                            .direction(100).latitude(currLocation.getLatitude())
                            .longitude(currLocation.getLongitude()).build();
                    // 设置定位数据
                    mBaiduMap.setMyLocationData(locData);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mapView.onDestroy();
        mapView = null;
        super.onDestroy();
    }

}
