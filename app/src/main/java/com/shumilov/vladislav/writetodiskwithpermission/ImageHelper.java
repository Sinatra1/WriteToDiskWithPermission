package com.shumilov.vladislav.writetodiskwithpermission;

import android.text.TextUtils;

public class ImageHelper {

    public boolean isImageUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        url = url.toLowerCase();

        if (!url.contains("/") || (!url.endsWith("jpg") && !url.endsWith("png") && !url.endsWith("bmp"))) {
            return false;
        }

        return true;
    }

    public String getFileSimpleNameForUrl(String url) {
        if (!isImageUrl(url)) {
            return "";
        }

        String[] urlArray = url.split("/");

        return urlArray[urlArray.length - 1];
    }
}
