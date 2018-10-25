package com.wulee.administrator.zuji.ui.pushmsg;


import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.database.bean.PushMessage;
import com.wulee.administrator.zuji.utils.DateTimeUtils;
import com.wulee.administrator.zuji.utils.ImageUtil;

import java.util.ArrayList;


public class MsgListAdapter extends BaseQuickAdapter<PushMessage,BaseViewHolder> {

    private Context mContext;

    public MsgListAdapter(int layoutResId, ArrayList<PushMessage> dataList,Context context) {
        super(layoutResId, dataList);
        this.mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, final PushMessage msg) {

        baseViewHolder.setText(R.id.tv_content,msg.getContent());
        if(msg.getTime()!=null && msg.getTime() >0 ){
            baseViewHolder.setText(R.id.tv_time, DateTimeUtils.getStringDateTime(msg.getTime()));
        }
        String msgType = msg.getType();
        ImageView ivMsgType =  baseViewHolder.getView(R.id.iv_msg_type);
        if(TextUtils.equals(msgType,PushMessage.MSG_TYPE_SYSTEM)){
            ImageUtil.setDefaultImageView(ivMsgType, "", R.mipmap.icon_system_msg, mContext);
        }else if(TextUtils.equals(msgType,PushMessage.MSG_TYPE_LOCATION)){
            ImageUtil.setDefaultImageView(ivMsgType, "", R.mipmap.icon_location_msg, mContext);
        }
    }
}
