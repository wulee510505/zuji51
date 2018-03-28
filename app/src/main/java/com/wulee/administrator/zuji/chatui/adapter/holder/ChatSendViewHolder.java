package com.wulee.administrator.zuji.chatui.adapter.holder;

import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.chatui.adapter.ChatAdapter;
import com.wulee.administrator.zuji.chatui.enity.MessageInfo;
import com.wulee.administrator.zuji.chatui.util.Constants;
import com.wulee.administrator.zuji.chatui.util.Utils;
import com.wulee.administrator.zuji.chatui.widget.BubbleImageView;
import com.wulee.administrator.zuji.chatui.widget.GifTextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 作者：Rance on 2016/11/29 10:47
 * 邮箱：rance935@163.com
 */
public class ChatSendViewHolder extends BaseViewHolder<MessageInfo> {


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
    @InjectView(R.id.chat_item_layout)
    RelativeLayout chatItemLayout;
    @InjectView(R.id.chat_item_voice_time)
    TextView chatItemVoiceTime;
    @InjectView(R.id.chat_item_fail)
    ImageView chatItemFail;
    @InjectView(R.id.chat_item_progress)
    ProgressBar chatItemProgress;
    private ChatAdapter.onItemClickListener onItemClickListener;
    private Handler handler;

    public ChatSendViewHolder(ViewGroup parent, ChatAdapter.onItemClickListener onItemClickListener, Handler handler) {
        super(parent, R.layout.item_chat_send);
        ButterKnife.inject(this, itemView);
        this.onItemClickListener = onItemClickListener;
        this.handler = handler;
    }


    @Override
    public void setData(MessageInfo data) {
        chatItemDate.setText(data.getTime() != null ? data.getTime() : "");
        Glide.with(getContext()).load(data.getHeader()).into(chatItemHeader);
        chatItemHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onHeaderClick(getDataPosition());
            }
        });
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
            Glide.with(getContext()).load(data.getImageUrl()).into(chatItemContentImage);
            chatItemContentImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onImageClick(chatItemContentImage, getDataPosition());
                }
            });
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
        switch (data.getSendState()) {
            case Constants.CHAT_ITEM_SENDING:
                chatItemProgress.setVisibility(View.VISIBLE);
                chatItemFail.setVisibility(View.GONE);
                break;
            case Constants.CHAT_ITEM_SEND_ERROR:
                chatItemProgress.setVisibility(View.GONE);
                chatItemFail.setVisibility(View.VISIBLE);
                break;
            case Constants.CHAT_ITEM_SEND_SUCCESS:
                chatItemProgress.setVisibility(View.GONE);
                chatItemFail.setVisibility(View.GONE);
                break;
        }
    }
}
