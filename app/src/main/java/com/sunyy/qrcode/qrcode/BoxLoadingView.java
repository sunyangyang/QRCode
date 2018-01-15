/**
 * Copyright (C) 2015 The AndroidRCStudent Project
 */
package com.sunyy.qrcode.qrcode;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyena.framework.app.widget.LoadingView;
import com.hyena.framework.utils.UiThreadHandler;

/**
 * @name 速算总动员老师端通用页面加载动画视图
 * @author Fanjb
 * @date 2015年9月1日
 */
public class BoxLoadingView extends LoadingView {

	private ImageView mLoadingImg;
	private TextView mLoadingHintTxt;

	public BoxLoadingView(Context context) {
		super(context);
		initView();
	}

	private void initView() {
	}

	@Override
	public void showLoading(final String hint) {
	}
}
