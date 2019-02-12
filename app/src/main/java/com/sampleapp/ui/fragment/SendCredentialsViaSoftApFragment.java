package com.sampleapp.ui.fragment;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cirrent.cirrentsdk.CirrentException;
import com.cirrent.cirrentsdk.CommonErrorCallback;
import com.cirrent.cirrentsdk.net.model.WiFiNetwork;
import com.cirrent.cirrentsdk.service.CirrentService;
import com.cirrent.cirrentsdk.service.SoftApService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sampleapp.DeviceBinder;
import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.SoftApBackupHolder;
import com.sampleapp.net.model.ManagedDeviceList;
import com.sampleapp.net.requester.ManagedDevicesRequester;

import java.lang.reflect.Type;
import java.util.List;

public class SendCredentialsViaSoftApFragment extends SendCredentialsBaseFragment {
    private static final String DEVICE_ID = "deviceId";
    private static final String SELECTED_NETWORK = "selectedNetwork";
    private static final String HIDDEN_NETWORK = "hiddenNetwork";
    private static final String PRE_SHARED_KEY = "preSharedKey";
    private static final String CREDENTIALS_ID = "credentialsId";
    private static final String BACKUP = "backup";

    private boolean isHiddenNetwork;
    private String deviceId;
    private String preSharedKey;
    private String credentialsId;
    private WiFiNetwork selectedNetwork;

    public static SendCredentialsViaSoftApFragment newInstance(String deviceId,
                                                               String serializedSelectedNetwork,
                                                               boolean isHiddenNetwork,
                                                               String preSharedKey) {
        SendCredentialsViaSoftApFragment fragment = new SendCredentialsViaSoftApFragment();
        Bundle args = new Bundle();
        args.putBoolean(HIDDEN_NETWORK, isHiddenNetwork);
        args.putString(DEVICE_ID, deviceId);
        args.putString(SELECTED_NETWORK, serializedSelectedNetwork);
        args.putString(PRE_SHARED_KEY, preSharedKey);
        fragment.setArguments(args);

        return fragment;
    }

