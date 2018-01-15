package com.sunyy.qrcode.qrcode;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sunyy.qrcode.mylibrary.QRCommonFragment;
import com.sunyy.qrcode.mylibrary.QRFragment;
import com.sunyy.qrcode.mylibrary.ResultListener;

/**
 * Created by sunyangyang on 2018/1/15.
 */

public class CommonActivity extends AppCompatActivity implements ResultListener {
    private QRCommonFragment mFragment;
    private QRView mQRView;
    private LinearLayout mLayout;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mFragment = new QRCommonFragment();
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
