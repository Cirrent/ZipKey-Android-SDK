package com.sampleapp.ui.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cirrent.cirrentsdk.CirrentException;
import com.cirrent.cirrentsdk.CommonErrorCallback;
import com.cirrent.cirrentsdk.internal.net.model.DeviceDto;
import com.cirrent.cirrentsdk.net.model.Device;
import com.cirrent.cirrentsdk.service.CirrentService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.net.model.ManagedDeviceList;
import com.sampleapp.net.requester.ManagedDevicesRequester;

import java.lang.reflect.Type;
import java.util.List;

public class SendProviderCredentialsFragment extends SendCredentialsBaseFragment {
    private static final String DEVICE = "device";

    private Device selectedDevice;
    private String serializedSelectedDevice;
    private DeviceDto.ProviderKnownNetwork providerKnownNetwork;

    public static SendProviderCredentialsFragment newInstance(String serializedSelectedDevice) {
        SendProviderCredentialsFragment fragment = new SendProviderCredentialsFragment();
        Bundle args = new Bundle();
        args.putString(DEVICE, serializedSelectedDevice);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            serializedSelectedDevice = getArguments().getString(DEVICE);
            Type type = new TypeToken<Device>() {
            }.getType();
            selectedDevice = new Gson().fromJson(serializedSelectedDevice, type);
            providerKnownNetwork = selectedDevice.getProviderKnownNetwork();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        requestManageTokenAndSendProviderCredentials();
        return view;
    }

    private void requestManageTokenAndSendProviderCredentials() {
        new ManagedDevicesRequester(getContext(), Prefs.ENCODED_CREDENTIALS.getValue()) {
            @Override
            public void onSuccess(final String manageToken, final List<ManagedDeviceList.ProductCloudDevice> managedDevices) {
                if (manageToken == null || manageToken.isEmpty()) {
                    Toast.makeText(getContext(), R.string.manage_token_has_not_received, Toast.LENGTH_SHORT).show();
                    showFragment(SetupDeviceAutomaticallyFragment.newInstance(serializedSelectedDevice), false);
                } else {
                    sendProviderCredentials();
                }
            }

        }.doRequest(progressView.withText(R.string.getting_manage_token));
    }

    private void sendProviderCredentials() {
        final String appId = Prefs.APP_ID.getValue();
        final String manageToken = Prefs.MANAGE_TOKEN.getValue();

        //----- SDK call ------------
        CirrentService
                .getCirrentService()
                .setProgressView(progressView.withText(R.string.sending_credentials))
                .putProviderCredentials(
                        getContext(),
                        selectedDevice.getDeviceId(),
                        providerKnownNetwork,
                        appId,
                        manageToken, new CirrentService.ProviderCredentialsSenderCallback() {

                            @Override
                            public void onTokenExpired() {
                                handleError(null);
                            }

                            @Override
                            public void onCredentialsSent(String credentialsId) {
                                pollDeviceStatus(credentialsId, appId, manageToken);
                            }

                        }, new CommonErrorCallback() {
                            @Override
                            public void onFailure(CirrentException e) {
                                handleError(e);
                            }
                        });
        //---------------------------
    }

    public void pollDeviceStatus(String credentialsId, String appId, String manageToken) {
        final String networkSsid = providerKnownNetwork.getSsid();
        //----- SDK call ------------
        CirrentService
                .getCirrentService()
                .setDeviceStatusTimings(10, 6)
                .pollDeviceJoiningStatus(
                        getContext(),
                        appId,
                        selectedDevice.getDeviceId(),
                        manageToken,
                        providerKnownNetwork,
                        credentialsId,
                        new CirrentService.JoiningStatusCallback() {
                    @Override
                    public void onTokenExpired() {
                        handleError(null);
                    }

                    @Override
                    public void onNetworkJoined() {
                        showFragment(SuccessFragment.newInstance(selectedDevice.getDeviceId(), networkSsid, serializedSelectedDevice), false);
                    }

                    @Override
                    public void onJoining() {
                        progressView.withText("Device is connecting to " + networkSsid + " Network…");
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
                            Toast.makeText(getContext(), R.string.joining_failed_try_manual, Toast.LENGTH_SHORT).show();
                            showFragment(SetupDeviceAutomaticallyFragment.newInstance(serializedSelectedDevice), false);
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
            Toast.makeText(getContext(), R.string.manage_expired, Toast.LENGTH_SHORT).show();
        }

        showFragment(SetupDeviceAutomaticallyFragment.newInstance(serializedSelectedDevice), false);
    }
}