    public static SendCredentialsViaSoftApFragment newInstance(String serializedBackup) {
        SendCredentialsViaSoftApFragment fragment = new SendCredentialsViaSoftApFragment();
        Bundle args = new Bundle();
        args.putString(BACKUP, serializedBackup);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            final String serializedBackup = arguments.getString(BACKUP);
            if (serializedBackup != null) {
                final SoftApBackupHolder softApBackupHolder = deserializeBackup(serializedBackup);
                deviceId = softApBackupHolder.getDeviceId();
                credentialsId = softApBackupHolder.getCredentialsId();
                selectedNetwork = softApBackupHolder.getSelectedNetwork();
            } else {
                deviceId = getCleanedDeviceId(arguments.getString(DEVICE_ID, ""));
                isHiddenNetwork = arguments.getBoolean(HIDDEN_NETWORK);
                preSharedKey = arguments.getString(PRE_SHARED_KEY);
                credentialsId = arguments.getString(CREDENTIALS_ID);
                deserializeSelectedNetwork(arguments.getString(SELECTED_NETWORK));
            }
        }
    }

    private void deserializeSelectedNetwork(String serializedSelectedNetwork) {
        Type type = new TypeToken<WiFiNetwork>() {
        }.getType();
        selectedNetwork = new Gson().fromJson(serializedSelectedNetwork, type);
    }

    private SoftApBackupHolder deserializeBackup(String serializedBackup) {
        Type collectionType = new TypeToken<SoftApBackupHolder>() {
        }.getType();
        return new Gson().fromJson(serializedBackup, collectionType);
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

        if (Prefs.SOFT_AP_DEVICE_SETUP_DATA.exists()) {
            finishOnboardingProcess(view);
        } else {
            putPrivateCredentialsViaSoftAp();
        }

        return view;
    }

    private void finishOnboardingProcess(View view) {
        if (view != null) {
            view.findViewById(R.id.text_finishing_onboard).setVisibility(View.VISIBLE);
        }

        bindDevice(deviceId);
    }

    private void putPrivateCredentialsViaSoftAp() {
        final int priority = 200;
        final String softApSsid = Prefs.SOFT_AP_SSID.getValue();

        //----- SDK call ------------
        SoftApService
                .getSoftApService()
                .setProgressView(progressView.withText(R.string.sending_via_soft_ap))
                .setSoftApDeviceStatusTimings(10, 6)
                .setSoftApDisconnectTimings(7, 5)
                .setSoftApPort(80)
                .putPrivateCredentialsViaSoftAp(
                        isHiddenNetwork,
                        Prefs.WIFI_NETWORK_ID.getValue(),
                        priority,
                        getContext(),
                        softApSsid,
                        selectedNetwork,
                        preSharedKey, new SoftApService.SoftApCredentialsSenderCallback() {
                            @Override
                            public void onCredentialsSent(String credentialsId) {
                                SendCredentialsViaSoftApFragment.this.credentialsId = credentialsId;
                                saveDeviceSetupData();
                            }

                            @Override
                            public void onReturnedToNetworkWithInternet() {
                                bindDevice(deviceId);
                            }

                            @Override
                            public void onFailedToReturnToNetworkWithInternet() {
                                Toast.makeText(getContext(), R.string.failed_to_return_to_network, Toast.LENGTH_LONG).show();
                                showFragment(new HomeFragment(), false);
                            }

                            @Override
                            public void onNetworkJoiningFailed() {
                                removeDeviceSetupData();

                                Toast.makeText(getContext(), R.string.joining_failed_try_again, Toast.LENGTH_SHORT).show();
                                showFragment(new SetupDeviceViaSoftApFragment(), false);
                            }

                            @Override
                            public void onIncorrectPriorityValueUsed() {
                                Toast.makeText(getContext(), R.string.incorrect_priority_value, Toast.LENGTH_LONG).show();
                                showFragment(new HomeFragment(), false);
                            }
                        }, new CommonErrorCallback() {
                            @Override
                            public void onFailure(CirrentException e) {
                                Log.e("CRED_SENDER", e.getMessage() + ". Code:" + e.getErrorCode());
                                Toast.makeText(getContext(), R.string.joining_failed_try_again, Toast.LENGTH_SHORT).show();
                                showFragment(new HomeFragment(), false);
                            }
                        });
        //---------------------------
    }

    /**
     * It is important to save device setup data. These data required to continue onboarding process
     * that has been interrupted.
     * It can be helpful in a situation where the device cannot be managed, because it may be
     * no longer discoverable (as it's on a private network) but isn't associated with any account.
     * This could happen if the app provisions the device over softAP and then either crashes or
     * the user kills the app before it sends the bind to the clouds.
     */
    private void saveDeviceSetupData() {
        Gson gson = new Gson();
        String serializedSetupData = gson.toJson(new SoftApBackupHolder(deviceId, credentialsId, selectedNetwork));
        Prefs.SOFT_AP_DEVICE_SETUP_DATA.setValue(serializedSetupData);
    }

    private void bindDevice(final String deviceId) {
        new DeviceBinder(getContext(), deviceId, new DeviceBinder.DeviceBinderCallback() {
            @Override
            public void onBound() {
                // if device was successfully bound you should remove saved data
                removeDeviceSetupData();
                getManageToken(deviceId);
            }

            @Override
            public void onFailure(CirrentException e) {
                Toast.makeText(getContext(), R.string.failed_to_bind, Toast.LENGTH_SHORT).show();
                showFragment(new HomeFragment(), false);
            }

            @Override
            public void onFailedRequestBindToken(String errorMessage) {
                Toast.makeText(getContext(), "Failed to get the Bind token. " + errorMessage, Toast.LENGTH_LONG).show();
                showFragment(new HomeFragment(), false);
            }
        })
                .setLocalSetup(true)
                .getBindTokenAndBindDevice(progressView.withText(R.string.binding_device));
    }

    private void removeDeviceSetupData() {
        Prefs.SOFT_AP_DEVICE_SETUP_DATA.remove();
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
                .setDeviceStatusTimings(10, 6)
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
                                Toast.makeText(getContext(), "Joining network failed. Retrying.", Toast.LENGTH_LONG).show();
                                showFragment(ConnectViaSoftApLoadingFragment.newInstance(Prefs.SOFT_AP_SSID.getValue()), false);
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
