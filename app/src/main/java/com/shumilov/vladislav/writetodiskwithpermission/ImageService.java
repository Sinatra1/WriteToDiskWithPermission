package com.shumilov.vladislav.writetodiskwithpermission;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

public class ImageService extends Service {

    private final IBinder mBinder = new ImageBinder();
    private ImageHelper mImageHelper = new ImageHelper();
    private DownloadManager mDownloadManager;
    private Image mImage = new Image();

    @Override
    public void onCreate() {
        super.onCreate();

        mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class ImageBinder extends Binder {
        public ImageService getService() {
            return ImageService.this;
        }
    }

    public void downloadImage(String imageUrl) {

        if (!mImageHelper.isImageUrl(imageUrl)) {
            Toast.makeText(this, R.string.set_image_url_message, Toast.LENGTH_SHORT).show();
            return;
        }

        mImage.setUrl(imageUrl);

        mImage.setName(mImageHelper.getFileSimpleNameForUrl(imageUrl));
        mImage.setFolder("/" + getString(R.string.app_name));

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(false);
        request.setTitle(getString(R.string.download_image_title));
        request.setDescription(getString(R.string.picture) + " " + mImage.getName());
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, mImage.getFullPath());

        mImage.setRequestId(mDownloadManager.enqueue(request));
        mImage.notifyObservers();
    }

    public Image getImage() {
        return mImage;
    }
}
