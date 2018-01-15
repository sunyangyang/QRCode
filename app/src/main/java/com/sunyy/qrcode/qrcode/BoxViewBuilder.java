/**
 * Copyright (C) 2015 The AndroidRCStudent Project
 */
package com.sunyy.qrcode.qrcode;

import com.hyena.framework.app.fragment.BaseUIFragment;
import com.hyena.framework.app.fragment.ViewBuilder;
import com.hyena.framework.app.widget.EmptyView;
import com.hyena.framework.app.widget.LoadingView;
import com.hyena.framework.app.widget.TitleBar;

/**
 * @name 通用页面视图构造器
 * @author Fanjb
 * @date 2015年9月1日
 */
public class BoxViewBuilder implements ViewBuilder {

	@Override
	public TitleBar buildTitleBar(BaseUIFragment<?> fragment) {
		BoxTitleBar titleBar = new BoxTitleBar(fragment.getActivity());
		titleBar.setBaseUIFragment(fragment);
		return titleBar;
	}

	@Override
	public EmptyView buildEmptyView(BaseUIFragment<?> fragment) {
		BoxEmptyView emptyView = new BoxEmptyView(fragment.getActivity());
		emptyView.setBaseUIFragment(fragment);
		return emptyView;
	}

	@Override
	public LoadingView buildLoadingView(BaseUIFragment<?> fragment) {
		BoxLoadingView loadingView = new BoxLoadingView(fragment.getActivity());
		loadingView.setBaseUIFragment(fragment);
		return loadingView;
	}
}
