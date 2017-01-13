package com.wulee.administrator.bmobtest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wulee.administrator.bmobtest.R;
import com.wulee.administrator.bmobtest.entity.PersonalInfo;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.wulee.administrator.bmobtest.App.aCache;

/**
 * Created by wulee on 2017/1/12 09:57
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText mEtMobile;
    private EditText mEtPwd;
    private Button  mBtnLogin;
    private TextView tvRegist;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        initView();
        addListener();
    }

    private void addListener() {
        mBtnLogin.setOnClickListener(this);
        tvRegist.setOnClickListener(this);
    }

    private void initView() {
        mEtMobile = (EditText) findViewById(R.id.et_mobile);
        mEtPwd = (EditText) findViewById(R.id.et_pwd);
        mBtnLogin = (Button) findViewById(R.id.btn_login);
        tvRegist = (TextView) findViewById(R.id.tv_regist);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_login:
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
                doLogin(mobile,pwd);
                break;
            case R.id.tv_regist:
                startActivity(new Intent(this,RegistActivity.class));
            break;
        }
    }

    private void doLogin(String mobile, String pwd) {
        BmobQuery<PersonalInfo> query = new BmobQuery<PersonalInfo>();
        query.addWhereEqualTo("mobile",mobile);
        query.addWhereEqualTo("pwd",pwd);
        //query.addWhereEqualTo("uid",aCache.getAsString("uid"));
        //执行查询方法
        query.findObjects(new FindListener<PersonalInfo>() {
            @Override
            public void done(List<PersonalInfo> object, BmobException e) {
                if(e == null){
                   if(object != null && object.size()>0){
                       aCache.put("has_login","yes");

                       startActivity(new Intent(LoginActivity.this,MainActivity.class));
                   }
                }else{
                    Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}

