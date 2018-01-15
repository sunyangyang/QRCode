package com.sunyy.qrcode.qrcode;

import android.app.Application;

import com.hyena.framework.servcie.ServiceProvider;
import com.hyena.framework.utils.BaseApp;

/**
 * Created by sunyangyang on 2018/1/15.
 */

public class App extends BaseApp {

    @Override
    public void onCreate() {
        super.onCreate();
        ServiceProvider.getServiceProvider().registServiceManager(new BoxServiceManager());
    }
}
