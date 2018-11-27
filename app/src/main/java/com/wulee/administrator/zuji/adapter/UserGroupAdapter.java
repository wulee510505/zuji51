package com.wulee.administrator.zuji.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.entity.UserGroupItem;
import com.wulee.administrator.zuji.utils.ImageUtil;
import com.wulee.administrator.zuji.utils.PhoneUtil;

import java.util.List;


public class UserGroupAdapter extends BaseMultiItemQuickAdapter<UserGroupItem,BaseViewHolder> {

    private Context context;

    private boolean isScrolling = false;


    public UserGroupAdapter(Context context, List<UserGroupItem> data) {
        super(data);
        this.context = context;
        addItemType(UserGroupItem.SECTION, R.layout.user_group_section_item);
        addItemType(UserGroupItem.ITEM, R.layout.user_group_list_item);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, UserGroupItem item) {
        switch (baseViewHolder.getItemViewType()){
            case UserGroupItem.SECTION :
                TextView tvTime = baseViewHolder.getView(R.id.tv_group_item);
                tvTime.setText(item.mPersonInfo.getGroupKey());
                break;
            case UserGroupItem.ITEM :
                TextView tvName = baseViewHolder.getView(R.id.tv_user_name);
                if(!TextUtils.isEmpty(item.mPersonInfo.getName()) && !TextUtils.equals("游客",item.mPersonInfo.getName())){
                    tvName.setText(item.mPersonInfo.getName());
                }else if(!TextUtils.isEmpty(item.mPersonInfo.getUsername())){
                    tvName.setText(PhoneUtil.encryptTelNum(item.mPersonInfo.getUsername()));
                }

                TextView tvLoginTime = baseViewHolder.getView(R.id.tv_first_login);
                String createTime = item.mPersonInfo.getCreatedAt();
                if(!TextUtils.isEmpty(createTime)){
                    tvLoginTime.setText(createTime.substring(11,createTime.length())+" 第一次来到了足迹");
                }

                ImageView ivAvatar = baseViewHolder.getView(R.id.iv_header);
                if(item.mPersonInfo != null && !TextUtils.isEmpty(item.mPersonInfo.getHeader_img_url()) && !isScrolling) {
                    ImageUtil.setCircleImageView(ivAvatar, item.mPersonInfo.getHeader_img_url(), R.mipmap.icon_user_def_colorized, context);
                } else{
                    if(TextUtils.equals("男",item.mPersonInfo.getSex())){
                        ImageUtil.setCircleImageView(ivAvatar,"",R.mipmap.icon_man_def_colorized,context);
                    }else if(TextUtils.equals("女",item.mPersonInfo.getSex())){
                        ImageUtil.setCircleImageView(ivAvatar,"",R.mipmap.icon_woman_def_colorized,context);
                    }else {
                        ImageUtil.setCircleImageView(ivAvatar,"",R.mipmap.icon_user_def_colorized,context);
                    }
                }
                break;
        }

    }

}
