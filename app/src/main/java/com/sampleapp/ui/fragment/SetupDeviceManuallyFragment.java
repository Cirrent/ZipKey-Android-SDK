package com.sampleapp.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.cirrent.cirrentsdk.CirrentException;
import com.cirrent.cirrentsdk.CommonErrorCallback;
import com.cirrent.cirrentsdk.net.model.Device;
import com.cirrent.cirrentsdk.net.model.DeviceKnownNetwork;
import com.cirrent.cirrentsdk.net.model.WiFiNetwork;
import com.cirrent.cirrentsdk.service.CirrentService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.SimpleProgressDialog;
import com.sampleapp.Utils;
import com.sampleapp.net.model.ManagedDeviceList;
import com.sampleapp.net.requester.ManagedDevicesRequester;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SetupDeviceManuallyFragment extends SetupDeviceBaseFragment {
    private static final String DEVICE = "device";
    private static final String DEVICE_ID_PARAM_NAME = "deviceId";
    private static final String KNOWN_NETWORKS = "knownNetworks";

    private Device selectedDevice;
    private String serializedSelectedDevice;
    private String deviceId;
    private List<DeviceKnownNetwork> knownNetworks;
    private String selectedNetworkSsid;
    private List<WiFiNetwork> deviceCandidateNetworks;
    private Map<String, WiFiNetwork> ssidMap;

    /**
     * Creates a new instance for case when you want to add a network for the new device.
     */
    public static SetupDeviceManuallyFragment newInstance(String serializedSelectedDevice) {
        SetupDeviceManuallyFragment fragment = new SetupDeviceManuallyFragment();
        Bundle args = new Bundle();
        args.putString(DEVICE, serializedSelectedDevice);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Creates a new instance for case when you want to add a new network for the managed device.
     */
    public static SetupDeviceManuallyFragment newInstance(String deviceId, String serializedKnownNetworks) {
        SetupDeviceManuallyFragment fragment = new SetupDeviceManuallyFragment();
        Bundle args = new Bundle();
        args.putString(DEVICE_ID_PARAM_NAME, deviceId);
        args.putString(KNOWN_NETWORKS, serializedKnownNetworks);
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
                deserializeSelectedDevice();
                deviceId = selectedDevice.getDeviceId();
            } else {
                setHasOptionsMenu(true);
                deviceId = arguments.getString(DEVICE_ID_PARAM_NAME);
                String serializedKnownNetworks = getArguments().getString(KNOWN_NETWORKS);
                if (serializedKnownNetworks != null) {
                    deserializeKnownNetworks(serializedKnownNetworks);
                }
            }
        }
    }

    private void deserializeSelectedDevice() {
        Type type = new TypeToken<Device>() {
        }.getType();
        selectedDevice = new Gson().fromJson(serializedSelectedDevice, type);
    }

    private void deserializeKnownNetworks(String serializedKnownNetworks) {
        Type collectionType = new TypeToken<List<DeviceKnownNetwork>>() {
        }.getType();
        knownNetworks = new Gson().fromJson(serializedKnownNetworks, collectionType);
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
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        requestManageTokenAndGetVisibleDeviceNetworks();

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


    private void requestManageTokenAndGetVisibleDeviceNetworks() {
        new ManagedDevicesRequester(getContext(), Prefs.ENCODED_CREDENTIALS.getValue()) {
            @Override
            public void onSuccess(final String manageToken, final List<ManagedDeviceList.ProductCloudDevice> managedDevices) {
                if (manageToken == null || manageToken.isEmpty()) {
                    Toast.makeText(getContext(), R.string.manage_token_has_not_received, Toast.LENGTH_SHORT).show();
                } else {
                    getVisibleDeviceNetworks(deviceId);
                }
            }
        }.doRequest(new SimpleProgressDialog(getContext(), getContext().getString(R.string.getting_manage_token)));
    }

    private void getVisibleDeviceNetworks(String deviceId) {
        //----- SDK call ------------
        CirrentService
                .getCirrentService()
                .setProgressView(new SimpleProgressDialog(getContext(), getString(R.string.getting_networks)))
                .getCandidateNetworks(getContext(), deviceId, Prefs.MANAGE_TOKEN.getValue(), new CirrentService.DeviceCandidateNetworksCallback() {
                    @Override
                    public void onDeviceCandidateNetworksReceived(List<WiFiNetwork> candidateNetworks) {
                        deviceCandidateNetworks = candidateNetworks;
                        setSpinnerAdapter();
                    }

                    @Override
                    public void onTokenExpired() {
                        Toast.makeText(getContext(), R.string.manage_expired, Toast.LENGTH_SHORT).show();
                    }
                }, new CommonErrorCallback() {
                    @Override
                    public void onFailure(CirrentException e) {
                        Toast.makeText(getContext(), getString(R.string.cant_get_candidate_networks) + " Reason: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        //---------------------------
    }

    private void setSpinnerAdapter() {
        final List<String> ssidList = new ArrayList<>(getSsidNames());
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getContext(), R.layout.item_spinner, ssidList);
        final Spinner spinner = getSsidListSpinner();
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedNetworkSsid = ssidList.get(position);
                getLayoutFloatingButton().setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @NonNull
    private Set<String> getSsidNames() {
        ssidMap = new HashMap<>();
        for (WiFiNetwork candidateNetwork : deviceCandidateNetworks) {
            final String candidateSsid = candidateNetwork.getSsid();
            final String candidateHexSsid = candidateNetwork.getHexSsid();
            if (candidateSsid != null && !candidateSsid.isEmpty()) {
                addIfNotKnown(candidateSsid, candidateNetwork);
            } else if (candidateHexSsid != null && !candidateHexSsid.isEmpty()) {
                addIfNotKnown(candidateNetwork.getDecodedSsid(), candidateNetwork);
            }
        }
        return ssidMap.keySet();
    }

    private void addIfNotKnown(String candidateSsid, WiFiNetwork candidateNetwork) {
        boolean isSsidDuplicated = false;
        if (knownNetworks != null) {
            for (DeviceKnownNetwork knownNetwork : knownNetworks) {
                final String knownSsid = knownNetwork.getSsid();
                final String knownDecodedHexSsid = knownNetwork.getDecodedSsid();
                if (candidateSsid.equals(knownDecodedHexSsid) || candidateSsid.equals(knownSsid)) {
                    isSsidDuplicated = true;
                    break;
                }
            }
        }

        if (!isSsidDuplicated) {
            ssidMap.put(candidateSsid, candidateNetwork);
        }
    }

    private final View.OnClickListener hiddenSsidListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String hiddenSsid = getHiddenSsid();
            if (hiddenSsid == null || hiddenSsid.isEmpty()) {
                Toast.makeText(getContext(), R.string.enter_ssid, Toast.LENGTH_SHORT).show();
                return;
            }

            Utils.hideKeyboard(getActivity());

            final String serializedHiddenNetwork = getSerializedHiddenNetwork(hiddenSsid);
            final boolean isHiddenNetwork = true;
                showFragment(SendPrivateCredentialsFragment.newInstance(
                        isHiddenNetwork,
                        deviceId,
                        serializedSelectedDevice == null ? "" : serializedSelectedDevice,
                        serializedHiddenNetwork,
                        getPreSharedKey()), false);
        }

        private String getSerializedHiddenNetwork(String hiddenSsid) {
            final WiFiNetwork hiddenNetwork = new WiFiNetwork();
            hiddenNetwork.setFlags(getSelectedSecurityType());
            hiddenNetwork.setSsid(hiddenSsid);
            return new Gson().toJson(hiddenNetwork);
        }
    };

    private final View.OnClickListener visibleSsidListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (selectedNetworkSsid == null) {
                Toast.makeText(getContext(), R.string.select_network, Toast.LENGTH_SHORT).show();
                return;
            }

            Utils.hideKeyboard(getActivity());

            final boolean isHiddenNetwork = false;
            final WiFiNetwork selectedNetwork = ssidMap.get(selectedNetworkSsid);
            final String serializedSelectedNetwork = new Gson().toJson(selectedNetwork);
            showFragment(SendPrivateCredentialsFragment.newInstance(
                        isHiddenNetwork,
                        deviceId,
                        serializedSelectedDevice == null ? "" : serializedSelectedDevice,
                    serializedSelectedNetwork,
                        getPreSharedKey()), false);
        }
    };
}
