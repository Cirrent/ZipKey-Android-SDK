package com.sampleapp.ui.fragment.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sampleapp.R;
import com.sampleapp.ui.fragment.BaseFullScreenFragment;

public class DemoGuideFragment extends BaseFullScreenFragment
        implements View.OnClickListener {

    private int connectionMethod;

    public static DemoGuideFragment newInstance(int connectionMethodCode) {
        DemoGuideFragment fragment = new DemoGuideFragment();
        Bundle args = new Bundle();
        args.putInt(DemoStartFragment.CONNECTION_METHOD_CODE, connectionMethodCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            connectionMethod = getArguments().getInt(DemoStartFragment.CONNECTION_METHOD_CODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_demo_guide, container, false);

        initViews(view);
        changeActionBarState(true, false, "");

        return view;
    }

    private void initViews(View view) {
        view.findViewById(R.id.text_preview_steps).setOnClickListener(this);
        view.findViewById(R.id.button_ready).setOnClickListener(this);
        view.findViewById(R.id.button_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_preview_steps:
                showFragment(new PreviewEasySetUpStepsFragment(), true);
                break;
            case R.id.button_ready:
                startDemo();
                break;
            case R.id.button_back:
                getActivity().onBackPressed();
                break;
        }
    }

    private void startDemo() {
        showFragment(DemoLookingForAcmeProductsFragment.newInstance(connectionMethod), true);
    }

}
