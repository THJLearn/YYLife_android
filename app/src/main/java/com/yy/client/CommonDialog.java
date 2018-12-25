package com.yy.client;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

class CommonDialog extends Dialog {

    public Context context;

    public CommonDialog(Context context) {
        super(context);
        this.context = context;
    }

    public CommonDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    protected CommonDialog(Context context, boolean cancelable
            , OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setCanceledOnTouchOutside(false);

        View view = View.inflate(context,R.layout.dialog_loginerror,null);
        setContentView(view);

        Window win = getWindow();
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.height = DensityUtil.dip2px(context, 200);
        lp.width = DensityUtil.dip2px(context, 300);

        win.setAttributes(lp);


//        view.findViewById(R.id.exit).setOnClickListener(this);
//        view.findViewById(R.id.start_setting).setOnClickListener(this);
//        view.findViewById(R.id.start_setting).setOnClickListener(new Button.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });

    }

    /**
     * 设置确定取消按钮的回调
     */
    public OnClickBottomListener onClickBottomListener;
    public CommonDialog setOnClickBottomListener(OnClickBottomListener onClickBottomListener) {
        this.onClickBottomListener = onClickBottomListener;
        return this;
    }
    public interface OnClickBottomListener{
        /**
         * 点击确定按钮事件
         */
        public void onPositiveClick();
        /**
         * 点击取消按钮事件
         */
        public void onNegtiveClick();
    }

//    @Override
//    public void onClick(View view) {
//
//        switch (view.getId()){
//            case R.id.exit :
//                Toast.makeText(context, "退出", Toast.LENGTH_SHORT).show();
//                break;
//            case R.id.start_setting:
//                Toast.makeText(context, "继续设置", Toast.LENGTH_SHORT).show();
//                break;
//        }
//    }
}
