package com.wulee.administrator.zuji.adapter;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.database.bean.PersonInfo;
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
                StringBuilder sbName = new StringBuilder();
                PersonInfo personInfo  = item.mPersonInfo;
                if(null != personInfo){

                    if(!TextUtils.isEmpty(personInfo.getName())&& !TextUtils.equals("游客",personInfo.getName())){
                        sbName.append(personInfo.getName()).append("<font color='#656565'><small>（").append(PhoneUtil.encryptTelNum(personInfo.getUsername())).append("）<small></font>");
                    }else{
                        sbName.append(PhoneUtil.encryptTelNum(personInfo.getUsername()));
                    }
                    tvName.setText(Html.fromHtml(sbName.toString()));

                    TextView tvCreatTime = baseViewHolder.getView(R.id.tv_first_login);
                    String createTime = personInfo.getCreatedAt();
                    if(!TextUtils.isEmpty(createTime)){
                        tvCreatTime.setText(createTime.substring(11,createTime.length())+" 注册了足迹");
                    }

                    TextView tvAppVersion = baseViewHolder.getView(R.id.tv_app_version);
                    if(!TextUtils.isEmpty(personInfo.getAppVersion())) {
                        tvAppVersion.setVisibility(View.VISIBLE);
                        tvAppVersion.setText("v"+personInfo.getAppVersion());
                    }else {
                        tvAppVersion.setVisibility(View.GONE);
                    }

                    ImageView ivAvatar = baseViewHolder.getView(R.id.iv_header);
                    if(!TextUtils.isEmpty(personInfo.getHeader_img_url()) && !isScrolling) {
                        ImageUtil.setCircleImageView(ivAvatar, personInfo.getHeader_img_url(), R.mipmap.icon_user_def_colorized, context);
                    } else{
                        if(TextUtils.equals("男",personInfo.getSex())){
                            ImageUtil.setCircleImageView(ivAvatar,"",R.mipmap.icon_man_def_colorized,context);
                        }else if(TextUtils.equals("女",personInfo.getSex())){
                            ImageUtil.setCircleImageView(ivAvatar,"",R.mipmap.icon_woman_def_colorized,context);
                        }else {
                            ImageUtil.setCircleImageView(ivAvatar,"",R.mipmap.icon_user_def_colorized,context);
                        }
                    }
                }
                break;
        }

    }

}
