package com.sampleapp.ui.fragment.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sampleapp.R;
import com.sampleapp.ui.fragment.BaseFragment;

public class DemoDeviceInfoFragment extends BaseFragment {

    private static final String DEVICE_ID = "deviceId";
    private static final String DEVICE_IMAGE_RES_ID = "deviceImageResId";

    private int deviceImageResId;
    private String deviceId;

    public static DemoDeviceInfoFragment newInstance(String deviceId, int deviceImageResId) {
        DemoDeviceInfoFragment fragment = new DemoDeviceInfoFragment();
        Bundle args = new Bundle();
        args.putString(DEVICE_ID, deviceId);
        args.putInt(DEVICE_IMAGE_RES_ID, deviceImageResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deviceId = getArguments().getString(DEVICE_ID);
            deviceImageResId = getArguments().getInt(DEVICE_IMAGE_RES_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_device_info, container, false);

        changeActionBarState(false, true, getString(R.string.walkthru_mode));
        setupViews(view);

        return view;
    }

    private void setupViews(View view) {
        TextView textDeviceId = (TextView) view.findViewById(R.id.text_device_id);
        textDeviceId.setText(deviceId);

        ImageView imgDevice = (ImageView) view.findViewById(R.id.img_device);
        imgDevice.setImageResource(deviceImageResId);
    }

}
