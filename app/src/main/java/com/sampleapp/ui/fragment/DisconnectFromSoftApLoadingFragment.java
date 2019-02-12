package com.sampleapp.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cirrent.cirrentsdk.service.SoftApService;
import com.sampleapp.Prefs;
import com.sampleapp.R;

public class DisconnectFromSoftApLoadingFragment extends DirectConnectionLoadingFragment {
    private static final String SOFT_AP_SSID = "softApSsid";
    private String softApSsid;

    public static DisconnectFromSoftApLoadingFragment newInstance(String softApSsid) {
        DisconnectFromSoftApLoadingFragment fragment = new DisconnectFromSoftApLoadingFragment();
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
        leaveSoftApNetwork();

        return view;
    }

    private void leaveSoftApNetwork() {
        //----- SDK call ------------
        SoftApService
                .getSoftApService()
                .setSoftApDisconnectTimings(7, 5)
                .leaveSoftApNetwork(getContext(), softApSsid, Prefs.WIFI_NETWORK_ID.getValue(), new SoftApService.InternetConnectionCallback() {
                    @Override
                    public void onReturnedToNetworkWithInternet() {
                        if (Prefs.SOFT_AP_DEVICE_SETUP_DATA.exists()) {
                            continueInterruptedDeviceSetup();
                        } else {
                            showFragment(new HomeFragment(), false);
                        }
                    }

                    @Override
                    public void onFailedToReturnToNetworkWithInternet() {
                        Toast.makeText(getContext(), R.string.failed_to_return_to_network, Toast.LENGTH_LONG).show();
                        showFragment(new HomeFragment(), false);
                    }
                });
        //---------------------------
    }

    private void continueInterruptedDeviceSetup() {
        String serializedSetupData = Prefs.SOFT_AP_DEVICE_SETUP_DATA.getValue();
        showFragment(SendCredentialsViaSoftApFragment.newInstance(serializedSetupData), false);
    }

    private void setStatusText(View view) {
        TextView textStatus = (TextView) view.findViewById(R.id.text_status);
        textStatus.setText(R.string.disconnect_from_soft_ap);
    }
}
