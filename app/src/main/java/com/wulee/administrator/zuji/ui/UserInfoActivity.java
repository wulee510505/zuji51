package com.wulee.administrator.zuji.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.base.BaseActivity;
import com.wulee.administrator.zuji.chatui.enity.AddFriendMessage;
import com.wulee.administrator.zuji.chatui.enity.Friend;
import com.wulee.administrator.zuji.chatui.model.UserModel;
import com.wulee.administrator.zuji.chatui.ui.activity.ChatMainActivity;
import com.wulee.administrator.zuji.database.bean.PersonInfo;
import com.wulee.administrator.zuji.utils.ImageUtil;
import com.wulee.administrator.zuji.utils.OtherUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.core.ConnectionStatus;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;


/**
 * Created by wulee on 2017/9/19 09:25
 */

public class UserInfoActivity extends BaseActivity {


    @InjectView(R.id.iv_back)
    ImageView ivBack;
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.user_photo)
    ImageView userPhoto;
    @InjectView(R.id.tv_name)
    TextView tvName;
    @InjectView(R.id.tv_gender)
    TextView tvGender;
    @InjectView(R.id.btn_message_board)
    Button btnMessageBoard;
    @InjectView(R.id.rl_circle)
    RelativeLayout rlCircle;
    @InjectView(R.id.tv_birthday)
    TextView tvBirthday;
    @InjectView(R.id.btn_add_friend)
    Button btnAddFriend;
    @InjectView(R.id.btn_send_msg_to_stranger)
    Button btnSendMsgToStranger;

    private PersonInfo personInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_info_activity);
        ButterKnife.inject(this);

        initData();
    }


    private void initData() {
        title.setText("个人中心");
        personInfo = (PersonInfo) getIntent().getSerializableExtra("piInfo");
        if (null != personInfo) {
            ImageUtil.setCircleImageView(userPhoto, personInfo.getHeader_img_url(), R.mipmap.icon_user_def, this);

            if (!TextUtils.isEmpty(personInfo.getName()))
                tvName.setText(personInfo.getName());
            else
                tvName.setText("游客");

            if (!TextUtils.isEmpty(personInfo.getSex()))
                tvGender.setText(personInfo.getSex());
            else
                tvGender.setText("其他");

            if (!TextUtils.isEmpty(personInfo.getBirthday())) {
                tvBirthday.setText(personInfo.getBirthday());
            } else {
                tvBirthday.setText("未选择");
            }

            final boolean[] isMyFiend = {false};
            UserModel.getInstance().queryFriends(new FindListener<Friend>() {
                @Override
                public void done(List<Friend> list, BmobException e) {
                    if (e == null) {
                        if (list != null && list.size() > 0) {
                            for (Friend friend : list) {
                                if (TextUtils.equals(friend.getFriendUser().getObjectId(), personInfo.getObjectId())) {
                                    isMyFiend[0] = true;
                                }
                            }
                        }
                        if (isMyFiend[0]) {
                            btnAddFriend.setVisibility(View.GONE);
                            btnSendMsgToStranger.setVisibility(View.GONE);
                        } else {
                            btnAddFriend.setVisibility(View.VISIBLE);
                            btnSendMsgToStranger.setVisibility(View.VISIBLE);
                        }
                    }else if(TextUtils.equals("暂无联系人",e.toString())){
                        btnAddFriend.setVisibility(View.VISIBLE);
                        btnSendMsgToStranger.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }


    @OnClick({R.id.iv_back, R.id.btn_message_board, R.id.rl_circle, R.id.btn_send_msg_to_stranger, R.id.btn_add_friend,R.id.user_photo})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.btn_add_friend:
                sendAddFriendMessage();
                break;
            case R.id.btn_send_msg_to_stranger:
                if (personInfo != null  && personInfo.getObjectId()!= null)
                    chat(personInfo.getObjectId(), !TextUtils.isEmpty(personInfo.getName()) ? personInfo.getName() : personInfo.getUsername(), personInfo.getHeader_img_url());
                break;
            case R.id.btn_message_board:
                startActivity(new Intent(this, MessageBoardActivity.class).putExtra("piInfo", personInfo));
                break;
            case R.id.rl_circle:
                startActivity(new Intent(this, PrivateCircleActivity.class).putExtra("piInfo", personInfo));
                break;
            case R.id.user_photo:
                if(personInfo != null){
                    startActivity(new Intent(this, BigSingleImgActivity.class).putExtra(BigSingleImgActivity.IMAGE_URL, personInfo.getHeader_img_url()));
                }
                break;
        }
    }

    /**
     * 发送添加好友的请求
     */
    //TODO 好友管理：9.7、发送添加好友请求
    private void sendAddFriendMessage() {
        //TODO 会话：4.1、创建一个暂态会话入口，发送好友请求
        if (personInfo != null && personInfo.getObjectId()!= null && BmobIM.getInstance().getCurrentStatus().getCode() == ConnectionStatus.CONNECTED.getCode()) {
            BmobIMUserInfo info = new BmobIMUserInfo(personInfo.getObjectId(), !TextUtils.isEmpty(personInfo.getName()) ? personInfo.getName() : personInfo.getUsername(), personInfo.getHeader_img_url());
            BmobIMConversation conversationEntrance = BmobIM.getInstance().startPrivateConversation(info, true, null);
            //TODO 消息：5.1、根据会话入口获取消息管理，发送好友请求
            BmobIMConversation messageManager = BmobIMConversation.obtain(BmobIMClient.getInstance(), conversationEntrance);
            AddFriendMessage msg = new AddFriendMessage();
            msg.setContent("很高兴认识你，可以加个好友吗?");//给对方的一个留言信息
            PersonInfo currentUser = BmobUser.getCurrentUser(PersonInfo.class);
            if(currentUser != null){
                Map<String, Object> map = new HashMap<>();
                map.put("name", currentUser.getUsername());//发送者姓名
                map.put("avatar", currentUser.getHeader_img_url());//发送者的头像
                map.put("uid", currentUser.getObjectId());//发送者的uid
                msg.setExtraMap(map);
            }
            messageManager.sendMessage(msg, new MessageSendListener() {
                @Override
                public void done(BmobIMMessage msg, BmobException e) {
                    if (e == null) {//发送成功
                        toast("好友请求发送成功，等待验证");
                    } else {//发送失败
                        toast("发送失败:" + e.getMessage());
                    }
                }
            });
        }
    }

    /**
     * 与陌生人聊天
     */
    private void chat(String objectId, String name, String avatar) {
        if (BmobIM.getInstance().getCurrentStatus().getCode() != ConnectionStatus.CONNECTED.getCode()) {
            OtherUtil.showToastText("尚未连接IM服务器");
            return;
        }
        Intent intent = new Intent(this, ChatMainActivity.class);
        //创建一个常态会话入口，陌生人聊天
        BmobIMUserInfo info = new BmobIMUserInfo(objectId, name, avatar);
        BmobIMConversation conversationEntrance = BmobIM.getInstance().startPrivateConversation(info, null);
        Bundle bundle = new Bundle();
        bundle.putSerializable("c", conversationEntrance);
        PersonInfo userInfo = new PersonInfo();
        userInfo.setObjectId(objectId);
        userInfo.setName(name);
        userInfo.setHeader_img_url(avatar);
        bundle.putSerializable("piInfo", userInfo);
        bundle.putInt("type",1);
        intent.putExtras(bundle);
        startActivity(intent);
    }

}
