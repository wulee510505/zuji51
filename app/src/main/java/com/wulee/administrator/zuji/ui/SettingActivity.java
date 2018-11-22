package com.wulee.administrator.zuji.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.base.BaseActivity;
import com.wulee.administrator.zuji.chatui.enity.AddLinkmanMessage;
import com.wulee.administrator.zuji.database.bean.PersonInfo;
import com.wulee.administrator.zuji.utils.Config;
import com.wulee.administrator.zuji.utils.ConfigKey;
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
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

import static cn.bmob.v3.BmobUser.getCurrentUser;

/**
 * Created by wulee on 2017/1/11 16:59
 */

public class SettingActivity extends BaseActivity implements View.OnClickListener {

    public static final int INTENT_KEY_HOME = 101;
    public static final int INTENT_KEY_COMPANY = 102;
    public static final int INTENT_KEY_LINKMAN = 103;
    @InjectView(R.id.linkman)
    TextView tvLinkman;
    @InjectView(R.id.iv_add)
    ImageView ivAdd;

    private TextView mEtHome;
    private TextView mEtCompany;
    private ImageView ivSave;
    private ImageView ivBack;

    private Double homeLat;
    private Double homeLon;
    private String homeAddress;

    private Double companyLat;
    private Double companyLon;
    private String companyAddress;

