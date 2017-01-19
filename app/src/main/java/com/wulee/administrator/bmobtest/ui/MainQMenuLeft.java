package com.wulee.administrator.bmobtest.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wulee.administrator.bmobtest.R;
import com.wulee.administrator.bmobtest.entity.PersonalInfo;
import com.wulee.administrator.bmobtest.utils.AppUtils;
import com.wulee.administrator.bmobtest.utils.LocationUtil;
import com.wulee.administrator.bmobtest.widget.CircleImageView;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.update.BmobUpdateAgent;

import static com.wulee.administrator.bmobtest.App.aCache;


/**
 * Created by wulee on 2017/1/22
 * 主页侧滑菜单 (仿QQ效果)
 */
public class MainQMenuLeft extends Fragment implements View.OnClickListener {

    private static final String REQ_CHECK_UPDATE = "menu_check_update";

    private Context mContext;

    private CircleImageView rbImage;
    private TextView mTvMobile;

    private TextView tvLoginOut,tvCheckUpdate; // 登录、退出登录提示语

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        View view = inflater.inflate(R.layout.main_menu_left, container, false);
        initUI(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initUI(View view) {
        rbImage = (CircleImageView) view.findViewById(R.id.circle_img_header);
        mTvMobile = (TextView) view.findViewById(R.id.tv_mobile);

        tvCheckUpdate = (TextView) view.findViewById(R.id.mml_checkupdate_tv);
        tvLoginOut = (TextView) view.findViewById(R.id.mml_loginout_tv);
        tvLoginOut.setOnClickListener(this);
        tvCheckUpdate.setOnClickListener(this);


        PersonalInfo piInfo = BmobUser.getCurrentUser(PersonalInfo.class);
        if(null != piInfo){
            mTvMobile.setText(piInfo.getMobilePhoneNumber());
        }
    }

    @Override
    public void onClick(View v) {
         switch (v.getId()){
             case R.id.mml_loginout_tv:
                 aCache.put("has_login","no");
                 LocationUtil.getInstance().stopGetLocation();
                 AppUtils.AppExit(mContext);
                 PersonalInfo.logOut();
                 startActivity(new Intent(mContext,LoginActivity.class));
                 break;
             case R.id.mml_checkupdate_tv:
                 BmobUpdateAgent.update(mContext);
                 break;
         }

    }


}
