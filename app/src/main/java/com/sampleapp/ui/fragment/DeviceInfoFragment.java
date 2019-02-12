package com.sampleapp.ui.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sampleapp.R;
import com.sampleapp.Utils;
import com.squareup.picasso.Picasso;

public class DeviceInfoFragment extends BaseFragment {
    private static final String DEVICE_ID = "deviceId";
    private static final String DEVICE_IMAGE_URL = "deviceImageUrl";

    private String deviceId;
    private String deviceImageUrl;

    public DeviceInfoFragment() {
    }

    public static DeviceInfoFragment newInstance(String deviceId, String deviceImageUrl) {
        DeviceInfoFragment fragment = new DeviceInfoFragment();
        Bundle args = new Bundle();
        args.putString(DEVICE_ID, deviceId);
        args.putString(DEVICE_IMAGE_URL, deviceImageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deviceId = getArguments().getString(DEVICE_ID);
            deviceImageUrl = getArguments().getString(DEVICE_IMAGE_URL);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_device_info, container, false);

        changeActionBarState(false, false, "");
        setupViews(view);

        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_reset).setVisible(true);
        menu.findItem(R.id.action_networks).setVisible(true);
        menu.findItem(R.id.action_edit_name).setVisible(true);
        menu.findItem(R.id.action_perform_action).setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }

    private void setupViews(View view) {
        TextView textDeviceId = (TextView) view.findViewById(R.id.text_device_id);
        textDeviceId.setText(Utils.getFriendlyName(deviceId));

        ImageView imgDevice = (ImageView) view.findViewById(R.id.img_device);
        Picasso.with(getContext()).load(deviceImageUrl).into(imgDevice);
    }
}
