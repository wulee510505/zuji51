package com.wulee.administrator.zuji.adapter;

import android.content.Context;
import android.view.View;

import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.adapter.base.BaseRecyclerAdapter;
import com.wulee.administrator.zuji.adapter.base.BaseRecyclerHolder;
import com.wulee.administrator.zuji.adapter.base.IMutlipleItem;
import com.wulee.administrator.zuji.entity.Conversation;
import com.wulee.administrator.zuji.utils.DateTimeUtils;

import java.util.Collection;

import cn.bmob.newim.bean.BmobIMConversationType;

/**
 * 使用进一步封装的Conversation,教大家怎么自定义会话列表
 */
public class ConversationAdapter extends BaseRecyclerAdapter<Conversation> {

    public ConversationAdapter(Context context, IMutlipleItem<Conversation> items, Collection<Conversation> datas) {
        super(context,items,datas);
    }

    /**
     * 获取指定会话类型指定会话id的会话位置
     * @param type
     * @param targetId
     * @return
     */
    public int findPosition(BmobIMConversationType type, String targetId) {
        int index = this.getCount();
        int position = -1;
        while(index-- > 0) {
            if((getItem(index)).getcType().equals(type) && (getItem(index)).getcId().equals(targetId)) {
                position = index;
                break;
            }
        }
        return position;
    }

    @Override
    public void bindView(BaseRecyclerHolder holder, Conversation conversation, int position) {
        holder.setText(R.id.tv_recent_msg,conversation.getLastMessageContent());
        holder.setText(R.id.tv_recent_time, DateTimeUtils.getStringDate(conversation.getLastMessageTime()));
        //会话图标
        Object obj = conversation.getAvatar();
        if(obj instanceof String){
            String avatar=(String)obj;
            holder.setImageView(avatar, R.mipmap.icon_user_def, R.id.iv_recent_avatar);
        }else{
            int defaultRes = (int)obj;
            holder.setImageView(null, defaultRes, R.id.iv_recent_avatar);
        }
        //会话标题
        holder.setText(R.id.tv_recent_name, conversation.getcName());
        //查询指定未读消息数
        long unread = conversation.getUnReadCount();
        if(unread>0){
            holder.setVisible(R.id.tv_recent_unread, View.VISIBLE);
            holder.setText(R.id.tv_recent_unread, String.valueOf(unread));
        }else{
            holder.setVisible(R.id.tv_recent_unread, View.GONE);
        }
    }
}