package com.sampleapp.ui.fragment.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sampleapp.R;
import com.sampleapp.ui.fragment.BaseFullScreenFragment;

public class DemoStartFragment extends BaseFullScreenFragment
        implements View.OnClickListener {

    public static final String CONNECTION_METHOD_CODE = "demo_mode_method";
    public static final int CONNECTION_METHOD_1 = 1;
    public static final int CONNECTION_METHOD_2 = 2;
    public static final int CONNECTION_METHOD_3 = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_demo_start, container, false);

        setupViews(view);
        changeActionBarState(true, true, getString(R.string.walkthru_mode));

        return view;
    }

    private void setupViews(View view) {
        view.findViewById(R.id.layout_best).setOnClickListener(this);
        view.findViewById(R.id.layout_better).setOnClickListener(this);
        view.findViewById(R.id.layout_good).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_best:
                getAnalyticsService().logEvent("demo_flow", "zipkey_subscriber");
                showFragment(DemoGuideFragment.newInstance(CONNECTION_METHOD_1), true);
                break;
            case R.id.layout_better:
                getAnalyticsService().logEvent("demo_flow", "zipkey_coverage");
                showFragment(DemoGuideFragment.newInstance(CONNECTION_METHOD_2), true);
                break;
            case R.id.layout_good:
                getAnalyticsService().logEvent("demo_flow", "soft_ap");
                showFragment(DemoGuideFragment.newInstance(CONNECTION_METHOD_3), true);
                break;
        }
    }

}
