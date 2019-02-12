package com.sampleapp.ui.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.cirrent.cirrentsdk.internal.logging.LogEvent;
import com.cirrent.cirrentsdk.internal.logging.LogService;
import com.cirrent.cirrentsdk.net.model.WiFiNetwork;
import com.cirrent.cirrentsdk.service.BluetoothService;
import com.google.gson.Gson;
import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.ui.activity.HomeActivity;

import java.util.List;

public class ConnectViaBluetoothLoadingFragment extends DirectConnectionLoadingFragment {
    private TextView textStatus;
    private HomeActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        activity = (HomeActivity) getActivity();
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setStatusText(view);
        connectToDeviceViaBluetooth();

        return view;
    }

    private void setStatusText(View view) {
        textStatus = (TextView) view.findViewById(R.id.text_status);
        textStatus.setText(R.string.searching_device);
    }

    private void connectToDeviceViaBluetooth() {
        String wcmBlePrefix = Prefs.WCM_BLE_PREFIX.getValue();

        //----- SDK call ------------
        BluetoothService
                .getBluetoothService()
                .connectToDeviceViaBluetooth(wcmBlePrefix, getActivity(), new BluetoothService.BluetoothDeviceConnectionCallback() {
                    @Override
                    public void onBleNotSupported() {
                        Toast.makeText(getContext(), R.string.ble_not_supported, Toast.LENGTH_LONG).show();
                        showFragment(new HomeFragment(), false);
                    }

                    @Override
                    public void onBluetoothDisabled() {
                        Toast.makeText(getContext(), R.string.bt_turned_off, Toast.LENGTH_LONG).show();
                        showFragment(new HomeFragment(), false);
                    }

                    @Override
                    public void onFailedToFindDevice() {
                        Toast.makeText(getContext(), R.string.failed_to_find_device_via_bluetooth, Toast.LENGTH_LONG).show();
                        showFragment(new HomeFragment(), false);
                    }

                    @Override
                    public void onDeviceConnectedSuccessfully() {
                        textStatus.setText(R.string.connected_getting_info);
                        getDeviceInfoViaBluetooth();
                    }
                });
        //---------------------------
    }

    private void getDeviceInfoViaBluetooth() {
        //----- SDK call ------------
        BluetoothService
                .getBluetoothService()
                .getDeviceInfoViaBluetooth(getActivity(), new BluetoothService.BluetoothDeviceInfoCallback() {
                    @Override
                    public void onOperationTimeLimitExceeded() {
                        Toast.makeText(activity, R.string.time_limit_exceeded, Toast.LENGTH_LONG).show();
                        activity.showFragment(new HomeFragment(), false);
                    }

                    @Override
                    public void onConnectionIsNotEstablished() {
                        Toast.makeText(getContext(), R.string.not_connected, Toast.LENGTH_LONG).show();
                        showFragment(new HomeFragment(), false);
                    }

                    @Override
                    public void onInfoReceived(String deviceId, List<WiFiNetwork> candidateNetworks) {
                        if (accountIdsIdentical(deviceId)) {
                            String serializedCandidateNetworks = new Gson().toJson(candidateNetworks);
                            showFragment(SetupDeviceViaBluetoothFragment.newInstance(deviceId, serializedCandidateNetworks), false);
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
                        showFragment(new HomeFragment(), false);
                    }
                })
                .create()
                .show();
    }
}
