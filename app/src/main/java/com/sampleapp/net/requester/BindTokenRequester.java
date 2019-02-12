package com.sampleapp.net.requester;

import android.content.Context;
import android.util.Log;

import com.cirrent.cirrentsdk.internal.logging.LogEvent;
import com.cirrent.cirrentsdk.internal.logging.LogService;
import com.sampleapp.Prefs;
import com.sampleapp.net.RetrofitClient;
import com.sampleapp.net.model.Token;

public abstract class BindTokenRequester extends BaseRequester<Token> {
    private Context context;

    protected BindTokenRequester(Context context, String deviceId, String encodedCredentials) {
        super(RetrofitClient.getCloudApi().getBindToken(deviceId, encodedCredentials), context);
        this.context = context;
    }

    @Override
    public void onSuccess(Token result) {
        final String bindToken = result.getToken();

        Prefs.BIND_TOKEN.setValue(bindToken);

        LogService.getLogService().addLog(context, LogEvent.TOKEN_RECEIVED, "Type=BIND;value=" + bindToken);
        Log.i("BindTokenRequester", "Bind Token received: " + bindToken);

        onSuccess(bindToken);
    }

    @Override
    public void onFailure(String error, int statusCode, String errorBody) {
        LogService.getLogService().addLog(context, LogEvent.TOKEN_ERROR, "Error=" + error);
        Log.e("BindTokenRequester", error);
        onFailure(error, statusCode == 404);
    }

    public abstract void onSuccess(String bindToken);

    public abstract void onFailure(String error, boolean deviceExists);
}
