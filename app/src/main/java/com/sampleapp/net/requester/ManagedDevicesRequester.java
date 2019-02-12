package com.sampleapp.net.requester;

import android.content.Context;
import android.util.Log;

import com.cirrent.cirrentsdk.internal.logging.LogEvent;
import com.cirrent.cirrentsdk.internal.logging.LogService;
import com.sampleapp.Prefs;
import com.sampleapp.net.RetrofitClient;
import com.sampleapp.net.model.ManagedDeviceList;

import java.util.Collections;
import java.util.List;

public abstract class ManagedDevicesRequester extends BaseRequester<ManagedDeviceList> {
    private Context context;

    protected ManagedDevicesRequester(Context context, String encodedCredentials) {
        super(RetrofitClient.getCloudApi().getProductCloudDevices(encodedCredentials), context);

        this.context = context;
    }

    @Override
    public void onSuccess(ManagedDeviceList result) {
        final String manageToken = result.getManageToken();
        final List<ManagedDeviceList.ProductCloudDevice> devices = result.getDevices();

        LogService.getLogService().addLog(context, LogEvent.TOKEN_RECEIVED, "Type=MANAGE;value=" + manageToken);
        Log.i("ManagedDevicesRequester", "Manage Token received: " + manageToken);

        Prefs.MANAGE_TOKEN.setValue(manageToken);

        onSuccess(manageToken, devices);
    }

    @Override
    public void onFailure(String error, int statusCode, String errorBody) {
        switch (statusCode) {
            case 0:
                failedToReachCloud(error);
                break;
            case 404:
                onSuccess("", Collections.EMPTY_LIST); //No matching devices for ownerId
                break;
            default:
                LogService.getLogService().addLog(context, LogEvent.TOKEN_ERROR, "Error=" + error);
                Log.e("ManagedDevicesRequester", error);
                break;
        }
    }

    public void failedToReachCloud(String error) {
        Log.e("ManagedDevicesRequester", error);
    }

    public abstract void onSuccess(String manageToken, List<ManagedDeviceList.ProductCloudDevice> managedDevices);
}
