package com.sampleapp.ui.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cirrent.cirrentsdk.CirrentException;
import com.cirrent.cirrentsdk.CommonErrorCallback;
import com.cirrent.cirrentsdk.net.model.WiFiNetwork;
import com.cirrent.cirrentsdk.service.BluetoothService;
import com.cirrent.cirrentsdk.service.CirrentService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sampleapp.DeviceBinder;
import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.net.model.ManagedDeviceList;
import com.sampleapp.net.requester.ManagedDevicesRequester;

import java.lang.reflect.Type;
import java.util.List;

public class SendCredentialsViaBluetoothFragment extends SendCredentialsBaseFragment {
    private static final String DEVICE_ID = "deviceId";
    private static final String SELECTED_NETWORK = "selectedNetwork";
    private static final String HIDDEN_NETWORK = "hiddenNetwork";
    private static final String PRE_SHARED_KEY = "preSharedKey";
    private static final String CREDENTIALS_ID = "credentialsId";
    private static final String CANDIDATE_NETWORKS = "candidateNetworks";

    private boolean isHiddenNetwork;
    private boolean isFailedToGetNetworkStatus;
    private String deviceId;
    private String preSharedKey;
    private String credentialsId;
    private String serializedCandidateNetworks;
    private WiFiNetwork selectedNetwork;

