package com.wulee.administrator.zuji.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.database.bean.PersonInfo;
import com.wulee.administrator.zuji.entity.StepInfo;
import com.wulee.administrator.zuji.ui.PersonalInfoActivity;
import com.wulee.administrator.zuji.ui.UserInfoActivity;
import com.wulee.administrator.zuji.utils.ImageUtil;
import com.wulee.administrator.zuji.utils.NoFastClickUtils;

import java.util.List;


public class StepRankingAdapter extends BaseQuickAdapter<StepInfo,BaseViewHolder> {

    private Context mcontext;

    public StepRankingAdapter(Context context, int layoutResId, List<StepInfo> stepList) {
        super(layoutResId, stepList);
        this.mcontext = context;
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, final StepInfo stepInfo) {

        PersonInfo piInfo = stepInfo.personInfo;
        if(null != piInfo){
            if(!TextUtils.isEmpty(piInfo.getName()))
                baseViewHolder.setText(R.id.tv_name,piInfo.getName());
            else
                baseViewHolder.setText(R.id.tv_name,"游客");
            ImageView ivHeader = baseViewHolder.getView(R.id.iv_header);
            ImageUtil.setCircleImageView(ivHeader,piInfo.getHeader_img_url(),R.mipmap.icon_user_def,mcontext);

            PersonInfo currPiInfo = PersonInfo.getCurrentUser(PersonInfo.class);

            ivHeader.setOnClickListener(view -> {
                if (NoFastClickUtils.isFastClick()) {
                    return;
                }
                if (null != currPiInfo) {
                    Intent intent = null;
                    if (TextUtils.equals(currPiInfo.getUsername(), piInfo.getUsername())) {
                        intent = new Intent(mcontext, PersonalInfoActivity.class);
                    } else {
                        intent = new Intent(mcontext, UserInfoActivity.class);
                        intent.putExtra("piInfo", stepInfo.personInfo);
                    }
                    mcontext.startActivity(intent);
                }
            });
        }else{
            baseViewHolder.setText(R.id.tv_name,"游客");
        }


        TextView tvStepCount = baseViewHolder.getView(R.id.tv_step);
        String stepCount = "<font color='#FF4081'><big><big> " + stepInfo.getCount() + "</big></big></font> 步";
        tvStepCount.setText(Html.fromHtml(stepCount));

        baseViewHolder.setText(R.id.tv_ranking,(baseViewHolder.getLayoutPosition()+ 1)+"");
    }
}
