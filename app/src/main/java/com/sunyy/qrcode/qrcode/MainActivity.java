package com.sunyy.qrcode.qrcode;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.sunyy.qrcode.mylibrary.QRFragment;
import com.sunyy.qrcode.mylibrary.ResultListener;

public class MainActivity extends AppCompatActivity implements ResultListener {
    private QRFragment mFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mFragment = new QRFragment();
        android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.container, new QRFragment());
        transaction.commit();
    }

    @Override
    public void getResult(String result) {
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    }
}
