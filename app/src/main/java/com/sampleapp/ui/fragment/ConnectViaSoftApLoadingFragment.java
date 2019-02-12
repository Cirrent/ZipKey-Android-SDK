package com.sampleapp.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cirrent.cirrentsdk.service.SoftApService;
import com.sampleapp.R;

public class ConnectViaSoftApLoadingFragment extends DirectConnectionLoadingFragment {
    private static final String SOFT_AP_SSID = "softApSsid";
    private String softApSsid;

    public static ConnectViaSoftApLoadingFragment newInstance(String softApSsid) {
        ConnectViaSoftApLoadingFragment fragment = new ConnectViaSoftApLoadingFragment();
        Bundle args = new Bundle();
        args.putString(SOFT_AP_SSID, softApSsid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            softApSsid = getArguments().getString(SOFT_AP_SSID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        setStatusText(view);
        connectToDeviceViaSoftAp();

        return view;
    }

    private void setStatusText(View view) {
        TextView textStatus = (TextView) view.findViewById(R.id.text_status);
        textStatus.setText(R.string.connecting_to_device_via_soft_ap);
    }

    private void connectToDeviceViaSoftAp() {
        //----- SDK call ------------
        SoftApService
                .getSoftApService()
                .setSoftApConnectTimings(10, 4)
                .connectToDeviceViaSoftAp(getContext(), softApSsid, new SoftApService.SoftApDeviceConnectionCallback() {
                    @Override
                    public void onDeviceConnectedSuccessfully() {
                        showFragment(ConnectedToSoftApFragment.newInstance(softApSsid), false);
                    }

                    @Override
                    public void onSoftApNetworkNotFound() {
                        Toast.makeText(getContext(), R.string.cant_find_soft_ap_network, Toast.LENGTH_SHORT).show();
                        showFragment(new HomeFragment(), false);
                    }

                    @Override
                    public void onConnectionFailed() {
                        int msgResId = R.string.cant_connect_to_device_soft_ap_network;
                        Toast.makeText(getContext(), msgResId, Toast.LENGTH_SHORT).show();
                        showFragment(new HomeFragment(), false);
                    }
                });
        //---------------------------
    }
}
