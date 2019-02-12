package com.sampleapp.ui.fragment.demo;


import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sampleapp.R;
import com.sampleapp.demo.DemoDevice;
import com.sampleapp.ui.fragment.BaseFragment;

import java.util.ArrayList;

public class DemoSetupDeviceAutomaticallyFragment extends BaseFragment
        implements View.OnClickListener {

    private static final String DEVICES = "devices";
    private static final String KNOWN_SSID = "knownSsid";
    private static final String PROVIDER_NAME = "providerName";

    private int connectionMethod;
    private ArrayList<DemoDevice> devices;
    private String knownSsid;
    private String providerName;

    private ImageView imgProviderLogo;
    private TextView textConnectionInfo;

    public static DemoSetupDeviceAutomaticallyFragment newInstance(int connectionMethod,
                                                                   ArrayList<DemoDevice> devices,
                                                                   String knownSsid,
                                                                   String providerName) {
        DemoSetupDeviceAutomaticallyFragment fragment = new DemoSetupDeviceAutomaticallyFragment();
        Bundle args = new Bundle();
        args.putInt(DemoStartFragment.CONNECTION_METHOD_CODE, connectionMethod);
        args.putSerializable(DEVICES, devices);
        args.putString(KNOWN_SSID, knownSsid);
        args.putString(PROVIDER_NAME, providerName);
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
            knownSsid = arguments.getString(KNOWN_SSID);
            providerName = arguments.getString(PROVIDER_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_setup_device_auto, container, false);

        changeActionBarState(false, true, getString(R.string.walkthru_mode));
        initViews(view);
        imgProviderLogo.setImageResource(R.drawable.comcast_xfinity_logo);
        textConnectionInfo.setText(buildConnectionInfoText());

        return view;
    }

    private void initViews(View view) {
        imgProviderLogo = (ImageView) view.findViewById(R.id.img_provider_logo);
        imgProviderLogo.setOnClickListener(this);
        textConnectionInfo = (TextView) view.findViewById((R.id.text_connection_info));
        view.findViewById(R.id.zipkey_logo).setOnClickListener(this);
        view.findViewById(R.id.text_choose_network).setOnClickListener(this);
        view.findViewById(R.id.floating_action_button).setOnClickListener(this);
    }

    private Spannable buildConnectionInfoText() {
        StringBuilder builder = new StringBuilder();
        builder.append(getString(R.string.connect_your)).append(" ");
        for (int i = 0; i < devices.size(); i++) {
            if (i > 0) builder.append(", ");
            builder.append(devices.get(i).getDeviceId());
        }
        builder.append(" ").append(getString(R.string.automatically_to)).append(" ");
        builder.append(knownSsid);
        builder.append(" ").append(getString(R.string.network_using)).append(" ");
        builder.append(providerName);

        final String connectionInfoText = String.valueOf(builder);
        Spannable spannable = new SpannableString(connectionInfoText);
        spannable.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), connectionInfoText.indexOf(knownSsid), connectionInfoText.indexOf(knownSsid) + knownSsid.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannable;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_provider_logo:
            case R.id.floating_action_button:
                showFragment(DemoSendCredentialsFragment.newInstance(
                        connectionMethod, devices, knownSsid
                ), true);
                break;

            case R.id.zipkey_logo:
                showFragment(new DemoZipkeyInfoFragment(), true);
                break;

            case R.id.text_choose_network:
                DemoSetupDeviceManuallyFragment fragment = DemoSetupDeviceManuallyFragment
                        .newInstance(DemoStartFragment.CONNECTION_METHOD_2, devices);
                showFragment(fragment, true);
                break;
        }
    }

}
