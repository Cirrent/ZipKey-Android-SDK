package com.sampleapp.net.requester;

import android.content.Context;
import android.util.Log;

import com.sampleapp.net.RetrofitClient;

import okhttp3.ResponseBody;

public abstract class ResetManagedDeviceRequester extends BaseRequester<ResponseBody> {

    private static final String TAG = "Reset(Product)";

    protected ResetManagedDeviceRequester(Context context, String deviceId, String encodedCredentials) {
        super(RetrofitClient.getCloudApi().resetDevice(deviceId, encodedCredentials), context);
    }

    @Override
    public void onSuccess(ResponseBody result) {
        Log.i(TAG, "Device has been successfully reset (Product Cloud)");
        onSuccess();
    }

    @Override
    public void onFailure(String error, int statusCode, String errorBody) {
        Log.e(TAG, error);
    }

    public abstract void onSuccess();

}
