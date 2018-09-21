package com.wulee.administrator.zuji;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

import com.app.hubert.guide.util.LogUtil;
import com.wulee.administrator.zuji.chatui.enity.AddFriendMessage;
import com.wulee.administrator.zuji.chatui.enity.AgreeAddFriendMessage;
import com.wulee.administrator.zuji.chatui.listener.UpdateCacheListener;
import com.wulee.administrator.zuji.chatui.model.UserModel;
import com.wulee.administrator.zuji.chatui.ui.activity.AddFriendAgreeActivity;
import com.wulee.administrator.zuji.chatui.ui.activity.ChatMainActivity;
import com.wulee.administrator.zuji.database.NewFriendManager;
import com.wulee.administrator.zuji.database.bean.NewFriendInfo;
import com.wulee.administrator.zuji.database.bean.PersonInfo;
import com.wulee.administrator.zuji.entity.RefreshEvent;

import java.util.List;
import java.util.Map;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMMessageType;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.event.OfflineMessageEvent;
import cn.bmob.newim.listener.BmobIMMessageHandler;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.util.BmobNotificationManager;
import de.greenrobot.event.EventBus;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by wulee on 2018/3/28.
 * 消息接收器处理在线消息和离线消息
 */

public class IMMessageHandler extends BmobIMMessageHandler {

    private Context context;
    private NotificationManager mNotificationManager;

    public IMMessageHandler(Context context) {
        this.context = context;
        mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onMessageReceive(final MessageEvent event) {
        //在线消息
        executeMessage(event);
    }

    @Override
    public void onOfflineReceive(final OfflineMessageEvent event) {
        //每次调用connect方法时会查询一次离线消息，如果有，此方法会被调用
        Map<String, List<MessageEvent>> map = event.getEventMap();
        LogUtil.i("有" + map.size() + "个用户发来离线消息");
        //挨个检测下离线消息所属的用户的信息是否需要更新
        for (Map.Entry<String, List<MessageEvent>> entry : map.entrySet()) {
            List<MessageEvent> list = entry.getValue();
            int size = list.size();
            LogUtil.i("用户" + entry.getKey() + "发来" + size + "条消息");
            for (int i = 0; i < size; i++) {
                //处理每条消息
                executeMessage(list.get(i));
            }
        }
    }

    /**
     * 处理消息
     *
     * @param event
     */
    private void executeMessage(final MessageEvent event) {
        //检测用户信息是否需要更新
        UserModel.getInstance().updateUserInfo(event, new UpdateCacheListener() {
            @Override
            public void done(BmobException e) {
                BmobIMMessage msg = event.getMessage();
                LogUtil.i(msg.toString());
                LogUtil.i(msg.getExtra());
                LogUtil.i(msg.getExtra());
                if (BmobIMMessageType.getMessageTypeValue(msg.getMsgType()) == 0) {
                    //自定义消息类型：0
                    processCustomMessage(msg, event.getFromUserInfo());
                } else {
                    //SDK内部内部支持的消息类型
                    processSDKMessage(msg, event);
                }
            }
        });
    }

    /**
     * 处理SDK支持的消息
     *
     * @param msg
     * @param event
     */
    private void processSDKMessage(BmobIMMessage msg, MessageEvent event) {
        //如果需要显示通知栏，SDK提供以下两种显示方式：
        Intent pendingIntent = new Intent(context, ChatMainActivity.class);
        pendingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);


        //TODO 消息接收：8.5、多个用户的多条消息合并成一条通知：有XX个联系人发来了XX条消息
        //BmobNotificationManager.getInstance(context).showNotification(event, pendingIntent);

        //TODO 消息接收：8.6、自定义通知消息：始终只有一条通知，新消息覆盖旧消息
        BmobIMUserInfo info = event.getFromUserInfo();
        BmobIMConversation conversationEntrance = BmobIM.getInstance().startPrivateConversation(info, null);
        Bundle bundle = new Bundle();
        bundle.putSerializable("c", conversationEntrance);
        PersonInfo userInfo = new PersonInfo();
        userInfo.setObjectId(info.getUserId());
        userInfo.setName(info.getName());
        userInfo.setHeader_img_url(info.getAvatar());
        bundle.putSerializable("piInfo", userInfo);
        pendingIntent.putExtras(bundle);
        //这里可以是应用图标，也可以将聊天头像转成bitmap
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        BmobNotificationManager.getInstance(context).showNotification(largeIcon,
                info.getName(), msg.getContent(), "您有一条新消息", pendingIntent);
    }

