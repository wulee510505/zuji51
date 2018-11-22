package com.wulee.administrator.zuji.entity;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.wulee.administrator.zuji.database.bean.PersonInfo;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.datatype.BmobRelation;

/**
 * Created by wulee on 2017/8/22 10:21
 */

public class CircleContent extends BmobObject implements MultiItemEntity{

    public static final int TYPE_TEXT_AND_IMG = 0;
    public static final int TYPE_ONLY_TEXT = 1;
    private int itemType;

    public CircleContent(int itemType) {
        this.itemType = itemType;
    }
    @Override
    public int getItemType() {
        return itemType;
    }

    private long id;

    private String userId;

    private String title;
    private String content;

    private String location;
    private BmobGeoPoint locationInfo;
    private String address;

    public PersonInfo personInfo;

    private String[] imgUrls;
    private ArrayList<CircleImageBean> imageList;

    private int likeNum;
    private BmobRelation likes;

    private List<CircleComment> commentList;

    public List<CircleComment> getCommentList() {
        return commentList;
    }

    public void setCommentList(List<CircleComment> commentList) {
        this.commentList = commentList;
    }

    public List<PersonInfo> getLikeList() {
        return likeList;
    }

    public void setLikeList(List<PersonInfo> likeList) {
        this.likeList = likeList;
    }

    private List<PersonInfo> likeList;//喜欢此圈子的用户集合

    public BmobRelation getLikes() {
        return likes;
    }

    public void setLikes(BmobRelation likes) {
        this.likes = likes;
    }

    public PersonInfo getPersonInfo() {
        return personInfo;
    }

    public void setPersonInfo(PersonInfo personInfo) {
        this.personInfo = personInfo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getLikeNum() {
        return likeNum;
    }

    public void setLikeNum(int likeNum) {
        this.likeNum = likeNum;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BmobGeoPoint getLocationInfo() {
        return locationInfo;
    }

    public void setLocationInfo(BmobGeoPoint locationInfo) {
        this.locationInfo = locationInfo;
    }

    public String[] getImgUrls() {
        return imgUrls;
    }

    public void setImgUrls(String[] imgUrls) {
        this.imgUrls = imgUrls;
    }

    public List<CircleImageBean> getImageList() {
        return imageList;
    }

    public void setImageList(ArrayList<CircleImageBean> imageList) {
        this.imageList = imageList;
    }

    public static class CircleImageBean {
        private String id;
        private String url;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
