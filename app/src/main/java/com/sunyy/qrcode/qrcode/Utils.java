package com.sunyy.qrcode.qrcode;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by sunyangyang on 2018/1/11.
 */

public class Utils {
    public static int dip2px(Context context, float dpValue) {
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        float scale = dm.density;
        return (int)(dpValue * scale + 0.5F);
    }
}
