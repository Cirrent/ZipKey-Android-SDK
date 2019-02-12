package com.sampleapp.ui.fragment;


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cirrent.cirrentsdk.internal.net.model.DeviceDto;
import com.cirrent.cirrentsdk.net.model.Device;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sampleapp.R;
import com.sampleapp.ui.fragment.demo.DemoZipkeyInfoFragment;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;

public class SetupDeviceAutomaticallyFragment extends BaseFragment implements View.OnClickListener {
    private static final String DEVICE = "device";

    private Device selectedDevice;
    private String serializedSelectedDevice;
    private ImageView imgProviderLogo;
    private TextView textConnectionInfo;
    private DeviceDto.ProviderKnownNetwork providerKnownNetwork;

    public static SetupDeviceAutomaticallyFragment newInstance(String serializedSelectedDevice) {
        SetupDeviceAutomaticallyFragment fragment = new SetupDeviceAutomaticallyFragment();
        Bundle args = new Bundle();
        args.putString(DEVICE, serializedSelectedDevice);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            serializedSelectedDevice = getArguments().getString(DEVICE);
            Type type = new TypeToken<Device>() {
            }.getType();
            selectedDevice = new Gson().fromJson(serializedSelectedDevice, type);
            providerKnownNetwork = selectedDevice.getProviderKnownNetwork();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_setup_device_auto, container, false);

        changeActionBarState(false, true, "");
        initViews(view);
        loadProviderLogo();
        setConnectionInfoText();

        return view;
    }

    private void initViews(View view) {
        imgProviderLogo = (ImageView) view.findViewById(R.id.img_provider_logo);
        textConnectionInfo = (TextView) view.findViewById((R.id.text_connection_info));
        TextView textChooseNetwork = (TextView) view.findViewById(R.id.text_choose_network);
        FloatingActionButton layoutFloatingButton = (FloatingActionButton) view.findViewById(R.id.floating_action_button);
        textChooseNetwork.setOnClickListener(this);
        layoutFloatingButton.setOnClickListener(this);
        imgProviderLogo.setOnClickListener(this);
        view.findViewById(R.id.zipkey_logo).setOnClickListener(this);
    }

    private void loadProviderLogo() {
        if (providerKnownNetwork != null) {
            Picasso.with(getContext()).load(providerKnownNetwork.getProviderLogo()).into(imgProviderLogo);
        }
    }

    private void setConnectionInfoText() {
        textConnectionInfo.setText(buildConnectionInfoText());
    }

    private Spannable buildConnectionInfoText() {
        final String knownSsid = providerKnownNetwork.getSsid();
        StringBuilder builder = new StringBuilder();
        builder.append("Connect your ");
        builder.append(selectedDevice.getDeviceId());
        builder.append(" automatically to ");
        builder.append(knownSsid);
        builder.append(" Network using ");
        builder.append(providerKnownNetwork.getProviderName());

        final String connectionInfoText = String.valueOf(builder);
        Spannable spannable = new SpannableString(connectionInfoText);
        spannable.setSpan(
                new StyleSpan(Typeface.BOLD_ITALIC),
                connectionInfoText.indexOf(knownSsid),
                connectionInfoText.indexOf(knownSsid) + knownSsid.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        return spannable;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_provider_logo:
            case R.id.floating_action_button:
                showFragment(SendProviderCredentialsFragment.newInstance(serializedSelectedDevice), false);
                break;
            case R.id.text_choose_network:
                showFragment(SetupDeviceManuallyFragment.newInstance(serializedSelectedDevice), false);
                break;
            case R.id.zipkey_logo:
                showFragment(new DemoZipkeyInfoFragment(), true);
                break;
        }
    }
}
