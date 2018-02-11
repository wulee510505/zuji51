package com.wulee.administrator.zuji.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wulee on 2017/8/22 14:01
 */

public class PublishPicture implements Parcelable {

    private int id;
    private String path;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.path);
    }

    public PublishPicture() {
    }

    protected PublishPicture(Parcel in) {
        this.id = in.readInt();
        this.path = in.readString();
    }

    public static final Parcelable.Creator<PublishPicture> CREATOR = new Parcelable.Creator<PublishPicture>() {
        @Override
        public PublishPicture createFromParcel(Parcel source) {
            return new PublishPicture(source);
        }

        @Override
        public PublishPicture[] newArray(int size) {
            return new PublishPicture[size];
        }
    };
}
