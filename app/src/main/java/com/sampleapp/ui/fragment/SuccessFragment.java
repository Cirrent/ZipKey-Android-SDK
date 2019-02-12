package com.sampleapp.ui.fragment;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cirrent.cirrentsdk.net.model.Device;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sampleapp.R;
import com.sampleapp.Utils;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;

public class SuccessFragment extends BaseFragment implements View.OnClickListener {
    private static final String DEVICE_ID = "deviceId";
    private static final String NETWORK_SSID = "networkSsid";
    private static final String DEVICE = "device";

    private Device selectedDevice;
    private String serializedSelectedDevice;
    private String deviceId;
    private String networkSsid;
    private FloatingActionButton layoutFloatingButton;
    private TextView textSuccessInfo;
    private Button buttonAddAnotherProduct;
    private EditText editFriendlyName;
    private LinearLayout layoutProviderInfo;
    private ImageView imageProviderLogo;
    private TextView textProviderInfo;
    private TextView textDeviceType;

    public static SuccessFragment newInstance(String deviceId, String networkSsid, String serializedSelectedDevice) {
        SuccessFragment fragment = new SuccessFragment();
        Bundle args = new Bundle();
        args.putString(DEVICE_ID, deviceId);
        args.putString(NETWORK_SSID, networkSsid);
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
            if (serializedSelectedDevice != null && !serializedSelectedDevice.isEmpty()) {
                Type type = new TypeToken<Device>() {
                }.getType();
                selectedDevice = new Gson().fromJson(serializedSelectedDevice, type);
            }

            deviceId = arguments.getString(DEVICE_ID);
            networkSsid = arguments.getString(NETWORK_SSID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_success, container, false);

        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        changeActionBarState(false, true, "");
        initViews(view);
        fillSuccessInfo();

        return view;
    }

    private void initViews(View view) {
        textDeviceType = (TextView) view.findViewById(R.id.text_hint_device_name);
        textSuccessInfo = (TextView) view.findViewById(R.id.text_success_info);
        editFriendlyName = (EditText) view.findViewById(R.id.edit_device_name);
        buttonAddAnotherProduct = (Button) view.findViewById(R.id.button_add_another_product);
        layoutFloatingButton = (FloatingActionButton) view.findViewById(R.id.floating_action_button);
        layoutProviderInfo = (LinearLayout) view.findViewById(R.id.provider_info);
        imageProviderLogo = (ImageView) view.findViewById(R.id.img_success_provider_logo);
        textProviderInfo = (TextView) view.findViewById(R.id.text_success_provider_info);

        buttonAddAnotherProduct.setOnClickListener(this);
        layoutFloatingButton.setOnClickListener(this);
        layoutProviderInfo.setOnClickListener(this);
    }

    private void fillSuccessInfo() {
        fillDeviceType();

        textSuccessInfo.setText(deviceId + " is now connected to " + networkSsid);
        editFriendlyName.setText(deviceId);

        if (serializedSelectedDevice != null && !serializedSelectedDevice.isEmpty()) {
            Picasso.with(getContext()).load(selectedDevice.getProviderAttributionLogo()).into(imageProviderLogo);
            textProviderInfo.setText("Product used " + selectedDevice.getProviderAttribution() + " with ZipKey to simplify your setup. Learn more.");
            layoutProviderInfo.setVisibility(View.VISIBLE);
        } else {
            layoutProviderInfo.setVisibility(View.GONE);
        }
    }

    private void fillDeviceType() {
        String deviceType;
        if (selectedDevice != null) {
            deviceType = selectedDevice.getDeviceType();
        } else {
            deviceType = deviceId;
        }
        textDeviceType.setText(getString(R.string.name_your) + " " + deviceType);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.provider_info:
                showMoreInfo();
                break;
            case R.id.button_add_another_product:
                break;
            case R.id.floating_action_button:
                String friendlyName = String.valueOf(editFriendlyName.getText());
                if (friendlyName.trim().length() > 0 && !friendlyName.equals(deviceId)) {
                    Utils.setFriendlyName(deviceId, friendlyName);
                }

                showFragment(new HomeFragment(), false);
                break;
        }
    }

    private void showMoreInfo() {
        try {
            Uri uri = Uri.parse(selectedDevice.getProviderAttributionLearnMoreURL());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), R.string.no_web_browser, Toast.LENGTH_LONG).show();
        }
    }
}
