package com.wulee.administrator.bmobtest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.baidu.mapapi.SDKInitializer;
import com.wulee.administrator.bmobtest.R;

import static com.wulee.administrator.bmobtest.App.aCache;

/**
 * Created by wulee on 2017/1/11 16:59
 */

public class SettingActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btnLogout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.settting);

        initView();
        addListerer();
    }

    private void addListerer() {
        btnLogout.setOnClickListener(this);
    }

    private void initView() {
        btnLogout = (Button) findViewById(R.id.btn_logout);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_logout:
                aCache.put("has_login","no");
                startActivity(new Intent(this,LoginActivity.class));
                finish();
            break;
        }
    }
}
