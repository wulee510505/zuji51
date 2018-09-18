package com.wulee.administrator.zuji.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.database.bean.PersonInfo;
import com.wulee.administrator.zuji.utils.ImageUtil;
import com.wulee.administrator.zuji.utils.PhoneUtil;

import java.util.ArrayList;


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

        TextView tvLoginTime = baseViewHolder.getView(R.id.tv_first_login);
        tvLoginTime.setText(piInfo.getCreatedAt()+"第一次来到了足迹");

        ImageView ivAvatar = baseViewHolder.getView(R.id.iv_header);
        if(piInfo != null && !TextUtils.isEmpty(piInfo.getHeader_img_url()) && !isScrolling) {
            ImageUtil.setCircleImageView(ivAvatar, piInfo.getHeader_img_url(), R.mipmap.icon_user_def_colorized, context);
        } else{
            if(TextUtils.equals("男",piInfo.getSex())){
                ImageUtil.setCircleImageView(ivAvatar,"",R.mipmap.icon_man_def_colorized,context);
            }else if(TextUtils.equals("女",piInfo.getSex())){
                ImageUtil.setCircleImageView(ivAvatar,"",R.mipmap.icon_woman_def_colorized,context);
            }else {
                ImageUtil.setCircleImageView(ivAvatar,"",R.mipmap.icon_user_def_colorized,context);
            }
        }
    }
}
