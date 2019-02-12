package com.sampleapp.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sampleapp.R;

public class ConnectedToSoftApFragment extends BaseFragment {

    private static final String SOFT_AP_SSID = "softApSsid";
    private String softApSsid;

    public static ConnectedToSoftApFragment newInstance(String softApSsid) {
        ConnectedToSoftApFragment fragment = new ConnectedToSoftApFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_soft_ap_connected, container, false);

        changeActionBarState(false, true, "");
        setupViews(view);

        return view;
    }

    private void setupViews(View view) {
        TextView textConnected = (TextView) view.findViewById(R.id.text_title);
        textConnected.setText(getString(R.string.connected_to) + " " + softApSsid);

        View vFab = view.findViewById(R.id.fab_active);
        vFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new SetupDeviceViaSoftApFragment(), false);
            }
        });
    }

}