    /**
     * 处理自定义消息类型
     *
     * @param msg
     */
    //TODO 好友管理：9.9、接收并处理好友相关的请求
    private void processCustomMessage(BmobIMMessage msg, BmobIMUserInfo info) {
        //消息类型
        String type = msg.getMsgType();
        //发送页面刷新的广播
        EventBus.getDefault().post(new RefreshEvent());
        //处理消息
        if (type.equals(AddFriendMessage.ADD)) {//接收到的添加好友的请求
            NewFriendInfo friend = AddFriendMessage.convert(msg);
            //本地好友请求表做下校验，本地没有的才允许显示通知栏--有可能离线消息会有些重复
            long id = NewFriendManager.getInstance(context).insertOrUpdateNewFriend(friend);
            if (id > 0) {
                showAddNotify(friend);
            }
        } else if (type.equals(AgreeAddFriendMessage.AGREE)) {//接收到的对方同意添加自己为好友,此时需要做的事情：1、添加对方为好友，2、显示通知
            AgreeAddFriendMessage agree = AgreeAddFriendMessage.convert(msg);
            addFriend(agree.getFromId());//添加消息的发送方为好友
            //这里应该也需要做下校验--来检测下是否已经同意过该好友请求，我这里省略了
            showAgreeNotify(info, agree);
        } else {
            Toast.makeText(context, "接收到的自定义消息：" + msg.getMsgType() + "," + msg.getContent() + "," + msg.getExtra(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 显示对方添加自己为好友的通知
     *
     * @param friend
     */
    private void showAddNotify(NewFriendInfo friend) {
        Intent pendingIntent = new Intent(context, AddFriendAgreeActivity.class);
        pendingIntent.putExtra("new_friend_info", friend);
        pendingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        //这里可以是应用图标，也可以将聊天头像转成bitmap
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        BmobNotificationManager.getInstance(context).showNotification(largeIcon,
                friend.getName(), friend.getMsg(), friend.getName() + "请求添加你为朋友", pendingIntent);
    }

    /**
     * 显示对方同意添加自己为好友的通知
     *
     * @param info
     * @param agree
     */
    private void showAgreeNotify(BmobIMUserInfo info, AgreeAddFriendMessage agree) {
        Intent pendingIntent = new Intent(context, ChatMainActivity.class);
        BmobIMConversation conversationEntrance = BmobIM.getInstance().startPrivateConversation(info, null);
        Bundle bundle = new Bundle();
        bundle.putSerializable("c", conversationEntrance);
        bundle.putInt("type", 0);
        pendingIntent.putExtras(bundle);
        pendingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        BmobNotificationManager.getInstance(context).showNotification(largeIcon, info.getName(), agree.getMsg(), agree.getMsg(), pendingIntent);
    }

    /**
     * TODO 好友管理：9.11、收到同意添加好友后添加好友
     *
     * @param uid
     */
    private void addFriend(String uid) {
        PersonInfo user = new PersonInfo();
        user.setObjectId(uid);
        UserModel.getInstance()
                .agreeAddFriend(user, new SaveListener<String>() {
                    @Override
                    public void done(String s, BmobException e) {
                        if (e == null) {
                            LogUtil.e("success");
                        } else {
                            LogUtil.e(e.getMessage());
                        }
                    }
                });
    }
}
