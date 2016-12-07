package com.wulee.administrator.bmobtest;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.wulee.administrator.bmobtest.entity.LocationInfo;
import com.wulee.administrator.bmobtest.entity.PersonalInfo;

import java.util.List;
import java.util.Random;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobConfig;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnAdd;
    private Button btnDel;
    private Button btnModfiy;
    private TextView tvContent;
    private ProgressBar mProgressBar;

    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    public static ACache aCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        aCache = ACache.get(this);

        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数
        initLocation();
        mLocationClient.start();

        initBmobSDK();
        initView();
        addListener();

        query();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span= 1000 * 60 * 5; // 5分钟
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
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
        btnAdd.setOnClickListener(this);
        btnDel.setOnClickListener(this);
        btnModfiy.setOnClickListener(this);
    }

    private void initView() {
        btnAdd = (Button) findViewById(R.id.btn_add);
        btnDel = (Button) findViewById(R.id.btn_delete);
        btnModfiy = (Button) findViewById(R.id.btn_modfiy);
        tvContent = (TextView) findViewById(R.id.tv_content);
        mProgressBar= (ProgressBar) findViewById(R.id.progressBar);
    }

    private  void addData(){
        PersonalInfo pi = new PersonalInfo();
        pi.uid = 1000  + new Random().nextInt(100000);
        pi.name = "王五";
        pi.pwd = "87654321";
        pi.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if(e==null){
                    Toast.makeText(MainActivity.this,"添加数据成功",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this,"添加数据失败"+e.getMessage()+","+e.getErrorCode(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    /**
     * 修改数据
     * 注意：修改数据只能通过objectId来修改，目前不提供查询条件方式的修改方法。
     */
    private  void modfiy(){
        PersonalInfo pi = new PersonalInfo();
        pi.pwd = "wx123456wx";
        pi.update("dc8952f597", new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e==null){
                    Toast.makeText(MainActivity.this,"更新数据成功",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this,"更新数据失败"+e.getMessage()+","+e.getErrorCode(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    /**
     * 删除数据
     *注意：删除数据只能通过objectId来删除，目前不提供查询条件方式的删除方法。
     */
    private void delete(){
        PersonalInfo pi = new PersonalInfo();
        pi.pwd = "313beaaa94";
        pi.delete(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e==null){
                    Toast.makeText(MainActivity.this,"删除数据成功",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this,"删除数据失败"+e.getMessage()+","+e.getErrorCode(),Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private void query(){
        BmobQuery<LocationInfo> query = new BmobQuery<LocationInfo>();
        //查询name叫“王五”的数据
        query.addWhereEqualTo("nativePhoneNumber", "Meizu MX4");
       //返回50条数据，如果不加上这条语句，默认返回10条数据
        query.setLimit(50);
       //执行查询方法
        query.findObjects(new FindListener<LocationInfo>() {
            @Override
            public void done(List<LocationInfo> object, BmobException e) {
                if(e==null){
                    mProgressBar.setVisibility(View.GONE);
                    tvContent.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this,"查询成功：共"+object.size()+"条数据",Toast.LENGTH_SHORT).show();
                    StringBuilder piSb = new StringBuilder();
                    for (LocationInfo location : object) {
                        if(!TextUtils.isEmpty(location.address))
                             piSb.append(location.address).append(" ").append(location.locationdescribe).append("\n");
                    }
                    tvContent.setText(piSb.toString());
                }else{
                    Toast.makeText(MainActivity.this,"查询失败"+e.getMessage()+","+e.getErrorCode(),Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add:
                addData();
                break;
            case R.id.btn_delete:
                delete();
                break;
            case R.id.btn_modfiy:
                modfiy();
                break;
        }
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            LocationInfo locationInfo = new LocationInfo();
            locationInfo.latitude = location.getLatitude()+"";
            locationInfo.lontitude = location.getLongitude()+"";
            locationInfo.address = location.getAddrStr();
            locationInfo.locationdescribe = location.getLocationDescribe();

            StringBuilder sbdeviceInfo = new StringBuilder();
            if(!TextUtils.isEmpty(getNativePhoneNumber())){
                locationInfo.nativePhoneNumber = sbdeviceInfo.append(getDeviceBrand()).append(" ").append(getSystemModel()).append(" ").append(getNativePhoneNumber()).toString();
            }else{
                locationInfo.nativePhoneNumber = sbdeviceInfo.append(getDeviceBrand()).append(" ").append(getSystemModel()).toString();
            }
            if(!TextUtils.isEmpty(location.getAddrStr())&& !TextUtils.equals(location.getLatitude()+"",aCache.getAsString("lat")) && !TextUtils.equals(location.getLongitude()+"",aCache.getAsString("lon")))
                submitLocationInfo(locationInfo);

            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            Log.i("Location", sb.toString());
        }
    }

    private  void submitLocationInfo(final LocationInfo locationInfo){
        if(null == locationInfo)
            return;
        locationInfo.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if(e==null){
                    aCache.put("lat",locationInfo.latitude);
                    aCache.put("lon",locationInfo.lontitude);
                    Toast.makeText(MainActivity.this,"位置同步成功",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this,"位置同步失败"+e.getMessage()+","+e.getErrorCode(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    /**
     * Role:获取当前设置的电话号码
     */
    public String getNativePhoneNumber() {
        TelephonyManager telephonyManager  = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String NativePhoneNumber = telephonyManager.getLine1Number();
        return NativePhoneNumber;
    }

    /**
     * 获取手机型号
     */
    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机厂商
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }
}
