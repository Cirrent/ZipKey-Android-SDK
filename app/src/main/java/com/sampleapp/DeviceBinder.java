package com.sampleapp;


import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.cirrent.cirrentsdk.CirrentException;
import com.cirrent.cirrentsdk.CirrentProgressView;
import com.cirrent.cirrentsdk.CommonErrorCallback;
import com.cirrent.cirrentsdk.service.CirrentService;
import com.sampleapp.net.requester.BindTokenRequester;

public class DeviceBinder {
    private boolean isLocalSetup;
    private int retryCount = 0;
    private Context context;
    private String deviceId;
    private DeviceBinderCallback binderCallback;
    private Handler handler;
    private Runnable runnable;

    public DeviceBinder(Context context, String deviceId, DeviceBinderCallback binderCallback) {
        this.context = context;
        this.deviceId = deviceId;
        this.binderCallback = binderCallback;
    }

    // should be set to "true" if you used Soft AP or BLE onboarding method
    public DeviceBinder setLocalSetup(boolean localSetup) {
        isLocalSetup = localSetup;

        return this;
    }

    /**
     * Requests bind token from product cloud then claims the device.
     */
    public void getBindTokenAndBindDevice(final CirrentProgressView progressView) {
        //----- SDK call ------------
        //It is necessary to check a device(that has gotten its credentials via Soft AP or BLE) binding status before you start a binding process.
        //It may be bound if you previously provided incorrect credentials.
        boolean deviceAlreadyBound = CirrentService.getCirrentService().isDeviceBound();
        //---------------------------

        if (isLocalSetup && deviceAlreadyBound) {
            Log.i("DEVICE_BINDER", "Bound procedure has been skipped.");
            binderCallback.onBound();
        } else {
            final BindTokenRequester bindTokenRequester = new BindTokenRequester(context, deviceId, Prefs.ENCODED_CREDENTIALS.getValue()) {
                @Override
                public void onSuccess(String bindToken) {
                    bindDevice(bindToken, progressView);
                }

                @Override
                public void onFailure(String errorMessage, boolean deviceExists) {
                    if (deviceExists) {
                        bindDevice(Prefs.BIND_TOKEN.getValue(), progressView);
                    } else {
                        binderCallback.onFailedRequestBindToken(errorMessage);
                    }
                }
            };

            bindTokenRequester.doRequest(progressView);
        }
    }

    private void bindDevice(final String bindToken, final CirrentProgressView progressView) {
        final String appId = Prefs.APP_ID.getValue();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                retryCount++;

                //----- SDK call ------------
                CirrentService
                        .getCirrentService()
                        .setProgressView(progressView)
                        .bindDevice(context, isLocalSetup, appId, bindToken, deviceId, new CirrentService.BindDeviceCallback() {
                            @Override
                            public void onDeviceBound() {
                                Prefs.BIND_TOKEN.remove();

                                binderCallback.onBound();
                            }
                        }, new CommonErrorCallback() {
                            @Override
                            public void onFailure(CirrentException e) {
                                if (retryCount < 4) {
                                    handler.postDelayed(runnable, 6000);
                                } else {
                                    binderCallback.onFailure(e);
                                }
                            }
                        });
                //---------------------------
            }
        };

        handler.post(runnable);
    }

    public interface DeviceBinderCallback {
        void onBound();

        void onFailure(CirrentException e);

        void onFailedRequestBindToken(String errorMessage);
    }
}
