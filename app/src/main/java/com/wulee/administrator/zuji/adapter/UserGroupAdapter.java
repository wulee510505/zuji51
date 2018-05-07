package com.wulee.administrator.zuji.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.database.bean.PersonInfo;
import com.wulee.administrator.zuji.ui.PersonalInfoActivity;
import com.wulee.administrator.zuji.ui.UserInfoActivity;
import com.wulee.administrator.zuji.utils.ImageUtil;
import com.wulee.administrator.zuji.utils.PhoneUtil;

import java.util.ArrayList;

import cn.bmob.v3.BmobUser;


public class UserGroupAdapter extends BaseQuickAdapter<PersonInfo,BaseViewHolder> {

    private Context context;

    protected boolean isScrolling = false;

    public UserGroupAdapter(int layoutResId, ArrayList<PersonInfo> dataList, Context context) {
        super(layoutResId, dataList);
        this.context = context;
    }

    public void setScrolling(boolean scrolling) {
        isScrolling = scrolling;
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, PersonInfo piInfo) {

        TextView tvName = baseViewHolder.getView(R.id.tv_user_name);
        if(!TextUtils.isEmpty(piInfo.getName()) && !TextUtils.equals("游客",piInfo.getName())){
            tvName.setText(piInfo.getName());
        }else if(!TextUtils.isEmpty(piInfo.getUsername())){
            tvName.setText(PhoneUtil.encryptTelNum(piInfo.getUsername()));
        }

        ImageView ivAvatar = baseViewHolder.getView(R.id.iv_header);
        if(piInfo != null && !TextUtils.isEmpty(piInfo.getHeader_img_url()) && !isScrolling) {
            ImageUtil.setDefaultImageView(ivAvatar, piInfo.getHeader_img_url(), R.mipmap.icon_user_def_rect, context);
        } else{
            ImageUtil.setDefaultImageView(ivAvatar,"",R.mipmap.icon_user_def_rect,context);
        }
        ivAvatar.setOnClickListener(view -> {
            PersonInfo currPiInfo = BmobUser.getCurrentUser(PersonInfo.class);
            if(currPiInfo == null){
                return;
            }
            Intent intent;
            if(TextUtils.equals(piInfo.getUsername(),currPiInfo.getUsername())){
                intent = new Intent(mContext, PersonalInfoActivity.class);
            }else{
                intent = new Intent(mContext, UserInfoActivity.class);
                intent.putExtra("piInfo",piInfo);
            }
            context.startActivity(intent);
        });
    }
}
