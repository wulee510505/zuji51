package com.wulee.administrator.zuji.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.chatui.enity.Friend;
import com.wulee.administrator.zuji.database.bean.PersonInfo;
import com.wulee.administrator.zuji.ui.PersonalInfoActivity;
import com.wulee.administrator.zuji.ui.UserInfoActivity;
import com.wulee.administrator.zuji.utils.ImageUtil;
import com.wulee.administrator.zuji.utils.PhoneUtil;

import java.util.ArrayList;

import cn.bmob.v3.BmobUser;


public class FriendAdapter extends BaseQuickAdapter<Friend,BaseViewHolder> {

    private Context context;

    protected boolean isScrolling = false;

    public FriendAdapter(int layoutResId, ArrayList<Friend> dataList, Context context) {
        super(layoutResId, dataList);
        this.context = context;
    }

    public void setScrolling(boolean scrolling) {
        isScrolling = scrolling;
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, Friend friend) {

        TextView tvName = baseViewHolder.getView(R.id.tv_user_name);
        PersonInfo friendInfo = friend.getFriendUser();
        if(friendInfo == null)
            return;
        if(!TextUtils.isEmpty(friendInfo.getName()) && !TextUtils.equals("游客",friendInfo.getName())){
            tvName.setText(friendInfo.getName());
        }else if(!TextUtils.isEmpty(friendInfo.getUsername())){
            tvName.setText(PhoneUtil.encryptTelNum(friendInfo.getUsername()));
        }

        ImageView ivAvatar = baseViewHolder.getView(R.id.iv_header);
        if(friendInfo != null && !TextUtils.isEmpty(friendInfo.getHeader_img_url()) && !isScrolling) {
            ImageUtil.setDefaultImageView(ivAvatar, friendInfo.getHeader_img_url(), R.mipmap.icon_user_def_colorized, context);
        } else{
            if(TextUtils.equals("男",friendInfo.getSex())){
                ImageUtil.setDefaultImageView(ivAvatar,"",R.mipmap.icon_man_def_colorized,context);
            }else if(TextUtils.equals("女",friendInfo.getSex())){
                ImageUtil.setDefaultImageView(ivAvatar,"",R.mipmap.icon_woman_def_colorized,context);
            }else {
                ImageUtil.setDefaultImageView(ivAvatar,"",R.mipmap.icon_user_def_colorized,context);
            }
        }
        ivAvatar.setOnClickListener(view -> {
            PersonInfo currPiInfo = BmobUser.getCurrentUser(PersonInfo.class);
            if(currPiInfo == null){
                return;
            }
            Intent intent = null;
            if(TextUtils.equals(friendInfo.getUsername(),currPiInfo.getUsername())){
                intent = new Intent(mContext, PersonalInfoActivity.class);
            }else{
                intent = new Intent(mContext, UserInfoActivity.class);
                intent.putExtra("piInfo",friendInfo);
            }
            context.startActivity(intent);
        });
    }
}
