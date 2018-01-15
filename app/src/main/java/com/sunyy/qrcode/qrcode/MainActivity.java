package com.sunyy.qrcode.qrcode;

import android.graphics.Rect;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hyena.framework.app.activity.NavigateActivity;
import com.sunyy.qrcode.mylibrary.QRFragment;
import com.sunyy.qrcode.mylibrary.ResultListener;

public class MainActivity extends NavigateActivity implements ResultListener {
    private QRFragment mFragment;
    private QRView mQRView;
    private LinearLayout mLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mFragment = (QRFragment) Fragment.instantiate(this, QRFragment.class.getName());
        mFragment.setParent(this, null);
        android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.container, mFragment);
        transaction.commitAllowingStateLoss();
        mQRView = (QRView) findViewById(R.id.qr_view);
        mLayout = (LinearLayout) findViewById(R.id.layout_title);
        mQRView.setListener(new QRView.RectListener() {
            @Override
            public void getRect(Rect rect) {
                mLayout.setPadding(0, rect.bottom, 0, 0);
            }
        });
    }

    @Override
    public void getResult(String result) {
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    }

    @Override
    public int setViewVisible() {
        return View.GONE;
    }
}
