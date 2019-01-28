package com.shumilov.vladislav.writetodiskwithpermission;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements Observer {

    private static final int WRITE_EXTERNAL_REQUEST_CODE = 123;
    private static final String IMAGE_KEY = "IMAGE_KEY";
    private static final String DOWNLOAD_IMAGE_REF_ID = "DOWNLOAD_IMAGE_REF_ID";

    private EditText mImageUrlEditView;
    private Button mDownloadImageButton;
    private Button mShowImageButton;
    private ImageView mPictureImageView;

    private Long mDownloadedImageRefId;

    private ImageService mImageService;
    private Image mImage = new Image();
    private boolean mBound = false;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWidgets();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, ImageService.class);
        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        registerReceiver(mOnCompleteDownloadImage, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    protected void onResume() {
        super.onResume();

        initListeners();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }

        unregisterReceiver(mOnCompleteDownloadImage);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        outState.putLong(DOWNLOAD_IMAGE_REF_ID, mDownloadedImageRefId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState == null) {
            return;
        }

        mDownloadedImageRefId = savedInstanceState.getLong(DOWNLOAD_IMAGE_REF_ID);

        onImageChanged();
    }

    private void initWidgets() {
        mImageUrlEditView = findViewById(R.id.evImageUrl);
        mDownloadImageButton = findViewById(R.id.btnDownloadImage);
        mShowImageButton = findViewById(R.id.btnShowImage);
        mPictureImageView = findViewById(R.id.ivPicture);
    }

    private void initListeners() {
        mDownloadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShowImageButton.setEnabled(false);
                downloadPicture();
            }
        });
    }

    private void downloadPicture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionWriteExternalStorage();
            return;
        }

        doDownloadPicture();
    }

    private void requestPermissionWriteExternalStorage() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.external_storage_rationale_description)
                    .setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            doRequestWriteExternalStoragePermission();
                        }
                    }).show();
        } else {
            doRequestWriteExternalStoragePermission();
        }
    }

    private void doRequestWriteExternalStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode != WRITE_EXTERNAL_REQUEST_CODE || grantResults.length != 1) {
            return;
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            doDownloadPicture();
        } else {
            Toast.makeText(this, R.string.permission_not_granted_message, Toast.LENGTH_LONG).show();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void doDownloadPicture() {
        if (!mBound) {
            return;
        }

        String imageUrl = mImageUrlEditView.getText().toString();

        mImageService.downloadImage(imageUrl);
    }

    BroadcastReceiver mOnCompleteDownloadImage = new BroadcastReceiver() {

        public void onReceive(Context ctxt, Intent intent) {
            mDownloadedImageRefId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if (mImage.getRequestId() != null && mImage.getRequestId().equals(mDownloadedImageRefId)) {
                onImageChanged();
                Toast.makeText(MainActivity.this, getString(R.string.image_downloaded, mImage.getName()), Toast.LENGTH_LONG).show();
            }
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            ImageService.ImageBinder binder = (ImageService.ImageBinder) service;
            mImageService = binder.getService();
            mImage = mImageService.getImage();
            mImage.addObserver(MainActivity.this);
            mDownloadImageButton.setEnabled(true);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    @Override
    public void update(Observable observable, Object arg) {
        if (!(observable instanceof Image)) {
            return;
        }

        onImageChanged();
    }

    private void onImageChanged() {
        if (mImage.getRequestId() != null && mImage.getRequestId().equals(mDownloadedImageRefId)) {
            mShowImageButton.setEnabled(true);
            return;
        }

        mShowImageButton.setEnabled(false);
    }
}