    public static SendCredentialsViaBluetoothFragment newInstance(String deviceId,
                                                                  String serializedSelectedNetwork,
                                                                  boolean isHiddenNetwork,
                                                                  String preSharedKey,
                                                                  String serializedCandidateNetworks) {
        SendCredentialsViaBluetoothFragment fragment = new SendCredentialsViaBluetoothFragment();
        Bundle args = new Bundle();
        args.putBoolean(HIDDEN_NETWORK, isHiddenNetwork);
        args.putString(DEVICE_ID, deviceId);
        args.putString(SELECTED_NETWORK, serializedSelectedNetwork);
        args.putString(PRE_SHARED_KEY, preSharedKey);
        args.putString(CANDIDATE_NETWORKS, serializedCandidateNetworks);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            deviceId = getCleanedDeviceId(arguments.getString(DEVICE_ID));
            isHiddenNetwork = arguments.getBoolean(HIDDEN_NETWORK);
            preSharedKey = arguments.getString(PRE_SHARED_KEY);
            credentialsId = arguments.getString(CREDENTIALS_ID);
            serializedCandidateNetworks = getArguments().getString(CANDIDATE_NETWORKS);
            deserializeSelectedNetwork(arguments.getString(SELECTED_NETWORK));
        }
    }

    private void deserializeSelectedNetwork(String serializedSelectedNetwork) {
        Type type = new TypeToken<WiFiNetwork>() {
        }.getType();
        selectedNetwork = new Gson().fromJson(serializedSelectedNetwork, type);
    }

    private String getCleanedDeviceId(String dirtyDeviceId) {
        final String underscore = "_";
        if (!dirtyDeviceId.contains(underscore)) {
            return dirtyDeviceId;
        }

        final String[] split = dirtyDeviceId.split(underscore);
        if (split.length > 2) {
            StringBuilder builder = new StringBuilder();
            for (int index = 1; index <= split.length - 1; index++) {
                if (index < split.length - 1) {
                    builder
                            .append(split[index])
                            .append(underscore);
                } else {
                    builder.append(split[index]);
                }
            }
            return builder.toString();
        } else {
            return split[1];
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            view.findViewById(R.id.img_zipkey_logo).setVisibility(View.INVISIBLE);
        }

        putPrivateCredentialsViaBluetooth();

        return view;
    }

    private void putPrivateCredentialsViaBluetooth() {
        final int priority = 200;
        //----- SDK call ------------
        progressView.withText(getString(R.string.bt_sending_creds));
        BluetoothService
                .getBluetoothService()
                .putPrivateCredentialsViaBluetooth(
                        isHiddenNetwork,
                        priority,
                        getActivity(),
                        selectedNetwork,
                        preSharedKey, new BluetoothService.BluetoothCredentialsSenderCallback() {
                            @Override
                            public void onOperationTimeLimitExceeded() {
                                Toast.makeText(getContext(), R.string.time_limit_exceeded, Toast.LENGTH_LONG).show();
                                showFragment(SetupDeviceViaBluetoothFragment.newInstance(deviceId, serializedCandidateNetworks), false);
                            }

                            @Override
                            public void onFailedToGetNetworkStatus() {
                                isFailedToGetNetworkStatus = true;
                                bindDevice(deviceId);
                            }

                            @Override
                            public void onConnectionIsNotEstablished() {
                                Toast.makeText(getContext(), R.string.not_connected, Toast.LENGTH_LONG).show();
                                showFragment(new HomeFragment(), false);
                            }

                            @Override
                            public void onCredentialsSent(String credentialsId) {
                                SendCredentialsViaBluetoothFragment.this.credentialsId = credentialsId;
                                progressView.withText(getString(R.string.bt_checking_status));
                            }

                            @Override
                            public void onConnectedToPrivateNetwork() {
                                bindDevice(deviceId);
                            }

                            @Override
                            public void onNetworkJoiningFailed() {
                                Toast.makeText(getContext(), R.string.bt_joining_failed, Toast.LENGTH_LONG).show();
                                showFragment(SetupDeviceViaBluetoothFragment.newInstance(deviceId, serializedCandidateNetworks), false);
                            }

                            @Override
                            public void onIncorrectPriorityValueUsed() {
                                Toast.makeText(getContext(), R.string.incorrect_priority_value, Toast.LENGTH_LONG).show();
                                showFragment(new HomeFragment(), false);
                            }
                        });
        //---------------------------
    }

    private void bindDevice(final String deviceId) {
        new DeviceBinder(getContext(), deviceId, new DeviceBinder.DeviceBinderCallback() {
            @Override
            public void onBound() {
                getManageToken(deviceId);
            }

            @Override
            public void onFailure(CirrentException e) {
                Toast.makeText(getContext(), R.string.failed_to_bind, Toast.LENGTH_SHORT).show();
                showFragment(new HomeFragment(), false);
            }

            @Override
            public void onFailedRequestBindToken(String errorMessage) {
                Toast.makeText(getContext(), getString(R.string.failed_to_get_bind_token) + errorMessage, Toast.LENGTH_LONG).show();
                showFragment(new HomeFragment(), false);
            }
        })
                .setLocalSetup(true)
                .getBindTokenAndBindDevice(progressView.withText(R.string.binding_device));
    }

    private void getManageToken(final String deviceId) {
        new ManagedDevicesRequester(getContext(), Prefs.ENCODED_CREDENTIALS.getValue()) {
            @Override
            public void onSuccess(String manageToken, List<ManagedDeviceList.ProductCloudDevice> managedDevices) {
                checkDeviceStatus(manageToken, deviceId);
            }
        }.doRequest(progressView.withText(R.string.getting_token));
    }

    private void checkDeviceStatus(final String manageToken, final String deviceId) {
        //----- SDK call ------------
        CirrentService
                .getCirrentService()
                .setProgressView(progressView.withText(R.string.checking_status))
                .pollDeviceJoiningStatus(
                        getContext(),
                        Prefs.APP_ID.getValue(),
                        deviceId,
                        manageToken,
                        selectedNetwork,
                        credentialsId,
                        new CirrentService.JoiningStatusCallback() {
                            @Override
                            public void onTokenExpired() {
                                Toast.makeText(getContext(), R.string.manage_expired, Toast.LENGTH_SHORT).show();
                                showFragment(new HomeFragment(), false);
                            }

                            @Override
                            public void onNetworkJoined() {
                                showFragment(SuccessFragment.newInstance(deviceId, selectedNetwork.getDecodedSsid(), ""), false);
                            }

                            @Override
                            public void onJoining() {
                                progressView.withText(getString(R.string.device_is_joining));
                            }

                            @Override
                            public void onPending() {
                                progressView.withText(getString(R.string.credentials_in_cloud));
                            }

                            @Override
                            public void onCredentialsReceived() {
                                progressView.withText(getString(R.string.creds_downloaded));
                            }

                            @Override
                            public void onNetworkJoiningFailed(boolean isPending, String credentialId) {
                                if (isFailedToGetNetworkStatus) {
                                    Toast.makeText(getContext(), R.string.bt_joining_failed_retr, Toast.LENGTH_LONG).show();
                                    showFragment(new ConnectViaBluetoothLoadingFragment(), false);
                                } else {
                                    Toast.makeText(getContext(), R.string.bt_joining_failed, Toast.LENGTH_LONG).show();
                                    showFragment(new HomeFragment(), false);
                                }
                            }

                        }, new CommonErrorCallback() {
                            @Override
                            public void onFailure(CirrentException e) {
                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                showFragment(new HomeFragment(), false);
                            }
                        });
        //---------------------------
    }
}
