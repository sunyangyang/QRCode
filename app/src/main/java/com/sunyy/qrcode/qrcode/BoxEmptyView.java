/**
 * Copyright (C) 2015 The AndroidRCStudent Project
 */
package com.sunyy.qrcode.qrcode;

import android.content.Context;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyena.framework.app.coretext.Html;
import com.hyena.framework.app.widget.EmptyView;
import com.hyena.framework.utils.UiThreadHandler;

/**
 * @name 速算总动员老师端通用空页面视图
 * @author Fanjb
 * @date 2015年9月1日
 */
public class BoxEmptyView extends EmptyView {

	private ImageView mEmptyHintImg;
	private TextView mEmptyHintTxt;
	private TextView mDescText;
	private TextView mEmptyFaqText;
	private TextView mEmptyBtn;

	public BoxEmptyView(Context context) {
		super(context);
	}

	@Override
	public void showNoNetwork() {
	}

	@Override
	public void showEmpty(String errorCode, String hint) {
	}

	public void showEmpty(String hint){
	}

	public void showEmpty(final int resId, String hint) {
		showEmpty(resId, hint, null, null, null);
	}

	public void showEmpty(final int resId, final String hint, final String desc,
			final String btnTxt, final OnClickListener btnClickListener) {
		UiThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				if (mEmptyHintImg != null)
					mEmptyHintImg.setImageResource(resId);

				if (null != mEmptyHintTxt) {
					if (null != hint && !TextUtils.isEmpty(hint)) {
						mEmptyHintTxt.setVisibility(VISIBLE);
						mEmptyHintTxt.setText(hint);
					}else {
						mEmptyHintTxt.setVisibility(INVISIBLE);
					}
				}

				if (null != mDescText) {
					if (null != desc && !TextUtils.isEmpty(desc)) {
						mDescText.setVisibility(VISIBLE);
						mDescText.setText(Html.fromHtml(desc));
					}else {
						mDescText.setVisibility(GONE);
					}
				}

				if (mEmptyBtn != null) {
					if (btnTxt != null) {
						mEmptyBtn.setVisibility(VISIBLE);
						mEmptyBtn.setText(btnTxt);
					} else {
						mEmptyBtn.setVisibility(INVISIBLE);
					}
					if (btnClickListener != null) {
						mEmptyBtn.setOnClickListener(btnClickListener);
					}
				}
				if (getBaseUIFragment() != null && getBaseUIFragment().getLoadingView() != null) {
					getBaseUIFragment().getLoadingView().clearAnimation();
					getBaseUIFragment().getLoadingView().setVisibility(INVISIBLE);
					setVisibility(VISIBLE);
				}
			}
		});
	}

	/**
	 * 设置空页面的附加信息
	 * 
	 * @param faqText
	 * @param listener
	 */
	public void setFaqText(final Spanned faqText, final OnClickListener listener) {
		UiThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				mEmptyFaqText.setVisibility(VISIBLE);
				mEmptyFaqText.setText(faqText);
				mEmptyFaqText.setOnClickListener(listener);
			}
		});
	}
}
