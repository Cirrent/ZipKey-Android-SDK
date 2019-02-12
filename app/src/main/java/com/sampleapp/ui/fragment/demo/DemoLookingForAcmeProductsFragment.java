package com.sampleapp.ui.fragment.demo;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sampleapp.R;
import com.sampleapp.Utils;
import com.sampleapp.ui.fragment.BaseFullScreenFragment;

public class DemoLookingForAcmeProductsFragment extends BaseFullScreenFragment {

    private int connectionMethod;

    public static DemoLookingForAcmeProductsFragment newInstance(int connectionMethodCode) {
        DemoLookingForAcmeProductsFragment fragment =
                new DemoLookingForAcmeProductsFragment();
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_device_loading, container, false);
        Utils.startConstantVerticalViewAnimation(view.findViewById(R.id.logo));
        changeActionBarState(true, false, "");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getContext() == null) return;
                showFragment(DemoAddDeviceFragment.newInstance(connectionMethod), true);
            }
        }, 2 * 1000);
        return view;
    }

}
