package com.yy.client;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import cn.jpush.android.api.JPushInterface;

import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static com.yy.client.WebCameraHelper.TYPE_REQUEST_PERMISSION;

public class MainActivity extends Activity {

    private WebView myWebview;
    private static final int REQUEST_CODE_STORAGE = 1;
    private AlertDialog dialog;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        极光推送
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
        JPushInterface.setAlias(this,1,"11111111");
        this.createNotificationChannel()
        ;


//        检查空间储存权限
        checkPermission();

        startMainActivity();


    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri path = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.test);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // 通知渠道的id 这个地方只要一直即可
            String id = "com.yy.client123456";
            // 用户可以看到的通知渠道的名字.
            CharSequence name = "翼优生活";
            // 用户可以看到的通知渠道的描述
            String description = "翼优打造美好生活";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            // 配置通知渠道的属性
            mChannel.setDescription(description);
            // 设置通知出现时的闪灯（如果 android 设备支持的话）
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            // 自定义声音
            // 设置通知出现时的震动（如果 android 设备支持的话
//            mChannel.setSound(path, null);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{500, 500});
            //最后在notificationmanager中创建该通知渠道
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    private void startMainActivity() {


        setContentView(R.layout.activity_main);

        myWebview = (WebView)findViewById(R.id.yywebview);
        myWebview.loadUrl("http://47.94.209.108:7003/yy/");
//        配置
        WebSettings webSettings = myWebview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setBlockNetworkImage(false);
        webSettings.setAppCacheEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAllowContentAccess(true);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        webSettings.setAppCachePath(appCachePath);
        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setAllowFileAccessFromFileURLs(true);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){//
            webSettings.setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        myWebview.setWebChromeClient(new MyWebChromeClient());
        myWebview.setWebViewClient(new MyWebViewClient());

    }


    private Uri imageUri;



    class MyWebChromeClient extends WebChromeClient {
        // For Android < 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            WebCameraHelper.getInstance().mUploadMessage = uploadMsg;
            WebCameraHelper.getInstance().showOptions(MainActivity.this);
        }

        // For Android > 4.1.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                    String acceptType, String capture) {
            WebCameraHelper.getInstance().mUploadMessage = uploadMsg;
            WebCameraHelper.getInstance().showOptions(MainActivity.this);
        }

        // For Android > 5.0支持多张上传
        @Override
        public boolean onShowFileChooser(WebView webView,
                                         ValueCallback<Uri[]> uploadMsg,
                                         FileChooserParams fileChooserParams) {

            checkCamer();
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                // 申请WRITE_EXTERNAL_STORAGE权限
                ActivityCompat
                        .requestPermissions(
                                MainActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                TYPE_REQUEST_PERMISSION);
                return false;
            }else {
                WebCameraHelper.getInstance().mUploadCallbackAboveL = uploadMsg;
                WebCameraHelper.getInstance().showOptions(MainActivity.this);
                return true;
            }



        }


    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        WebCameraHelper.getInstance().onActivityResult(requestCode, resultCode, intent);
    }


    class MyWebViewClient extends WebViewClient {

        //重写shouldOverrideUrlLoading方法，使点击链接后不使用其他的浏览器打开。

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            System.out.print("thjwebview----"+url);
//            Log.d("thjwebview----",url);
            view.loadUrl(url);
            return true;

        }

    }


    @Override
    public void onBackPressed() {

        if (myWebview.canGoBack()){
            myWebview.goBack();
        }else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //如果按下的是回退键且历史记录里确实还有页面
        if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebview.canGoBack()) {
            myWebview.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setNavigationBar(Activity activity, int visible){
        View decorView = activity.getWindow().getDecorView();
        //显示NavigationBar
        if (View.GONE == visible){
            int option = SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            decorView.setSystemUiVisibility(option);

        }
    }

    private boolean isPermissionAllowed() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    private void checkPermission() {

        if (isPermissionAllowed()) {


//            startMainActivity();
        } else {



            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                showStorageFailDialog();

            } else {
                requestPermission();
//                showOpenStorageDialog();
            }
        }
    }

//    private void showOpenStorageDialog() {
//
//        final AppCompatDialog dialog = new AppCompatDialog(this, R.style.no_title_dialog);
//        View view = LayoutInflater.from(this).inflate(R.layout.dialog_open_storage, null);
//        view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                requestPermission();
//                dialog.dismiss();
//            }
//        });
//        int screenWidth = getResources().getDisplayMetrics().widthPixels;
//        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(screenWidth * 4 / 5, ViewGroup.LayoutParams.WRAP_CONTENT);
//        dialog.setContentView(view, params);
//        dialog.setCancelable(false);
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.show();
//    }


    private void requestPermission() {

        ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_CODE_STORAGE);

//        ActivityCompat.requestPermissions(this,
//                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE);
    }

    private void showStorageFailDialog() {

//       AppCompatDialog dialog = new AppCompatDialog(this, R.style.Theme_AppCompat_DayNight_Dialog);
//       dialog.getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//       dialog.setTitle("1111");
//       dialog.setContentView(R.layout.dialog_storage_fail);
//       Button exit = ( Button)findViewById(R.id.exit);
//       exit.setOnClickListener(this);
//        Button setting = ( Button)findViewById(R.id.start_setting);
//        exit.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//
//        });
//        setting.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//        mIsStartSetting = true;
//        startApplicationDetailsSetting();
//            }
//        });
//        dialog.setCancelable(false);
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.show();
//        View view = View.inflate(this,R.layout.dialog_loginerror,null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        dialog = builder
                .setView(R.layout.dialog_loginerror) //自定义的布局文件
                .create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        TextView text =  dialog.getWindow().findViewById(R.id.settingDeatil);
        text.setText("在设置-应用-"+ getString(R.string.app_name) +"-权限中开启存储空间权限，以正常使用App功能");
        dialog.getWindow().findViewById(R.id.start_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mIsStartSetting = true;
                dialog.dismiss(); //取消对话框
                startApplicationDetailsSetting();
            }
        });
        dialog.getWindow().findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); //取消对话框
                finish();
            }
        });


    }

    private boolean mIsStartSetting = false;

    private void startApplicationDetailsSetting() {
        Uri uri = Uri.parse("package:" + getPackageName());
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
        startActivity(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mIsStartSetting) {
            mIsStartSetting = false;
            if (isPermissionAllowed()) {

                dialog.dismiss();
            }else {
                checkPermission();
            }
        }
    }

    private void   checkCamer(){

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            // 申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat
                    .requestPermissions(
                            this,
                            new String[]{Manifest.permission.CAMERA},
                            TYPE_REQUEST_PERMISSION);
        }

    }



//    public class CommonDialog extends Dialog {
//        @Override
//        protected void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            View view = View.inflate(context,R.layout.dialog_loginerror,null);
//            setContentView(view);
//        }
//
//
//        public CommonDialog(Context context) {
//            super(context);
//        }
//
//        public CommonDialog(Context context, int themeResId) {
//            super(context, themeResId);
//        }
//
//        protected CommonDialog(Context context, boolean cancelable
//                , OnCancelListener cancelListener) {
//            super(context, cancelable, cancelListener);
//        }
//    }


    /**
     * 状态栏相关工具类
     *
     */
    public static void setWindowStatusBarColor(Activity activity, int colorResId) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(activity.getResources().getColor(colorResId));

                //底部导航栏
                //window.setNavigationBarColor(activity.getResources().getColor(colorResId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
