package com.sunyy.qrcode.qrcode;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sunyy.qrcode.mylibrary.QRFragment;

/**
 * Created by sunyangyang on 2018/1/15.
 */

public class ProjectionFragment extends QRFragment {
    public static final String PROJECTION_ACTION_RESULT = "projection_action_result";

    @Override
    public void onCreateImpl(Bundle icicle) {
        super.onCreateImpl(icicle);
        setSlideable(true);
    }


    @Override
    public View onCreateViewImpl(Bundle savedInstanceState) {
        View contentView = super.onCreateViewImpl(savedInstanceState);
        View myView = View.inflate(getActivity(), R.layout.fragment_projection, null);
        RelativeLayout relativeLayout = (RelativeLayout) contentView.findViewById(R.id.container);
        relativeLayout.addView(myView);
        return contentView;
    }

    @Override
    public void onViewCreatedImpl(View view, Bundle savedInstanceState) {
        super.onViewCreatedImpl(view, savedInstanceState);
        getTitleBar().setTitleVisible(false);
        view.findViewById(R.id.viewfinder_view).setVisibility(View.GONE);
    }

    @Override
    public void getResult(String s) {
        Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();

    }

    @Override
    public int setViewVisible() {
        return View.GONE;
    }

}
