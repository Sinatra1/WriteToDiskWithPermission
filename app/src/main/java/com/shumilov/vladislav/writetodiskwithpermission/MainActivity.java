package com.shumilov.vladislav.writetodiskwithpermission;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

public class MainActivity extends AppCompatActivity {

    private static final int WRITE_EXTERNAL_REQUEST_CODE = 123;

    private EditText mImageUrlEditView;
    private Button mUploadImageButton;
    private Button mShowImageButton;
    private ImageView mPictureImageView;

    private Long mRefId;
    private String mFileName;

    private DownloadManager mDownloadManager;
    private ImageHelper mImageHelper = new ImageHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWidgets();
    }

    @Override
    protected void onStart() {
        super.onStart();

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

        unregisterReceiver(mOnCompleteDownloadImage);
    }

    private void initWidgets() {
        mImageUrlEditView = findViewById(R.id.evImageUrl);
        mUploadImageButton = findViewById(R.id.btnUploadImage);
        mShowImageButton = findViewById(R.id.btnShowImage);
        mPictureImageView = findViewById(R.id.ivPicture);

        mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
    }

    private void initListeners() {
        mUploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        String imageUrl = mImageUrlEditView.getText().toString();

        if (!mImageHelper.isImageUrl(imageUrl)) {
            Toast.makeText(this, R.string.set_image_url_message, Toast.LENGTH_SHORT).show();
            return;
        }

        mFileName = mImageHelper.getFileSimpleNameForUrl(imageUrl);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(false);
        request.setTitle(getString(R.string.download_image_title));
        request.setDescription(getString(R.string.picture) + " " + mFileName);
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/" + getString(R.string.app_name) + "/" + mFileName);

        mRefId = mDownloadManager.enqueue(request);
    }

    BroadcastReceiver mOnCompleteDownloadImage = new BroadcastReceiver() {

        public void onReceive(Context ctxt, Intent intent) {
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if (mRefId == referenceId) {
                mShowImageButton.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, getString(R.string.image_downloaded, mFileName), Toast.LENGTH_LONG).show();
            }
        }
    };
}
