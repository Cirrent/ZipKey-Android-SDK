package com.sampleapp.ui.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cirrent.cirrentsdk.CirrentException;
import com.cirrent.cirrentsdk.CommonErrorCallback;
import com.cirrent.cirrentsdk.net.model.WiFiNetwork;
import com.cirrent.cirrentsdk.service.CirrentService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sampleapp.Prefs;
import com.sampleapp.R;

import java.lang.reflect.Type;

public class SendPrivateCredentialsFragment extends SendCredentialsBaseFragment {
    private static final String DEVICE = "device";
    private static final String DEVICE_ID = "deviceId";
    private static final String SELECTED_NETWORK = "serializedSelectedNetwork";
    private static final String HIDDEN_NETWORK = "hiddenNetwork";
    private static final String PRE_SHARED_KEY = "preSharedKey";

    private boolean isHiddenNetwork;
    private String serializedSelectedDevice;
    private String deviceId;
    private String preSharedKey;
    private WiFiNetwork selectedNetwork;

    public static SendPrivateCredentialsFragment newInstance(boolean isHiddenNetwork,
                                                             String deviceId,
                                                             String serializedSelectedDevice,
                                                             String serializedSelectedNetwork,
                                                             String preSharedKey) {
        SendPrivateCredentialsFragment fragment = new SendPrivateCredentialsFragment();
        Bundle args = new Bundle();
        args.putBoolean(HIDDEN_NETWORK, isHiddenNetwork);
        args.putString(DEVICE_ID, deviceId);
        args.putString(DEVICE, serializedSelectedDevice);
        args.putString(SELECTED_NETWORK, serializedSelectedNetwork);
        args.putString(PRE_SHARED_KEY, preSharedKey);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            serializedSelectedDevice = getArguments().getString(DEVICE);
            deviceId = arguments.getString(DEVICE_ID);
            isHiddenNetwork = arguments.getBoolean(HIDDEN_NETWORK);
            preSharedKey = arguments.getString(PRE_SHARED_KEY);
            deserializeSelectedNetwork(arguments.getString(SELECTED_NETWORK));
        }
    }

    private void deserializeSelectedNetwork(String serializedSelectedNetwork) {
        Type type = new TypeToken<WiFiNetwork>() {
        }.getType();
        selectedNetwork = new Gson().fromJson(serializedSelectedNetwork, type);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        sendPrivateCredentials();
        return view;
    }

    private void sendPrivateCredentials() {
        final int priority = 200;
        final String manageToken = Prefs.MANAGE_TOKEN.getValue();
        final String appId = Prefs.APP_ID.getValue();

        //----- SDK call ------------
        CirrentService
                .getCirrentService()
                .setProgressView(progressView.withText(R.string.sending_credentials))
                .putPrivateCredentials(
                        isHiddenNetwork,
                        priority,
                        getContext(),
                        appId,
                        deviceId,
                        manageToken,
                        selectedNetwork,
                        preSharedKey, new CirrentService.PrivateCredentialsSenderCallback() {
                            @Override
                            public void onTokenExpired() {
                                handleError(null);
                            }

                            @Override
                            public void onCredentialsSent(String credentialId) {
                                if (serializedSelectedDevice.isEmpty()) {
                                    Toast.makeText(getContext(), R.string.credentials_sent, Toast.LENGTH_SHORT).show();
                                    showFragment(KnownNetworksFragment.newInstance(deviceId), false);
                                } else {
                                    pollDeviceStatus(credentialId, appId, manageToken);
                                }
                            }

                            @Override
                            public void onIncorrectPriorityValueUsed() {
                                handleError(new CirrentException(getString(R.string.incorrect_priority_value)));
                            }
                        }, new CommonErrorCallback() {
                            @Override
                            public void onFailure(CirrentException e) {
                                handleError(e);
                            }
                        });
        //---------------------------
    }

    private void pollDeviceStatus(String credentialId, String appId, String manageToken) {
        //----- SDK call ------------
        CirrentService
                .getCirrentService()
                .setDeviceStatusTimings(10, 6)
                .pollDeviceJoiningStatus(
                        getContext(),
                        appId,
                        deviceId,
                        manageToken,
                        selectedNetwork,
                        credentialId,
                        new CirrentService.JoiningStatusCallback() {
                    @Override
                    public void onTokenExpired() {
                        handleError(null);
                    }

                    @Override
                    public void onNetworkJoined() {
                        showFragment(SuccessFragment.newInstance(
                                deviceId,
                                selectedNetwork.getDecodedSsid(),
                                serializedSelectedDevice
                        ), false);
                    }

                    @Override
                    public void onJoining() {
                        progressView.withText(
                                "Device is connecting to " + selectedNetwork.getDecodedSsid() + " Network…"
                        );
                    }

                    @Override
                    public void onPending() {
                        progressView.withText("Credentials are in the cloud");
                    }

                    @Override
                    public void onCredentialsReceived() {
                        progressView.withText("Credentials have been downloaded to the device");
                    }

                    @Override
                    public void onNetworkJoiningFailed(boolean isPending, String credentialId) {
                        if (isPending) {
                            Toast.makeText(getContext(), R.string.joining_failed_try_soft_ap, Toast.LENGTH_LONG).show();
                            showFragment(new HomeFragment(), false);
                        } else {
                            Toast.makeText(getContext(), R.string.joining_failed_try_again, Toast.LENGTH_SHORT).show();
                            showFragment(SetupDeviceManuallyFragment.newInstance(serializedSelectedDevice), false);
                        }
                    }
                }, new CommonErrorCallback() {
                    @Override
                    public void onFailure(CirrentException e) {
                        handleError(e);
                    }
                });
        //---------------------------
    }

    private void handleError(CirrentException exception) {
        if (exception != null) {
            Toast.makeText(getContext(), getString(R.string.joining_failed_try_again) + "(" + exception.getMessage() + ")", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), getString(R.string.manage_expired), Toast.LENGTH_SHORT).show();
        }

        if (serializedSelectedDevice.isEmpty()) {
            showFragment(KnownNetworksFragment.newInstance(deviceId), false);
        } else {
            showFragment(SetupDeviceManuallyFragment.newInstance(serializedSelectedDevice), false);
        }
    }
}
