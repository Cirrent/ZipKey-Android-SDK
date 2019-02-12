package com.sampleapp.ui.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.cirrent.cirrentsdk.CirrentException;
import com.cirrent.cirrentsdk.CommonErrorCallback;
import com.cirrent.cirrentsdk.internal.logging.LogEvent;
import com.cirrent.cirrentsdk.internal.logging.LogService;
import com.cirrent.cirrentsdk.net.model.WiFiNetwork;
import com.cirrent.cirrentsdk.service.SoftApService;
import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.SimpleProgressDialog;
import com.sampleapp.Utils;

import java.util.ArrayList;
import java.util.List;

public class SetupDeviceViaSoftApFragment extends SetupDeviceDirectlyFragment {
    private String deviceId;
    private List<WiFiNetwork> deviceCandidateNetworks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        getDeviceInfoViaSoftAp();

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

    private void getDeviceInfoViaSoftAp() {
        //----- SDK call ------------
        SoftApService
                .getSoftApService()
                .setProgressView(new SimpleProgressDialog(getContext(), getString(R.string.getting_device_info_via_soft_ap)))
                .setSoftApPort(80)
                .getDeviceInfoViaSoftAp(getContext(), new SoftApService.SoftApDeviceInfoCallback() {
                    @Override
                    public void onDeviceInfoReceived(String deviceId, List<WiFiNetwork> candidateNetworks) {
                        SetupDeviceViaSoftApFragment.this.deviceId = deviceId;
                        deviceCandidateNetworks = candidateNetworks;
                        if (accountIdsIdentical(deviceId)) {
                            setSpinnerAdapter();
                        } else {
                            showInterruptionDialog();
                        }
                    }

                    private boolean accountIdsIdentical(String deviceId) {
                        boolean accountIdsIdentical = false;
                        final String[] deviceDataArray = deviceId.split("_");

                        String userAccountId = null;
                        String deviceAccountId = null;
                        if (deviceDataArray.length > 0 && Prefs.ACCOUNT_ID.exists()) {
                            userAccountId = Prefs.ACCOUNT_ID.getValue();
                            deviceAccountId = deviceDataArray[0];

                            accountIdsIdentical = userAccountId.equals(deviceAccountId);
                        }

                        if (!accountIdsIdentical) {
                            LogService.getLogService().addLog(
                                    getContext(),
                                    LogEvent.DEBUG,
                                    String.format(
                                            "Onboarding process has been interrupted; userAccountID=%s;deviceAccountID=%s;",
                                            userAccountId,
                                            deviceAccountId
                                    )
                            );
                        }

                        return accountIdsIdentical;
                    }
                }, new CommonErrorCallback() {
                    @Override
                    public void onFailure(CirrentException e) {
                        Toast.makeText(getContext(), getString(R.string.cant_get_info) + " Reason: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        //---------------------------
    }

    private void showInterruptionDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.interrupted)
                .setMessage(R.string.process_interrupted)
                .setCancelable(false)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String softApSsid = Prefs.SOFT_AP_SSID.getValue();
                        final String currentSsid = Utils.getSsid(getContext());
                        if (currentSsid.equals(softApSsid)) {
                            showFragment(DisconnectFromSoftApLoadingFragment.newInstance(softApSsid), false);
                        } else {
                            showFragment(new HomeFragment(), false);
                        }
                    }
                })
                .create()
                .show();
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

        preselectPrivateNetwork(Prefs.PRIVATE_SSID.getValue(), spinnerAdapter, spinnerSsidList);
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

    private void hideKeyboardAndShowSendCredentialsFragment(boolean isHiddenNetwork, String serializedSelectedNetwork) {
        showFragment(SendCredentialsViaSoftApFragment.newInstance(
                deviceId,
                serializedSelectedNetwork,
                isHiddenNetwork,
                getPreSharedKey()
        ), false);
    }
}
