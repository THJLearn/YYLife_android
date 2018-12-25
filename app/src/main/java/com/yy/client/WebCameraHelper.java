package com.yy.client;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;

public class WebCameraHelper {

    private static class SingletonHolder {
        static final WebCameraHelper INSTANCE = new WebCameraHelper();
    }

    public static WebCameraHelper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 图片选择回调
     */
    public ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> mUploadCallbackAboveL;

    public Uri fileUri;
    public static final int TYPE_REQUEST_PERMISSION = 3;
    public static final int TYPE_CAMERA = 1;
    public static final int TYPE_GALLERY = 2;

    /**
     * 包含拍照和相册选择
     */
    public void showOptions(final Activity act) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(act);
        alertDialog.setOnCancelListener(new ReOnCancelListener());
        alertDialog.setTitle("选择");
        alertDialog.setItems(new CharSequence[]{"相机", "相册"},
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {

                            if (ContextCompat.checkSelfPermission(act,
                                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                // 申请WRITE_EXTERNAL_STORAGE权限
                                ActivityCompat
                                        .requestPermissions(
                                                act,
                                                new String[]{Manifest.permission.CAMERA},
                                                TYPE_REQUEST_PERMISSION);
                            } else {
                                toCamera(act);
                            }

                        } else {
                            Intent i = new Intent(
                                    Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);// 调用android的图库
                            act.startActivityForResult(i,
                                    TYPE_GALLERY);
                        }
                    }
                });
        alertDialog.show();
    }

    /**
     * 点击取消的回调
     */
    private class ReOnCancelListener implements
            DialogInterface.OnCancelListener {

        @Override
        public void onCancel(DialogInterface dialogInterface) {
            if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(null);
                mUploadMessage = null;
            }
            if (mUploadCallbackAboveL != null) {
                mUploadCallbackAboveL.onReceiveValue(null);
                mUploadCallbackAboveL = null;
            }
        }
    }

    /**
     * 请求拍照
     * @param act
     */
    public void toCamera(Activity act) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 调用android的相机
        // 创建一个文件保存图片
        fileUri = Uri.fromFile(FileManager.getImgFile(act.getApplicationContext()));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        act.startActivityForResult(intent, TYPE_CAMERA);
    }

    /**
     * startActivityForResult之后要做的处理
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == TYPE_CAMERA) { // 相机选择
            if (resultCode == -1) {//RESULT_OK = -1，拍照成功
                if (mUploadCallbackAboveL != null) { //高版本SDK处理方法
                    Uri[] uris = new Uri[]{fileUri};
                    mUploadCallbackAboveL.onReceiveValue(uris);
                    mUploadCallbackAboveL = null;
                } else if (mUploadMessage != null) { //低版本SDK 处理方法
                    mUploadMessage.onReceiveValue(fileUri);
                    mUploadMessage = null;
                } else {
                    Log.d("onActivityResult","无法获取数据");
//                    Toast.makeText(CubeAndroid.this, "无法获取数据", Toast.LENGTH_LONG).show();
                }
            } else { //拍照不成功，或者什么也不做就返回了，以下的处理非常有必要，不然web页面不会有任何响应
                if (mUploadCallbackAboveL != null) {
                    mUploadCallbackAboveL.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                    mUploadCallbackAboveL = null;
                } else if (mUploadMessage != null) {
                    mUploadMessage.onReceiveValue(fileUri);
                    mUploadMessage = null;
                } else {
                    Log.d("onActivityResult1","无法获取数据");
//                    Toast.makeText(MainActivity., "无法获取数据", Toast.LENGTH_LONG).show();
                }

            }
        } else if (requestCode == TYPE_GALLERY) {// 相册选择
            if (mUploadCallbackAboveL != null) {
                mUploadCallbackAboveL.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                mUploadCallbackAboveL = null;
            } else if (mUploadMessage != null) {
                Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            } else {
                Log.d("onActivityResult2","无法获取数据");
//                Toast.makeText(CubeAndroid.this, "无法获取数据", Toast.LENGTH_LONG).show();
            }
        }
    }
}
