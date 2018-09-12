package com.wulee.administrator.zuji.chatui.adapter.holder;

import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.chatui.adapter.ChatAdapter;
import com.wulee.administrator.zuji.chatui.enity.MessageInfo;
import com.wulee.administrator.zuji.chatui.util.Utils;
import com.wulee.administrator.zuji.chatui.widget.BubbleImageView;
import com.wulee.administrator.zuji.chatui.widget.GifTextView;
import com.wulee.administrator.zuji.utils.ImageUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 作者：Rance on 2016/11/29 10:47
 * 邮箱：rance935@163.com
 */
public class ChatAcceptViewHolder extends BaseViewHolder<MessageInfo> {

    @InjectView(R.id.chat_item_date)
    TextView chatItemDate;
    @InjectView(R.id.chat_item_header)
    ImageView chatItemHeader;
    @InjectView(R.id.chat_item_content_image)
    BubbleImageView chatItemContentImage;
    @InjectView(R.id.chat_item_content_text)
    GifTextView chatItemContentText;
    @InjectView(R.id.chat_item_voice)
    ImageView chatItemVoice;
    @InjectView(R.id.chat_item_layout_content)
    LinearLayout chatItemLayoutContent;
    @InjectView(R.id.chat_item_voice_time)
    TextView chatItemVoiceTime;
    private ChatAdapter.onItemClickListener onItemClickListener;
    private Handler handler;

    public ChatAcceptViewHolder(ViewGroup parent, ChatAdapter.onItemClickListener onItemClickListener, Handler handler) {
        super(parent, R.layout.item_chat_accept);
        ButterKnife.inject(this, itemView);
        this.onItemClickListener = onItemClickListener;
        this.handler = handler;
    }

    @Override
    public void setData(MessageInfo data) {
        chatItemDate.setText(data.getTime() != null ? data.getTime() : "");
        ImageUtil.setCircleImageView(chatItemHeader,data.getHeader(),R.mipmap.bg_pic_def_rect,getContext());
        chatItemHeader.setOnClickListener(v -> onItemClickListener.onHeaderClick(getDataPosition()));
        if (data.getContent() != null) {
            chatItemContentText.setSpanText(handler, data.getContent(), true);
            chatItemVoice.setVisibility(View.GONE);
            chatItemContentText.setVisibility(View.VISIBLE);
            chatItemLayoutContent.setVisibility(View.VISIBLE);
            chatItemVoiceTime.setVisibility(View.GONE);
            chatItemContentImage.setVisibility(View.GONE);
        } else if (data.getImageUrl() != null) {
            chatItemVoice.setVisibility(View.GONE);
            chatItemLayoutContent.setVisibility(View.GONE);
            chatItemVoiceTime.setVisibility(View.GONE);
            chatItemContentText.setVisibility(View.GONE);
            chatItemContentImage.setVisibility(View.VISIBLE);
            ImageUtil.setDefaultImageView(chatItemContentImage,data.getImageUrl(),R.mipmap.bg_pic_def_rect,getContext());
            chatItemContentImage.setOnClickListener(v -> onItemClickListener.onImageClick(chatItemContentImage, getDataPosition()));
        } else if (data.getFilepath() != null) {
            chatItemVoice.setVisibility(View.VISIBLE);
            chatItemLayoutContent.setVisibility(View.VISIBLE);
            chatItemContentText.setVisibility(View.GONE);
            chatItemVoiceTime.setVisibility(View.VISIBLE);
            chatItemContentImage.setVisibility(View.GONE);
            chatItemVoiceTime.setText(Utils.formatTime(data.getVoiceTime()));
            chatItemLayoutContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onVoiceClick(chatItemVoice, getDataPosition());
                }
            });
        }
    }
}
