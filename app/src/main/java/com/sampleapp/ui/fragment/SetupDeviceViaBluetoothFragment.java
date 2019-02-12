package com.sampleapp.ui.fragment;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.cirrent.cirrentsdk.net.model.WiFiNetwork;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sampleapp.R;
import com.sampleapp.Utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SetupDeviceViaBluetoothFragment extends SetupDeviceDirectlyFragment {
    private static final String DEVICE_ID = "deviceId";
    private static final String CANDIDATE_NETWORKS = "candidateNetworks";

    private String deviceId;
    private String serializedCandidateNetworks;
    private List<WiFiNetwork> deviceCandidateNetworks;

    public static SetupDeviceViaBluetoothFragment newInstance(String deviceId, String serializedCandidateNetworks) {
        SetupDeviceViaBluetoothFragment fragment = new SetupDeviceViaBluetoothFragment();
        Bundle args = new Bundle();
        args.putString(DEVICE_ID, deviceId);
        args.putString(CANDIDATE_NETWORKS, serializedCandidateNetworks);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deviceId = getArguments().getString(DEVICE_ID);
            serializedCandidateNetworks = getArguments().getString(CANDIDATE_NETWORKS);
            Type type = new TypeToken<List<WiFiNetwork>>() {
            }.getType();
            deviceCandidateNetworks = new Gson().fromJson(serializedCandidateNetworks, type);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        setSpinnerAdapter();

        return view;
    }

    @Override
    public void setFloatingButtonOnClickListener(boolean isHiddenNetworkSetup) {
        final FloatingActionButton layoutFloatingButton = getLayoutFloatingButton();
        if (isHiddenNetworkSetup) {
            layoutFloatingButton.setOnClickListener(hiddenSsidListener);
        } else {
            layoutFloatingButton.setOnClickListener(visibleSsidListener);
        }
    }

    private void setSpinnerAdapter() {
        final List<String> ssidList = new ArrayList<>(getSsidNames(deviceCandidateNetworks));
        final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getContext(), R.layout.item_spinner, ssidList);

        final Spinner spinnerSsidList = getSsidListSpinner();
        spinnerSsidList.setAdapter(spinnerAdapter);
        spinnerSsidList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSelectedNetworkSsid(spinnerAdapter.getItem(position));
                getLayoutFloatingButton().setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //nothing
            }
        });

        preselectPrivateNetwork(Utils.getSsid(getContext()), spinnerAdapter, spinnerSsidList);
    }

    private final View.OnClickListener hiddenSsidListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String hiddenSsid = getHiddenSsid();
            if (hiddenSsid == null || hiddenSsid.isEmpty()) {
                Toast.makeText(getContext(), R.string.enter_ssid, Toast.LENGTH_SHORT).show();
                return;
            }

            final boolean isHiddenNetwork = true;
            hideKeyboardAndShowSendCredentialsFragment(isHiddenNetwork, getSerializedHiddenNetwork(hiddenSsid));
        }
    };

    private final View.OnClickListener visibleSsidListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String selectedNetworkSsid = getSelectedNetworkSsid();
            if (selectedNetworkSsid == null) {
                Toast.makeText(getContext(), R.string.select_network, Toast.LENGTH_SHORT).show();
                return;
            }

            final boolean isHiddenNetwork = false;
            hideKeyboardAndShowSendCredentialsFragment(isHiddenNetwork, getSerializedSelectedNetwork());
        }
    };

    private void hideKeyboardAndShowSendCredentialsFragment(boolean isHiddenNetwork, String serializedHiddenNetwork) {
        Utils.hideKeyboard(getActivity());

        showFragment(SendCredentialsViaBluetoothFragment.newInstance(
                deviceId,
                serializedHiddenNetwork,
                isHiddenNetwork,
                getPreSharedKey(),
                serializedCandidateNetworks), false);
    }
}
