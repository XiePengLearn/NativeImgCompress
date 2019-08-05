package com.logistics.jubao.nativeimgcompress;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import net.bither.util.NativeUtil;

import java.io.File;

import jpegcompress.FileUtils;

public class MainActivity extends AppCompatActivity {
    private Button btnCompress;

    private static final int CODE_REQUEST_PERMISSION = 0;
    private static final int CODE_CAMERA = 1;
    private final String[] PERMISSIONS = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private Uri imageFileUri;
    private ImageView ivCompassImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        btnCompress = (Button) findViewById(R.id.btn_compress);
        ivCompassImg = (ImageView) findViewById(R.id.iv_compass_img);
        btnCompress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkLackPermissions(PERMISSIONS)) {
                    takePhoto();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, CODE_REQUEST_PERMISSION);
                }
            }
        });
    }

    private boolean checkLackPermissions(String[] permissions) {
        for (String s : permissions) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, s) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    private void takePhoto() {
        // 判断存储卡是否可以用，可用进行存储
        String sdStatus = Environment.getExternalStorageState();
        if (sdStatus.equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String path = Environment.getExternalStorageDirectory().toString() + "/Photo";
            File path1 = new File(path);
            if (!path1.exists()) {
                path1.mkdirs();
            }
            String imageFileName = System.currentTimeMillis() + ".jpg";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//如果是7.0android系统
                ContentValues contentValues = new ContentValues(1);
                contentValues.put(MediaStore.Images.Media.DATA, new File(path, imageFileName).getAbsolutePath());
                imageFileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            } else {
                imageFileUri = Uri.fromFile(new File(path, imageFileName));
            }

            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
            startActivityForResult(intent, CODE_CAMERA);
        } else {
            Toast.makeText(this, "内部存储不可用", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CODE_REQUEST_PERMISSION:
                for (Integer i : grantResults) {
                    if (i != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "权限获取失败", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                takePhoto();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CODE_CAMERA:
                    if (imageFileUri != null) {
                        String cameraImgPath = FileUtils.getPathByUri(getApplication(), imageFileUri);
                        Log.e("cameraImgPath: ", cameraImgPath);
                        compressImage(cameraImgPath);
                    }
                    break;
            }
        }
    }

    //    public void compressImage(Uri uri) {
    public void compressImage(String imageUrl) {
        Log.e("androidpiccompress", "====开始====imageUrl==" + imageUrl);
        File saveFile = new File(getPicPath(), "compress_" + System.currentTimeMillis() + ".jpg");

        Log.e("androidpiccompress", "====开始==压缩==saveFile==" + saveFile.getAbsolutePath());
        NativeUtil.compressBitmap(imageUrl, saveFile.getAbsolutePath());
        Log.e("androidpiccompress", "====完成==压缩==saveFile==" + saveFile.getAbsolutePath());
        File imgFile = new File(saveFile.getAbsolutePath());
//        thumbFileSize.setText(imgFile.length() / 1024 + "k");
//        thumbImageSize.setText(FileUtils.getImageSize(saveFile.getAbsolutePath())[0] + " * "
//                + FileUtils.getImageSize(saveFile.getAbsolutePath())[1]);
        Glide.with(MainActivity.this).load(imgFile).into(ivCompassImg);
    }

    public String getPicPath() {
        String sdCardPath = getSDPath();
        String picUrl = "";
        if (TextUtils.isEmpty(sdCardPath)) {
//            return "";
        } else {
            picUrl = sdCardPath + File.separator + "PicCompress"
                    + File.separator + "pic";
        }
        File file = new File(picUrl);
        if (!file.exists()) {
            file.mkdirs();
        }
        return picUrl;
    }

    public String getSDPath() {
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            return Environment.getExternalStorageDirectory().toString();
        } else {
            return "";
        }
    }
}
