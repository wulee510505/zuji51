package com.wulee.administrator.zuji.chatui.ui.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huxq17.swipecardsview.LogUtil;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.base.BaseActivity;
import com.wulee.administrator.zuji.chatui.adapter.ChatAdapter;
import com.wulee.administrator.zuji.chatui.adapter.CommonFragmentPagerAdapter;
import com.wulee.administrator.zuji.chatui.enity.FullImageInfo;
import com.wulee.administrator.zuji.chatui.enity.MessageInfo;
import com.wulee.administrator.zuji.chatui.ui.fragment.ChatEmotionFragment;
import com.wulee.administrator.zuji.chatui.ui.fragment.ChatFunctionFragment;
import com.wulee.administrator.zuji.chatui.util.Constants;
import com.wulee.administrator.zuji.chatui.util.GlobalOnItemClickManagerUtils;
import com.wulee.administrator.zuji.chatui.util.MediaManager;
import com.wulee.administrator.zuji.chatui.widget.EmotionInputDetector;
import com.wulee.administrator.zuji.chatui.widget.NoScrollViewPager;
import com.wulee.administrator.zuji.chatui.widget.StateButton;
import com.wulee.administrator.zuji.database.bean.PersonInfo;
import com.wulee.administrator.zuji.ui.PersonalInfoActivity;
import com.wulee.administrator.zuji.ui.UserInfoActivity;
import com.wulee.administrator.zuji.utils.NoFastClickUtils;
import com.wulee.administrator.zuji.utils.OtherUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMAudioMessage;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMImageMessage;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMTextMessage;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.listener.MessageListHandler;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.newim.listener.MessagesQueryListener;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * 作者：Rance on 2016/11/29 10:47
 * 邮箱：rance935@163.com
 */
public class ChatMainActivity extends BaseActivity implements MessageListHandler {


    @InjectView(R.id.chat_list)
    RecyclerView chatList;
    @InjectView(R.id.emotion_voice)
    ImageView emotionVoice;
    @InjectView(R.id.edit_text)
    EditText editText;
    @InjectView(R.id.voice_text)
    TextView voiceText;
    @InjectView(R.id.emotion_button)
    ImageView emotionButton;
    @InjectView(R.id.emotion_add)
    ImageView emotionAdd;
    @InjectView(R.id.emotion_send)
    StateButton emotionSend;
    @InjectView(R.id.viewpager)
    NoScrollViewPager viewpager;
    @InjectView(R.id.emotion_layout)
    RelativeLayout emotionLayout;
    @InjectView(R.id.iv_back)
    ImageView ivBack;
    @InjectView(R.id.title)
    TextView title;
    private EmotionInputDetector mDetector;
    private ArrayList<Fragment> fragments;
    private ChatEmotionFragment chatEmotionFragment;
    private ChatFunctionFragment chatFunctionFragment;
    private CommonFragmentPagerAdapter adapter;

    private ChatAdapter chatAdapter;
    private LinearLayoutManager layoutManager;
    private List<MessageInfo> messageInfos;
    //录音相关
    int animationRes = 0;
    int res = 0;
    AnimationDrawable animationDrawable = null;
    private ImageView animView;

