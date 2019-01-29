package com.shumilov.vladislav.writetodiskwithpermission;

import java.util.Observable;

public class Image extends Observable {

    private String mUrl;
    private String mName;
    private String mFolder;
    private Long mRequestId;
    private Long mResultId;

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

    public Long getResultId() {
        return mResultId;
    }

    public void setResultId(Long resultId) {
        mResultId = resultId;
    }

    public String getFolder() {
        return mFolder;
    }

    public void setFolder(String folder) {
        mFolder = folder;
    }

    public String getFullPath() {
        return mFolder + "/" + mName;
    }
}