    private PersonInfo currentUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settting);
        ButterKnife.inject(this);

        currentUser = getCurrentUser(PersonInfo.class);

        initView();
        addListerer();
    }

    private void addListerer() {
        ivBack.setOnClickListener(this);
        ivSave.setOnClickListener(this);
        mEtHome.setOnClickListener(this);
        mEtCompany.setOnClickListener(this);
    }

    private void initView() {
        ivBack = findViewById(R.id.iv_back);
        ivSave = findViewById(R.id.iv_save);
        mEtHome = findViewById(R.id.et_home);
        mEtCompany = findViewById(R.id.et_company);


        if (currentUser != null) {
            homeLat = currentUser.getHomeLat();
            homeLon = currentUser.getHomeLon();
            homeAddress = currentUser.getHomeAddress();

            companyLat = currentUser.getCompanyLat();
            companyLon = currentUser.getCompanyLon();
            companyAddress = currentUser.getCompanyAddress();

            mEtHome.setText(homeAddress);
            mEtCompany.setText(companyAddress);

            PersonInfo linkmanInfo = currentUser.getLinkman();
            if(linkmanInfo != null){
                BmobQuery<PersonInfo> query = new BmobQuery<>();
                query.addWhereEqualTo("objectId", linkmanInfo.getObjectId());
                query.findObjects(new FindListener<PersonInfo>() {
                    @Override
                    public void done(List<PersonInfo> userList, BmobException e) {
                        if(e == null){
                            if(userList != null && userList.size()>0){
                                PersonInfo linkmanInfo = userList.get(0);
                                if(linkmanInfo!= null){
                                    if(!TextUtils.isEmpty(linkmanInfo.getName())){
                                        ivAdd.setVisibility(View.GONE);
                                        tvLinkman.setText(linkmanInfo.getName());
                                    }else{
                                        ivAdd.setVisibility(View.VISIBLE);
                                    }
                                    if(!TextUtils.isEmpty(linkmanInfo.getInstallationId())){
                                        Config.get(SettingActivity.this).put(ConfigKey.KEY_LINKMAN_INSTALLATIONID,linkmanInfo.getInstallationId());
                                    }
                                }
                            }
                        }
                    }
                });
            }

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.et_home:
                startActivityForResult(new Intent(this, MapSelPointActivity.class), INTENT_KEY_HOME);
                break;
            case R.id.et_company:
                startActivityForResult(new Intent(this, MapSelPointActivity.class), INTENT_KEY_COMPANY);
                break;
            case R.id.iv_save:
                PersonInfo user = BmobUser.getCurrentUser(PersonInfo.class);
                if (null == user)
                    return;
                if (user.getHomeLat() != null && user.getHomeLon() != null && user.getCompanyLat() != null && user.getCompanyLon() != null) {
                    if (OtherUtil.equal(user.getHomeLat(), homeLat) && OtherUtil.equal(user.getHomeLon(), homeLon) && OtherUtil.equal(user.getCompanyLat(), companyLat) && OtherUtil.equal(user.getCompanyLon(), companyLon)) {
                        return;
                    }
                }

                if (TextUtils.isEmpty(homeAddress)) {
                    toast("请选择家庭地址");
                    return;
                }
                if (TextUtils.isEmpty(companyAddress)) {
                    toast("请选择公司地址");
                    return;
                }

                showProgressDialog(false);

                user.setHomeLat(homeLat);
                user.setHomeLon(homeLon);
                user.setHomeAddress(homeAddress);

                user.setCompanyLat(companyLat);
                user.setCompanyLon(companyLon);
                user.setCompanyAddress(companyAddress);
                user.update(user.getObjectId(), new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        stopProgressDialog();
                        if (e == null) {
                            toast("保存成功");
                            finish();
                        } else {
                            toast("保存失败:" + e.getMessage());
                        }
                    }
                });
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case INTENT_KEY_HOME:
                    LatLng homeLoction = data.getParcelableExtra("latLng");
                    if (null == homeLoction)
                        return;
                    homeLat = homeLoction.latitude;
                    homeLon = homeLoction.longitude;

                    homeAddress = data.getStringExtra("address");
                    mEtHome.setText(homeAddress);
                    break;
                case INTENT_KEY_COMPANY:
                    LatLng companyLoction = data.getParcelableExtra("latLng");
                    if (null == companyLoction)
                        return;
                    companyLat = companyLoction.latitude;
                    companyLon = companyLoction.longitude;

                    companyAddress = data.getStringExtra("address");
                    mEtCompany.setText(companyAddress);
                    break;
                case INTENT_KEY_LINKMAN:
                    ivAdd.setVisibility(View.GONE);
                    PersonInfo piInfo = (PersonInfo) data.getSerializableExtra("piInfo");
                    if (piInfo != null) {
                        if(currentUser != null){
                            currentUser.setLinkman(piInfo);
                            showProgressDialog(true);
                            currentUser.update(new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    stopProgressDialog();
                                    if(e == null){
                                        sendAddLinkmanMessage(piInfo);
                                        if(!TextUtils.isEmpty(piInfo.getName())){
                                            tvLinkman.setText(piInfo.getName());
                                        }
                                    }
                                }
                            });
                        }
                    }
                    break;
            }
        }
    }

    @OnClick({R.id.iv_add,R.id.linkman})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_add:
            case R.id.linkman:
                startActivityForResult(new Intent(this, SelectFriendActivity.class), INTENT_KEY_LINKMAN);
                break;
        }
    }


    /**
     * 发送添加紧急联系人的请求
     */
    private void sendAddLinkmanMessage(PersonInfo personInfo) {
        //TODO 会话：4.1、创建一个暂态会话入口，发送紧急联系人请求
        if (personInfo != null && personInfo.getObjectId()!= null && BmobIM.getInstance().getCurrentStatus().getCode() == ConnectionStatus.CONNECTED.getCode()) {
            BmobIMUserInfo info = new BmobIMUserInfo(personInfo.getObjectId(), !TextUtils.isEmpty(personInfo.getName()) ? personInfo.getName() : personInfo.getUsername(), personInfo.getHeader_img_url());
            BmobIMConversation conversationEntrance = BmobIM.getInstance().startPrivateConversation(info, true, null);
            //TODO 消息：5.1、根据会话入口获取消息管理，发送紧急联系人请求
            BmobIMConversation messageManager = BmobIMConversation.obtain(BmobIMClient.getInstance(), conversationEntrance);
            AddLinkmanMessage msg = new AddLinkmanMessage();
            PersonInfo currentUser = BmobUser.getCurrentUser(PersonInfo.class);
            msg.setContent("请求添加您为紧急联系人");//给对方的一个留言信息
            //TODO 这里只是举个例子，其实可以不需要传发送者的信息过去
            Map<String, Object> map = new HashMap<>();
            map.put("name", currentUser.getUsername());//发送者姓名
            map.put("avatar", currentUser.getHeader_img_url());//发送者的头像
            map.put("uid", currentUser.getObjectId());//发送者的uid
            msg.setExtraMap(map);
            messageManager.sendMessage(msg, new MessageSendListener() {
                @Override
                public void done(BmobIMMessage msg, BmobException e) {
                    if (e == null) {//发送成功
                        toast("紧急联系人请求发送成功，等待验证");
                    } else {//发送失败
                        toast("发送失败:" + e.getMessage());
                    }
                }
            });
        }
    }
}
