package com.wulee.administrator.zuji.chatui.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huxq17.swipecardsview.LogUtil;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.base.BaseActivity;
import com.wulee.administrator.zuji.chatui.enity.AgreeAddFriendMessage;
import com.wulee.administrator.zuji.chatui.enity.AgreeAddLinkmanMessage;
import com.wulee.administrator.zuji.chatui.model.UserModel;
import com.wulee.administrator.zuji.database.NewFriendManager;
import com.wulee.administrator.zuji.database.bean.NewFriendInfo;
import com.wulee.administrator.zuji.database.bean.PersonInfo;
import com.wulee.administrator.zuji.entity.Constant;
import com.wulee.administrator.zuji.entity.LinkmanInfo;
import com.wulee.administrator.zuji.utils.ImageUtil;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;


/**
 * Created by wulee on 2017/9/19 09:25
 */

public class AddAgreeActivity extends BaseActivity {


    @InjectView(R.id.iv_back)
    ImageView ivBack;
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.titlelayout)
    RelativeLayout titlelayout;
    @InjectView(R.id.user_photo)
    ImageView userPhoto;
    @InjectView(R.id.tv_name)
    TextView tvName;
    @InjectView(R.id.rl_name)
    RelativeLayout rlName;
    @InjectView(R.id.btn_agree_add_friend)
    Button btnAgreeAddFriend;
    private NewFriendInfo mNewFriendInfo;
    private LinkmanInfo mLinkmanInfo;

    public static final int INTENT_TYPE_ADD_FRIEND = 101;
    public static final int INTENT_TYPE_ADD_LINKMAN = 102;
    private int mType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_friend_agree_activity);
        ButterKnife.inject(this);

        initData();
    }


    private void initData() {
        title.setText("个人中心");
        mType =  getIntent().getIntExtra("type",-1);
        if(mType == INTENT_TYPE_ADD_FRIEND){
            btnAgreeAddFriend.setText("同意添加为好友");
            mNewFriendInfo = (NewFriendInfo) getIntent().getSerializableExtra("new_friend_info");
            if (null != mNewFriendInfo) {
                ImageUtil.setCircleImageView(userPhoto, mNewFriendInfo.getAvatar(), R.mipmap.icon_user_def, this);

                if (!TextUtils.isEmpty(mNewFriendInfo.getName()))
                    tvName.setText(mNewFriendInfo.getName());
                else
                    tvName.setText("游客");
            }
        }else if(mType == INTENT_TYPE_ADD_LINKMAN){
            btnAgreeAddFriend.setText("同意被添加为紧急联系人");
            mLinkmanInfo = (LinkmanInfo) getIntent().getSerializableExtra("linkman_info");
            if (null != mLinkmanInfo) {
                ImageUtil.setCircleImageView(userPhoto, mLinkmanInfo.getAvatar(), R.mipmap.icon_user_def, this);

                if (!TextUtils.isEmpty(mLinkmanInfo.getName()))
                    tvName.setText(mLinkmanInfo.getName());
                else
                    tvName.setText("游客");
            }
        }
    }


    @OnClick({R.id.iv_back, R.id.btn_agree_add_friend})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.btn_agree_add_friend:
                if(mType == INTENT_TYPE_ADD_FRIEND){
                    agreeAdd(mNewFriendInfo, new SaveListener<Object>() {
                        @Override
                        public void done(Object o, BmobException e) {
                            if (e == null) {
                                btnAgreeAddFriend.setText("已添加");
                                btnAgreeAddFriend.setEnabled(false);
                            } else {
                                btnAgreeAddFriend.setEnabled(true);
                                LogUtil.e("添加好友失败:" + e.getMessage());
                                toast("添加好友失败:" + e.getMessage());
                            }
                        }
                    });
                }else if(mType == INTENT_TYPE_ADD_LINKMAN){
                    agreeAddLinkman(mLinkmanInfo, new SaveListener<Object>() {
                        @Override
                        public void done(Object o, BmobException e) {
                            if (e == null) {
                                btnAgreeAddFriend.setText("已添加");
                                btnAgreeAddFriend.setEnabled(false);
                            } else {
                                btnAgreeAddFriend.setEnabled(true);
                                LogUtil.e("添加紧急联系人失败:" + e.getMessage());
                                toast("添加紧急联系人失败:" + e.getMessage());
                            }
                        }
                    });
                }

                break;
        }
    }

    /**
     * TODO 好友管理：9.10、添加到好友表中再发送同意添加好友的消息
     *
     * @param add
     * @param listener
     */
    private void agreeAdd(final NewFriendInfo add, final SaveListener<Object> listener) {
        PersonInfo user = new PersonInfo();
        user.setObjectId(add.getUid());
        UserModel.getInstance()
                .agreeAddFriend(user, new SaveListener<String>() {
                    @Override
                    public void done(String s, BmobException e) {
                        if (e == null) {
                            sendAgreeAddFriendMessage(add, listener);
                        } else {
                            LogUtil.e(e.getMessage());
                            listener.done(null, e);
                        }
                    }
                });
    }


    private void agreeAddLinkman(final LinkmanInfo add, final SaveListener<Object> listener) {
        PersonInfo user = new PersonInfo();
        user.setObjectId(add.getUid());
        UserModel.getInstance()
                .agreeAddFriend(user, new SaveListener<String>() {
                    @Override
                    public void done(String s, BmobException e) {
                        if (e == null) {
                            sendAgreeAddLinkmanMessage(add, listener);
                        } else {
                            LogUtil.e(e.getMessage());
                            listener.done(null, e);
                        }
                    }
                });
    }


    /**
     * 发送同意添加好友的消息
     */
    //TODO 好友管理：9.8、发送同意添加好友
    private void sendAgreeAddFriendMessage(final NewFriendInfo add, final SaveListener<Object> listener) {
        BmobIMUserInfo info = new BmobIMUserInfo(add.getUid(), add.getName(), add.getAvatar());
        //TODO 会话：4.1、创建一个暂态会话入口，发送同意好友请求
        BmobIMConversation conversationEntrance = BmobIM.getInstance().startPrivateConversation(info, true, null);
        //TODO 消息：5.1、根据会话入口获取消息管理，发送同意好友请求
        BmobIMConversation messageManager = BmobIMConversation.obtain(BmobIMClient.getInstance(), conversationEntrance);
        //而AgreeAddFriendMessage的isTransient设置为false，表明我希望在对方的会话数据库中保存该类型的消息
        AgreeAddFriendMessage msg = new AgreeAddFriendMessage();
        final PersonInfo currentUser = BmobUser.getCurrentUser(PersonInfo.class);
        msg.setContent("我通过了您的好友验证请求，我们可以开始 聊天了!");//这句话是直接存储到对方的消息表中的
        Map<String, Object> map = new HashMap<>();
        map.put("msg", currentUser.getUsername() + "同意添加您为好友");//显示在通知栏上面的内容
        map.put("uid", add.getUid());//发送者的uid-方便请求添加的发送方找到该条添加好友的请求
        map.put("time", add.getTime());//添加好友的请求时间
        msg.setExtraMap(map);
        messageManager.sendMessage(msg, new MessageSendListener() {
            @Override
            public void done(BmobIMMessage msg, BmobException e) {
                if (e == null) {//发送成功
                    //TODO 3、修改本地的好友请求记录
                    NewFriendManager.getInstance(AddAgreeActivity.this).updateNewFriend(add, Constant.STATUS_VERIFIED);
                    listener.done(msg, e);
                } else {//发送失败
                    LogUtil.e(e.getMessage());
                    listener.done(msg, e);
                }
            }
        });
    }


    /**
     * 发送同意添加联系人的消息
     */
    private void sendAgreeAddLinkmanMessage(final LinkmanInfo add, final SaveListener<Object> listener) {
        BmobIMUserInfo info = new BmobIMUserInfo(add.getUid(), add.getName(), add.getAvatar());
        //TODO 会话：4.1、创建一个暂态会话入口，发送同意好友请求
        BmobIMConversation conversationEntrance = BmobIM.getInstance().startPrivateConversation(info, true, null);
        //TODO 消息：5.1、根据会话入口获取消息管理，发送同意好友请求
        BmobIMConversation messageManager = BmobIMConversation.obtain(BmobIMClient.getInstance(), conversationEntrance);
        //而AgreeAddFriendMessage的isTransient设置为false，表明我希望在对方的会话数据库中保存该类型的消息
        AgreeAddLinkmanMessage msg = new AgreeAddLinkmanMessage();
        final PersonInfo currentUser = BmobUser.getCurrentUser(PersonInfo.class);
        msg.setContent("我通过了您的紧急联系人验证请求!");//这句话是直接存储到对方的消息表中的
        Map<String, Object> map = new HashMap<>();
        map.put("msg", currentUser.getUsername() + "同意被添加为紧急联系人");//显示在通知栏上面的内容
        map.put("uid", add.getUid());//发送者的uid-方便请求添加的发送方找到该条添加好友的请求
        map.put("time", add.getTime());//添加好友的请求时间
        msg.setExtraMap(map);
        messageManager.sendMessage(msg, new MessageSendListener() {
            @Override
            public void done(BmobIMMessage msg, BmobException e) {
                if (e == null) {//发送成功
                    listener.done(msg, e);
                } else {//发送失败
                    LogUtil.e(e.getMessage());
                    listener.done(msg, e);
                }
            }
        });
    }

}
