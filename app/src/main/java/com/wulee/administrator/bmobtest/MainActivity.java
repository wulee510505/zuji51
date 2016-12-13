package com.wulee.administrator.bmobtest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wulee.administrator.bmobtest.entity.LocationInfo;
import com.wulee.administrator.bmobtest.entity.PersonalInfo;
import com.wulee.administrator.bmobtest.service.ScreenService;
import com.wulee.administrator.bmobtest.utils.LocationUtil;
import com.wulee.administrator.bmobtest.utils.MD5Util;
import com.wulee.administrator.bmobtest.utils.PhoneUtil;

import java.io.File;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobConfig;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnAdd;
    private Button btnDel;
    private Button btnModfiy;
    private Button btnUploadImg;
    private TextView tvContent;
    private ProgressBar mProgressBar;

    public static ACache aCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        btnAdd.setOnClickListener(this);
        btnDel.setOnClickListener(this);
        btnModfiy.setOnClickListener(this);
        btnUploadImg.setOnClickListener(this);
    }

    private void initView() {
        btnAdd = (Button) findViewById(R.id.btn_add);
        btnDel = (Button) findViewById(R.id.btn_delete);
        btnModfiy = (Button) findViewById(R.id.btn_modfiy);
        btnUploadImg = (Button) findViewById(R.id.btn_upload_img);
        tvContent = (TextView) findViewById(R.id.tv_content);
        mProgressBar= (ProgressBar) findViewById(R.id.progressBar);
    }

    private  void addData(){
        PersonalInfo pi = new PersonalInfo();
        pi.uuid = PhoneUtil.getUUID();
        pi.name = "王五";
        pi.pwd = MD5Util.MD5("123456");
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
            public void done(List<LocationInfo> object, BmobException e) {
                if(e==null){
                    mProgressBar.setVisibility(View.GONE);
                    tvContent.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this,"查询成功：共"+object.size()+"条数据",Toast.LENGTH_SHORT).show();
                    StringBuilder piSb = new StringBuilder();
                    String imgUrl = "";
                    for (LocationInfo location : object) {
                        if(!TextUtils.isEmpty(location.address))
                             piSb.append(location.address).append(" ").append(location.locationdescribe).append("\n");
                        if(!TextUtils.isEmpty(location.imgurl))
                            imgUrl = location.imgurl;

                    }
                    tvContent.setText(piSb.toString());

                }else{
                    Toast.makeText(MainActivity.this,"查询失败"+e.getMessage()+","+e.getErrorCode(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadImg(){
        String picPath = "/mnt/sdcard/temp.png";
        final BmobFile bmobFile = new BmobFile(new File(picPath));
        bmobFile.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if(e==null){
                    Toast.makeText(MainActivity.this,"上传文件成功"+ bmobFile.getFileUrl(),Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this,"上传文件失败",Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onProgress(Integer value) {
                // 返回的上传进度（百分比）
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
            case R.id.btn_upload_img:
                uploadImg();
                break;
        }
    }
}
