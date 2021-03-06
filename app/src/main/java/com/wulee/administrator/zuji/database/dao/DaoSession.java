package com.wulee.administrator.zuji.database.dao;

import android.database.sqlite.SQLiteDatabase;

import com.wulee.administrator.zuji.database.bean.LocationInfo;
import com.wulee.administrator.zuji.database.bean.LoginBean;
import com.wulee.administrator.zuji.database.bean.NewFriendInfo;
import com.wulee.administrator.zuji.database.bean.PersonInfo;
import com.wulee.administrator.zuji.database.bean.PushMessage;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see de.greenrobot.dao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig loginBeanDaoConfig;
    private final DaoConfig pushMessageDaoConfig;
    private final DaoConfig personInfoDaoConfig;
    private final DaoConfig locationInfoDaoConfig;

    private final LoginBeanDao loginBeanDao;
    private final PushMessageDao pushMessageDao;
    private final PersonInfoDao personInfoDao;
    private final LocationInfoDao locationInfoDao;

    private final DaoConfig newFriendInfoDaoConfig;

    private final NewFriendInfoDao newFriendInfoDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        newFriendInfoDaoConfig = daoConfigMap.get(NewFriendInfoDao.class).clone();
        newFriendInfoDaoConfig.initIdentityScope(type);

        loginBeanDaoConfig = daoConfigMap.get(LoginBeanDao.class).clone();
        loginBeanDaoConfig.initIdentityScope(type);

        pushMessageDaoConfig = daoConfigMap.get(PushMessageDao.class).clone();
        pushMessageDaoConfig.initIdentityScope(type);

        personInfoDaoConfig = daoConfigMap.get(PersonInfoDao.class).clone();
        personInfoDaoConfig.initIdentityScope(type);

        locationInfoDaoConfig = daoConfigMap.get(LocationInfoDao.class).clone();
        locationInfoDaoConfig.initIdentityScope(type);

        loginBeanDao = new LoginBeanDao(loginBeanDaoConfig, this);
        pushMessageDao = new PushMessageDao(pushMessageDaoConfig, this);
        personInfoDao = new PersonInfoDao(personInfoDaoConfig, this);
        locationInfoDao = new LocationInfoDao(locationInfoDaoConfig, this);
        newFriendInfoDao = new NewFriendInfoDao(newFriendInfoDaoConfig, this);

        registerDao(LoginBean.class, loginBeanDao);
        registerDao(PushMessage.class, pushMessageDao);
        registerDao(PersonInfo.class, personInfoDao);
        registerDao(LocationInfo.class, locationInfoDao);

        registerDao(NewFriendInfo.class, newFriendInfoDao);
    }
    
    public void clear() {
        newFriendInfoDaoConfig.getIdentityScope().clear();
        loginBeanDaoConfig.getIdentityScope().clear();
        pushMessageDaoConfig.getIdentityScope().clear();
        personInfoDaoConfig.getIdentityScope().clear();
        locationInfoDaoConfig.getIdentityScope().clear();
    }

    public NewFriendInfoDao getNewFriendInfoDao() {
        return newFriendInfoDao;
    }

    public LoginBeanDao getLoginBeanDao() {
        return loginBeanDao;
    }

    public PushMessageDao getPushMessageDao() {
        return pushMessageDao;
    }

    public PersonInfoDao getPersonInfoDao() {
        return personInfoDao;
    }

    public LocationInfoDao getLocationInfoDao() {
        return locationInfoDao;
    }

}
