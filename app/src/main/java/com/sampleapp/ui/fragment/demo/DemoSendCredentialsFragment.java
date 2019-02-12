package com.sampleapp.ui.fragment.demo;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sampleapp.R;
import com.sampleapp.Utils;
import com.sampleapp.demo.DemoDevice;
import com.sampleapp.ui.fragment.BaseFullScreenFragment;

import java.util.ArrayList;

public class DemoSendCredentialsFragment extends BaseFullScreenFragment {

    private static final String DEVICES = "devices";
    private static final String NETWORK_SSID = "networkSsid";

    private int connectionMethod;
    private ArrayList<DemoDevice> devices;
    private String networkSsid;

    public static DemoSendCredentialsFragment newInstance(int connectionMethod,
                                                          ArrayList<DemoDevice> devices,
                                                          String networkSsid) {
        DemoSendCredentialsFragment fragment = new DemoSendCredentialsFragment();
        Bundle args = new Bundle();
        args.putInt(DemoStartFragment.CONNECTION_METHOD_CODE, connectionMethod);
        args.putSerializable(DEVICES, devices);
        args.putString(NETWORK_SSID, networkSsid);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            connectionMethod = arguments.getInt(DemoStartFragment.CONNECTION_METHOD_CODE);
            devices = (ArrayList<DemoDevice>) arguments.getSerializable(DEVICES);
            networkSsid = arguments.getString(NETWORK_SSID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_send_credentials_loading, container, false);
        processZipKeyLogoVisibility(connectionMethod, view);
        Utils.startConstantVerticalViewAnimation(view.findViewById(R.id.logo));
        changeActionBarState(true, false, "");

        TextView vTextView = (TextView) view.findViewById(R.id.text_connecting_to);
        vTextView.setText(getString(R.string.connecting_to) + " "
                + networkSsid + " " + getString(R.string.network_ellipsis));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getContext() == null) return;
                showFragment(DemoSuccessFragment.newInstance(
                        connectionMethod, devices, networkSsid), true);
            }
        }, 2 * 1000);

        return view;
    }

    private void processZipKeyLogoVisibility(int connectionMethod, View view) {
        switch (connectionMethod) {
            case DemoStartFragment.CONNECTION_METHOD_1:
            case DemoStartFragment.CONNECTION_METHOD_2:
                view.findViewById(R.id.img_zipkey_logo).setVisibility(View.VISIBLE);
                break;
            case DemoStartFragment.CONNECTION_METHOD_3:
                view.findViewById(R.id.img_zipkey_logo).setVisibility(View.GONE);
                break;
        }
    }

}
