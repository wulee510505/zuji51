package com.wulee.administrator.bmobtest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wulee.administrator.bmobtest.R;
import com.wulee.administrator.bmobtest.base.BaseActivity;
import com.wulee.administrator.bmobtest.entity.PersonalInfo;

import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by wulee on 2017/1/12 09:57
 */

public class RegistActivity extends BaseActivity implements View.OnClickListener{

    private EditText mEtMobile;
    private EditText mEtPwd;
    private EditText mEtPincode;
    private Button  mBtnRegist;
    private Button  mBtnPincode;

    private String mobile ,authCode,pwd ;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.regist);

        initView();
        addListener();
    }

    private void addListener() {
        mBtnPincode.setOnClickListener(this);
        mBtnRegist.setOnClickListener(this);
    }

    private void initView() {
        mEtMobile = (EditText) findViewById(R.id.et_mobile);
        mEtPwd = (EditText) findViewById(R.id.et_pwd);
        mEtPincode = (EditText) findViewById(R.id.et_pincode);
        mBtnPincode = (Button) findViewById(R.id.btn_pincode);
        mBtnRegist = (Button) findViewById(R.id.btn_regist);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_regist:
                mobile  = mEtMobile.getText().toString().trim();
                pwd = mEtPwd.getText().toString().trim();
                authCode = mEtPincode.getText().toString().trim();

                if(TextUtils.isEmpty(mobile)){
                    Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(pwd)){
                    Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
               if(TextUtils.isEmpty(authCode)){
                    Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
                    return;
                }
                doRegist(mobile,pwd);
            break;
            case R.id.btn_pincode:
                mobile  = mEtMobile.getText().toString().trim();
                if(TextUtils.isEmpty(mobile)){
                    Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
                    return;
                }
                //获取短信验证码
                BmobSMS.requestSMSCode(mobile, "regist", new QueryListener<Integer>() {
                    @Override
                    public void done(Integer integer, BmobException e) {
                        if(e==null){
                            Toast.makeText(RegistActivity.this, "发送短信成功", Toast.LENGTH_SHORT).show();
                        }else{
                            e.printStackTrace();
                        }
                    }
                });
                break;
        }
    }


    private void doRegist(String mobile, String pwd) {
        PersonalInfo piInfo = new PersonalInfo();
        piInfo.setMobilePhoneNumber(mobile);
        piInfo.setUsername(mobile);
        piInfo.setPassword(pwd);
        piInfo.signOrLogin(authCode, new SaveListener<PersonalInfo>() {
            @Override
            public void done(PersonalInfo user,BmobException e) {
                if(e==null){
                    Toast.makeText(RegistActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegistActivity.this,LoginActivity.class));
                }else{
                    toast("注册失败:" + e.getMessage());
                }
            }
        });
    }
}
