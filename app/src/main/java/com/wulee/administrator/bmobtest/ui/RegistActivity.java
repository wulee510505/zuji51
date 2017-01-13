package com.wulee.administrator.bmobtest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wulee.administrator.bmobtest.R;
import com.wulee.administrator.bmobtest.entity.PersonalInfo;
import com.wulee.administrator.bmobtest.utils.PhoneUtil;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

import static com.wulee.administrator.bmobtest.App.aCache;

/**
 * Created by wulee on 2017/1/12 09:57
 */

public class RegistActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText mEtMobile;
    private EditText mEtPwd;
    private Button  mBtnRegist;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.regist);

        initView();
        addListener();
    }

    private void addListener() {
        mBtnRegist.setOnClickListener(this);
    }

    private void initView() {
        mEtMobile = (EditText) findViewById(R.id.et_mobile);
        mEtPwd = (EditText) findViewById(R.id.et_pwd);
        mBtnRegist = (Button) findViewById(R.id.btn_regist);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_regist:
                String mobile = mEtMobile.getText().toString().trim();
                String pwd = mEtPwd.getText().toString().trim();
                if(TextUtils.isEmpty(mobile)){
                    Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(pwd)){
                    Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                doRegist(mobile,pwd);
            break;
        }
    }


    private void doRegist(String mobile, String pwd) {
        PersonalInfo piInfo = new PersonalInfo();
        piInfo.uuid = PhoneUtil.getUDID();
        piInfo.mobile =  mobile;
        piInfo.pwd =  pwd;
        piInfo.save(new SaveListener<String>() {
            @Override
            public void done(String objectId,BmobException e) {
                if(e == null){
                    aCache.put("uid",objectId);

                    startActivity(new Intent(RegistActivity.this,LoginActivity.class));
                }else{
                    Toast.makeText(RegistActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
