package com.sampleapp.ui.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.sampleapp.Prefs;
import com.sampleapp.R;

public class ConfigurationFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_configuration, container, false);

        setupViews(view);

        return view;
    }

    private void setupViews(View view) {
        final EditText editSoftApSsid = (EditText) view.findViewById(R.id.edit_soft_ap_ssid);
        final EditText editBlePrefix = (EditText) view.findViewById(R.id.edit_prefix);

        if (Prefs.SOFT_AP_SSID.exists()) {
            final String softApSsid = Prefs.SOFT_AP_SSID.getValue();
            editSoftApSsid.setText(String.valueOf(softApSsid));
        }

        if (Prefs.WCM_BLE_PREFIX.exists()) {
            final String prefix = Prefs.WCM_BLE_PREFIX.getValue();
            editBlePrefix.setText(String.valueOf(prefix));
        }

        view.findViewById(R.id.floating_action_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String softApSsid = String.valueOf(editSoftApSsid.getText()).trim();
                String blePrefix = String.valueOf(editBlePrefix.getText()).trim();

                if (softApSsid.isEmpty() && blePrefix.isEmpty()) {
                    Toast.makeText(getContext(), R.string.conf_both_empty, Toast.LENGTH_SHORT).show();
                } else {
                    Prefs.SOFT_AP_SSID.setValue(softApSsid);
                    Prefs.WCM_BLE_PREFIX.setValue(blePrefix);
                    Toast.makeText(getContext(), R.string.configuration_is_saved, Toast.LENGTH_LONG).show();
                    showFragment(new HomeFragment(), false);
                }
            }
        });
    }
}
