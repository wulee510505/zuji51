package com.wulee.administrator.zuji.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.wulee.administrator.zuji.database.bean.NewFriendInfo;
import com.wulee.administrator.zuji.database.bean.PersonInfo;
import com.wulee.administrator.zuji.database.dao.DaoMaster;
import com.wulee.administrator.zuji.database.dao.DaoSession;
import com.wulee.administrator.zuji.database.dao.NewFriendInfoDao;
import com.wulee.administrator.zuji.entity.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.bmob.v3.BmobUser;


/**
 * Created by Administrator on 2016/4/27.
 */
public class NewFriendManager {

    private DaoMaster.DevOpenHelper openHelper;
    Context mContext;
    String uid=null;
    private static HashMap<String, NewFriendManager> daoMap = new HashMap<>();

    /**获取DB实例
     * @param context
     * @return
     */
    public static NewFriendManager getInstance(Context context) {
        PersonInfo user = BmobUser.getCurrentUser( PersonInfo.class);
        String loginId=user.getObjectId();
        if(TextUtils.isEmpty(loginId)){
            throw new RuntimeException("you must login.");
        }
        NewFriendManager dao = daoMap.get(loginId);
        if (dao == null) {
            dao = new NewFriendManager(context,loginId);
            daoMap.put(loginId, dao);
        }
        return dao;
    }

    private NewFriendManager(Context context, String uId){
        clear();
        this.mContext =context.getApplicationContext();
        this.uid=uId;
        String DBName = uId+".demodb";
        this.openHelper = new DaoMaster.DevOpenHelper(mContext, DBName, null);
    }

    /**
     * 清空资源
     */
    public void clear() {
        if(openHelper !=null) {
            openHelper.close();
            openHelper = null;
            mContext=null;
            uid =null;
        }
    }

    private DaoSession openReadableDb() {
        checkInit();
        SQLiteDatabase db = openHelper.getReadableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        return daoSession;
    }

    private DaoSession openWritableDb(){
        checkInit();
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        return daoSession;
    }

    private void checkInit(){
        if(openHelper ==null){
            throw new RuntimeException("请初始化db");
        }
    }

    //-------------------------------------------------------------

    /**获取本地所有的邀请信息
     * @return
     */
    public List<NewFriendInfo> getAllNewFriend(){
        NewFriendInfoDao dao = openReadableDb().getNewFriendInfoDao();
        return dao.queryBuilder().orderDesc(NewFriendInfoDao.Properties.Time).list();
    }

    /**创建或更新新朋友信息
     * @param info
     * @return long:返回插入或修改的id
     */
    public long insertOrUpdateNewFriend(NewFriendInfo info){
        NewFriendInfoDao dao = openWritableDb().getNewFriendInfoDao();
        NewFriendInfo local = getNewFriend(info.getUid(), info.getTime());
        if(local==null){
            return dao.insertOrReplace(info);
        }else{
            return -1;
        }
    }

    /**
     * 获取本地的好友请求
     * @param uid
     * @param time
     * @return
     */
    private NewFriendInfo getNewFriend(String uid, Long time){
        NewFriendInfoDao dao =  openReadableDb().getNewFriendInfoDao();
        return dao.queryBuilder().where(NewFriendInfoDao.Properties.Uid.eq(uid))
                .where(NewFriendInfoDao.Properties.Time.eq(time)).build().unique();
    }

    /**
     * 是否有新的好友邀请
     * @return
     */
    public boolean hasNewFriendInvitation(){
        List<NewFriendInfo> infos = getNoVerifyNewFriend();
        if(infos!=null && infos.size()>0){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 获取未读的好友邀请
     * @return
     */
    public int getNewInvitationCount(){
        List<NewFriendInfo> infos =getNoVerifyNewFriend();
        if(infos!=null && infos.size()>0){
            return infos.size();
        }else{
            return 0;
        }
    }
    /**
     * 获取所有未读未验证的好友请求
     * @return
     */
    private List<NewFriendInfo> getNoVerifyNewFriend(){
        NewFriendInfoDao dao =  openReadableDb().getNewFriendInfoDao();
        return dao.queryBuilder().where(NewFriendInfoDao.Properties.Status.eq(Constant.STATUS_VERIFY_NONE))
                .build().list();
    }

    /**
     * 批量更新未读未验证的状态为已读
     */
    public void updateBatchStatus(){
        List<NewFriendInfo> infos =getNoVerifyNewFriend();
        if(infos!=null && infos.size()>0){
            int size =infos.size();
            List<NewFriendInfo> all =new ArrayList<>();
            for (int i = 0; i < size; i++) {
                NewFriendInfo msg =infos.get(i);
                msg.setStatus(Constant.STATUS_VERIFY_READED);
                all.add(msg);
            }
            insertBatchMessages(infos);
        }
    }

    /**批量插入消息
     * @param msgs
     */
    public  void insertBatchMessages(List<NewFriendInfo> msgs){
        NewFriendInfoDao dao =openWritableDb().getNewFriendInfoDao();
        dao.insertOrReplaceInTx(msgs);
    }

    /**
     * 修改指定好友请求的状态
     * @param friend
     * @param status
     * @return
     */
    public long updateNewFriend(NewFriendInfo friend,int status){
        NewFriendInfoDao dao = openWritableDb().getNewFriendInfoDao();
        friend.setStatus(status);
        return dao.insertOrReplace(friend);
    }

    /**
     * 删除指定的添加请求
     * @param friend
     */
    public void deleteNewFriend(NewFriendInfo friend){
        NewFriendInfoDao dao =openWritableDb().getNewFriendInfoDao();
        dao.delete(friend);
    }

}
