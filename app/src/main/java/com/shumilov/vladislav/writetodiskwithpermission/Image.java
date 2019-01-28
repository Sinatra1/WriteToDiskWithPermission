package com.shumilov.vladislav.writetodiskwithpermission;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Observable;

public class Image extends Observable {

    private String mUrl;
    private String mName;
    private Long mRequestId;

    public Image() {

    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
        setChanged();
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
        setChanged();
    }

    public Long getRequestId() {
        return mRequestId;
    }

    public void setRequestId(Long requestId) {
        mRequestId = requestId;
        setChanged();
    }
}
