package com.sampleapp.ui.fragment.demo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.cirrent.cirrentsdk.net.model.WiFiNetwork;
import com.sampleapp.R;
import com.sampleapp.Utils;
import com.sampleapp.demo.DemoDevice;
import com.sampleapp.ui.fragment.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class DemoSetupDeviceManuallyFragment extends BaseFragment {

    private static final String DEVICES = "devices";
    private static final String MANAGED_DEVICE_SETUP_PARAM_NAME = "managedSetup";

    private int connectionMethod;
    private boolean isManagedDeviceSetup;
    private ArrayList<DemoDevice> devices;
    private String selectedNetworkSsid;
    private List<WiFiNetwork> deviceCandidateNetworks;

    private Spinner spinnerSsidList;
    private EditText editTextPassword;
    private FloatingActionButton layoutFloatingButton;

    public static DemoSetupDeviceManuallyFragment newInstance(int connectionMethod, ArrayList<DemoDevice> devices) {
        DemoSetupDeviceManuallyFragment fragment = new DemoSetupDeviceManuallyFragment();
        Bundle args = new Bundle();
        args.putInt(DemoStartFragment.CONNECTION_METHOD_CODE, connectionMethod);
        args.putSerializable(DEVICES, devices);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            connectionMethod = getArguments().getInt(DemoStartFragment.CONNECTION_METHOD_CODE);
            devices = (ArrayList<DemoDevice>) getArguments().getSerializable(DEVICES);
            isManagedDeviceSetup = getArguments().getBoolean(MANAGED_DEVICE_SETUP_PARAM_NAME);
        }

        if (isManagedDeviceSetup) {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_reset).setVisible(false);
        menu.findItem(R.id.action_networks).setVisible(false);
        menu.findItem(R.id.action_edit_name).setVisible(false);
        menu.findItem(R.id.action_edit_name).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_setup_network, container, false);

        changeActionBarState(false, true, getString(R.string.walkthru_mode));
        initViews(view);
        getDeviceInfoViaSoftAp();

        return view;
    }

    private void initViews(View view) {
        spinnerSsidList = (Spinner) view.findViewById(R.id.list_wifi_networks);
        editTextPassword = (EditText) view.findViewById(R.id.edit_pre_shared_password);
        final CheckBox checkBoxShowPassword = (CheckBox) view.findViewById(R.id.checkbox_show_password);
        layoutFloatingButton = (FloatingActionButton) view.findViewById(R.id.floating_action_button);

        view.findViewById(R.id.layout_checkbox_add_hidden_network).setVisibility(View.GONE);
        view.findViewById(R.id.layout_remember_password).setVisibility(View.VISIBLE);
        view.findViewById(R.id.info_remember_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new DemoAcmeRememberCredentialsFragment(), true);
            }
        });
        setupShowPassCheckboxListener(checkBoxShowPassword);
        setupFloatingButtonListener();
    }

    private void setupShowPassCheckboxListener(CheckBox checkBoxShowPassword) {
        checkBoxShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    editTextPassword.setSelection(editTextPassword.getText().length());
                } else {
                    editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    editTextPassword.setSelection(editTextPassword.getText().length());
                }
            }
        });
    }

    private void setupFloatingButtonListener() {
        layoutFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedNetworkSsid == null) {
                    int msgResId = R.string.select_network;
                    Toast.makeText(getContext(), msgResId, Toast.LENGTH_SHORT).show();
                    return;
                }

                Utils.hideKeyboard(getActivity());
                showFragment(DemoSendCredentialsFragment.newInstance(
                        connectionMethod, devices, selectedNetworkSsid
                ), true);
            }
        });
    }

    private void getDeviceInfoViaSoftAp() {
        deviceCandidateNetworks = new ArrayList<>();
        deviceCandidateNetworks.add(new WiFiNetwork("XYZ", "XYZ"));
        deviceCandidateNetworks.add(new WiFiNetwork("other", "other"));
        setSpinnerAdapter();
    }

    private void setSpinnerAdapter() {
        final List<String> ssidList = getSsidNames();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), R.layout.item_spinner, ssidList);
        spinnerSsidList.setAdapter(spinnerAdapter);
        spinnerSsidList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedNetworkSsid = ssidList.get(position);
                layoutFloatingButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { /* nothing */ }
        });
    }

    @NonNull
    private List<String> getSsidNames() {
        List<String> ssidList = new ArrayList<>();
        for (WiFiNetwork environment : deviceCandidateNetworks) {
            ssidList.add(environment.getSsid());
        }
        return ssidList;
    }

}
