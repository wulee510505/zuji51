package com.wulee.administrator.zuji.entity;

import com.wulee.administrator.zuji.database.bean.PersonInfo;

import java.util.Objects;

import cn.bmob.v3.BmobObject;

/**
 * Created by wulee on 2017/6/9 14:45
 */

public class StepInfo extends BmobObject{

    private int count;
    private String date;
    public PersonInfo personInfo;


    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    // 设置分组的key
    public String groupKey(){
        return getCreatedAt().substring(0,10);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StepInfo stepInfo = (StepInfo) o;
        return count == stepInfo.count &&
                Objects.equals(date, stepInfo.date) &&
                Objects.equals(personInfo, stepInfo.personInfo);
    }

    @Override
    public int hashCode() {

        return Objects.hash(count, date, personInfo);
    }
}