    BmobIMConversation mConversationManager;
    private String mConversationId;
    private PersonInfo currPiInfo;
    private PersonInfo userInfo;
    private int mType; //0 好友，1 陌生人

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_main);
        ButterKnife.inject(this);

        BmobIMConversation conversationEntrance = (BmobIMConversation) getIntent().getExtras().getSerializable("c");
        //TODO 消息：5.1、根据会话入口获取消息管理，聊天页面
        mConversationManager = BmobIMConversation.obtain(BmobIMClient.getInstance(), conversationEntrance);
        userInfo =  (PersonInfo) getIntent().getSerializableExtra("piInfo");
        mType   =  getIntent().getIntExtra("type",-1);

        currPiInfo = BmobUser.getCurrentUser(PersonInfo.class);

        EventBus.getDefault().register(this);
        initWidget();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BmobIM.getInstance().addMessageListHandler(this);
    }

    private void initWidget() {
        mConversationId = mConversationManager.getConversationId();
        if(mType == 0)
           title.setText(mConversationManager.getConversationTitle());
        else
           title.setText("匿名用户");

        fragments = new ArrayList<>();
        chatEmotionFragment = new ChatEmotionFragment();
        fragments.add(chatEmotionFragment);
        chatFunctionFragment = new ChatFunctionFragment();
        fragments.add(chatFunctionFragment);
        adapter = new CommonFragmentPagerAdapter(getSupportFragmentManager(), fragments);
        viewpager.setAdapter(adapter);
        viewpager.setCurrentItem(0);

        mDetector = EmotionInputDetector.with(this)
                .setEmotionView(emotionLayout)
                .setViewPager(viewpager)
                .bindToContent(chatList)
                .bindToEditText(editText)
                .bindToEmotionButton(emotionButton)
                .bindToAddButton(emotionAdd)
                .bindToSendButton(emotionSend)
                .bindToVoiceButton(emotionVoice)
                .bindToVoiceText(voiceText)
                .build();

        GlobalOnItemClickManagerUtils globalOnItemClickListener = GlobalOnItemClickManagerUtils.getInstance(this);
        globalOnItemClickListener.attachToEditText(editText);

        chatAdapter = new ChatAdapter(this);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        chatList.setLayoutManager(layoutManager);
        chatList.setAdapter(chatAdapter);
        chatList.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        chatAdapter.handler.removeCallbacksAndMessages(null);
                        chatAdapter.notifyDataSetChanged();
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        chatAdapter.handler.removeCallbacksAndMessages(null);
                        mDetector.hideEmotionLayout(false);
                        mDetector.hideSoftInput();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        chatAdapter.addItemClickListener(itemClickListener);
        LoadData();
    }

    /**
     * item点击事件
     */
    private ChatAdapter.onItemClickListener itemClickListener = new ChatAdapter.onItemClickListener() {
        @Override
        public void onHeaderClick(int position) {
            if (NoFastClickUtils.isFastClick()) {
                return;
            }
            Intent intent = null;
            MessageInfo msgInfo = chatAdapter.getItem(position);
            if(msgInfo.getType() == Constants.CHAT_ITEM_TYPE_RIGHT){
                intent = new Intent(ChatMainActivity.this, PersonalInfoActivity.class);
            }else{
                intent = new Intent(ChatMainActivity.this, UserInfoActivity.class);
                intent.putExtra("piInfo",userInfo);
            }
            startActivity(intent);
        }

        @Override
        public void onImageClick(View view, int position) {
            int location[] = new int[2];
            view.getLocationOnScreen(location);
            FullImageInfo fullImageInfo = new FullImageInfo();
            fullImageInfo.setLocationX(location[0]);
            fullImageInfo.setLocationY(location[1]);
            fullImageInfo.setWidth(view.getWidth());
            fullImageInfo.setHeight(view.getHeight());
            fullImageInfo.setImageUrl(messageInfos.get(position).getImageUrl());
            EventBus.getDefault().postSticky(fullImageInfo);
            startActivity(new Intent(ChatMainActivity.this, FullImageActivity.class));
            overridePendingTransition(0, 0);
        }

        @Override
        public void onVoiceClick(final ImageView imageView, final int position) {
            if (animView != null) {
                animView.setImageResource(res);
                animView = null;
            }
            switch (messageInfos.get(position).getType()) {
                case 1:
                    animationRes = R.drawable.voice_left;
                    res = R.mipmap.icon_voice_left3;
                    break;
                case 2:
                    animationRes = R.drawable.voice_right;
                    res = R.mipmap.icon_voice_right3;
                    break;
            }
            animView = imageView;
            animView.setImageResource(animationRes);
            animationDrawable = (AnimationDrawable) imageView.getDrawable();
            animationDrawable.start();
            MediaManager.playSound(messageInfos.get(position).getFilepath(), mp -> animView.setImageResource(res));
        }
    };

    /**
     * 构造聊天数据
     */
    private void LoadData() {
        messageInfos = new ArrayList<>();

        /*MessageInfo messageInfo = new MessageInfo();
        messageInfo.setContent("你好，欢迎使用Rance的聊天界面框架");
        messageInfo.setType(Constants.CHAT_ITEM_TYPE_LEFT);
        messageInfo.setHeader("http://tupian.enterdesk.com/2014/mxy/11/2/1/12.jpg");
        messageInfos.add(messageInfo);

        MessageInfo messageInfo1 = new MessageInfo();
        messageInfo1.setFilepath("http://www.trueme.net/bb_midi/welcome.wav");
        messageInfo1.setVoiceTime(3000);
        messageInfo1.setType(Constants.CHAT_ITEM_TYPE_RIGHT);
        messageInfo1.setSendState(Constants.CHAT_ITEM_SEND_SUCCESS);
        messageInfo1.setHeader("http://img.dongqiudi.com/uploads/avatar/2014/10/20/8MCTb0WBFG_thumb_1413805282863.jpg");
        messageInfos.add(messageInfo1);

        MessageInfo messageInfo2 = new MessageInfo();
        messageInfo2.setImageUrl("http://img4.imgtn.bdimg.com/it/u=1800788429,176707229&fm=21&gp=0.jpg");
        messageInfo2.setType(Constants.CHAT_ITEM_TYPE_LEFT);
        messageInfo2.setHeader("http://tupian.enterdesk.com/2014/mxy/11/2/1/12.jpg");
        messageInfos.add(messageInfo2);

        MessageInfo messageInfo3 = new MessageInfo();
        messageInfo3.setContent("[微笑][色][色][色]");
        messageInfo3.setType(Constants.CHAT_ITEM_TYPE_RIGHT);
        messageInfo3.setSendState(Constants.CHAT_ITEM_SEND_ERROR);
        messageInfo3.setHeader("http://img.dongqiudi.com/uploads/avatar/2014/10/20/8MCTb0WBFG_thumb_1413805282863.jpg");
        messageInfos.add(messageInfo3);

        chatAdapter.addAll(messageInfos);*/


        mConversationManager.queryMessages(null, 20, new MessagesQueryListener() {
            @Override
            public void done(List<BmobIMMessage> list, BmobException e) {
                if (e == null) {
                    if (null != list && list.size() > 0) {
                        for (int i = 0; i < list.size(); i++) {
                            BmobIMMessage message = list.get(i);

                            MessageInfo messageInfo = new MessageInfo();
                            if(TextUtils.equals("txt",message.getMsgType())){
                                messageInfo.setContent(message.getContent());
                            }else if(TextUtils.equals("image",message.getMsgType())){
                                messageInfo.setImageUrl(message.getContent());
                            }else if(TextUtils.equals("sound",message.getMsgType())){
                                messageInfo.setFilepath(message.getContent());
                            }
                            if(TextUtils.equals(mConversationId,message.getFromId())){
                                messageInfo.setType(Constants.CHAT_ITEM_TYPE_LEFT);
                                if(userInfo != null && !TextUtils.isEmpty(userInfo.getHeader_img_url()))
                                    messageInfo.setHeader(userInfo.getHeader_img_url());
                            }else  if(TextUtils.equals(mConversationId,message.getToId())){
                                messageInfo.setType(Constants.CHAT_ITEM_TYPE_RIGHT);
                                if(currPiInfo != null)
                                   messageInfo.setHeader(currPiInfo.getHeader_img_url());
                            }
                            messageInfo.setSendState(message.getSendStatus());


                            messageInfos.add(messageInfo);
                        }
                        chatAdapter.addAll(messageInfos);
                        chatAdapter.notifyDataSetChanged();
                        layoutManager.scrollToPositionWithOffset(list.size() - 1, 0);
                    }
                } else {
                    Toast.makeText(ChatMainActivity.this, e.getMessage() + "(" + e.getErrorCode() + ")", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void MessageEventBus(final MessageInfo messageInfo) {
        if(currPiInfo != null)
            messageInfo.setHeader(currPiInfo.getHeader_img_url());
        messageInfo.setType(Constants.CHAT_ITEM_TYPE_RIGHT);
        messageInfo.setSendState(Constants.CHAT_ITEM_SENDING);
        messageInfos.add(messageInfo);
        chatAdapter.add(messageInfo);
        chatList.scrollToPosition(chatAdapter.getCount() - 1);
        new Handler().postDelayed(() -> {
            messageInfo.setSendState(Constants.CHAT_ITEM_SEND_SUCCESS);
            chatAdapter.notifyDataSetChanged();
        }, 2000);

        if(!TextUtils.isEmpty(messageInfo.getContent())){
             sendMessage();
        }
        if(!TextUtils.isEmpty(messageInfo.getImageUrl())){
            sendLocalImageMessage(messageInfo.getImageUrl());
        }
        if(messageInfo.getVoiceTime()>0){
            sendLocalAudioMessage(messageInfo.getFilepath());
        }
    }

    @Override
    public void onBackPressed() {
        if (!mDetector.interceptBackPress()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().removeStickyEvent(this);
        EventBus.getDefault().unregister(this);
    }

    private void scrollToBottom() {
        layoutManager.scrollToPositionWithOffset(chatAdapter.getItemCount() - 1, 0);
    }

    /**
     * 消息发送监听器
     */
    public MessageSendListener listener = new MessageSendListener() {
        @Override
        public void onProgress(int value) {
            super.onProgress(value);
            //文件类型的消息才有进度值
            LogUtil.i("onProgress：" + value);
        }

        @Override
        public void onStart(BmobIMMessage msg) {
            super.onStart(msg);
            //chatAdapter.add(new MessageInfo());
            editText.setText("");
            scrollToBottom();
        }

        @Override
        public void done(BmobIMMessage msg, BmobException e) {
            adapter.notifyDataSetChanged();
            editText.setText("");
            scrollToBottom();
            if (e != null) {
                OtherUtil.showToastText(e.getMessage());
            }
        }
    };


    /**
     * 发送文本消息
     */
    private void sendMessage() {
        String text = editText.getText().toString();
        if (TextUtils.isEmpty(text.trim())) {
            OtherUtil.showToastText("请输入内容");
            return;
        }
        //TODO 发送消息：6.1、发送文本消息
        BmobIMTextMessage msg = new BmobIMTextMessage();
        msg.setContent(text);
        //可随意设置额外信息
        Map<String, Object> map = new HashMap<>();
        map.put("level", "1");
        msg.setExtraMap(map);
        msg.setExtra("OK");
        mConversationManager.sendMessage(msg, listener);
    }


    /**
     * 发送本地图片文件
     */
    public void sendLocalImageMessage(String imgurl) {
        //TODO 发送消息：6.2、发送本地图片消息
        //正常情况下，需要调用系统的图库或拍照功能获取到图片的本地地址，开发者只需要将本地的文件地址传过去就可以发送文件类型的消息
        BmobIMImageMessage image = new BmobIMImageMessage();
        image.setLocalPath(imgurl);
        mConversationManager.sendMessage(image, listener);
    }

    /**
     * 发送本地音频文件
     */
    private void sendLocalAudioMessage(String audioPath) {
        //TODO 发送消息：6.4、发送本地音频文件消息
        BmobIMAudioMessage audio = new BmobIMAudioMessage(audioPath);
        mConversationManager.sendMessage(audio, listener);
    }


    @Override
    public void onPause() {
        super.onPause();
        BmobIM.getInstance().removeMessageListHandler(this);
    }

    @Override
    public void onMessageReceive(List<MessageEvent> list) {
        LogUtil.i("聊天页面接收到消息：" + list.size());
        //当注册页面消息监听时候，有消息（包含离线消息）到来时会回调该方法
        for (int i = 0; i < list.size(); i++) {
            addMessage2Chat(list.get(i));
        }
    }


    /**
     * 添加消息到聊天界面中
     * @param event
     */
    private void addMessage2Chat(MessageEvent event) {
        BmobIMMessage msg = event.getMessage();
        if (mConversationManager != null && event != null && mConversationManager.getConversationId().equals(event.getConversation().getConversationId()) //如果是当前会话的消息
                && !msg.isTransient()) {//并且不为暂态消息
            if (chatAdapter.findPosition(msg) < 0) {//如果未添加到界面中
                MessageInfo messageInfo = new MessageInfo();
                if(TextUtils.equals("txt",msg.getMsgType())){
                    messageInfo.setContent(msg.getContent());
                }else if(TextUtils.equals("image",msg.getMsgType())){
                    messageInfo.setImageUrl(msg.getContent());
                }else if(TextUtils.equals("sound",msg.getMsgType())){
                    messageInfo.setFilepath(msg.getContent());
                }
                messageInfo.setType(Constants.CHAT_ITEM_TYPE_LEFT);
                if(msg.getBmobIMUserInfo() != null){
                    messageInfo.setHeader(msg.getBmobIMUserInfo().getAvatar());
                }
                messageInfo.setSendState(msg.getSendStatus());
                messageInfos.add(messageInfo);

                //更新该会话下面的已读状态
                mConversationManager.updateReceiveStatus(msg);
            }
            chatAdapter.addAll(messageInfos);
            chatAdapter.notifyDataSetChanged();
            scrollToBottom();
        } else {
            LogUtil.i("不是与当前聊天对象的消息");
        }
    }

    @OnClick(R.id.iv_back)
    public void onViewClicked() {
        finish();
    }
}
