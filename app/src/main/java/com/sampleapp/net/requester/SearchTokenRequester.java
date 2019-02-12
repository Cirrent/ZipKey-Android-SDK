package com.sampleapp.net.requester;

import android.content.Context;
import android.util.Log;

import com.cirrent.cirrentsdk.internal.logging.LogEvent;
import com.cirrent.cirrentsdk.internal.logging.LogService;
import com.sampleapp.Prefs;
import com.sampleapp.net.RetrofitClient;
import com.sampleapp.net.model.Token;

public abstract class SearchTokenRequester extends BaseRequester<Token> {
    private Context context;

    protected SearchTokenRequester(Context context, String encodedCredentials) {
        super(RetrofitClient.getCloudApi().getSearchToken(encodedCredentials), context);
        this.context = context;
    }

    @Override
    public void onSuccess(Token result) {
        final String searchToken = result.getToken();
        LogService.getLogService().addLog(context, LogEvent.TOKEN_RECEIVED, "Type=SEARCH;value=" + searchToken);
        Log.i("SearchTokenRequester", "Search Token received: " + searchToken);
        Prefs.SEARCH_TOKEN.setValue(searchToken);
        onSuccess(searchToken);
    }

    @Override
    public void onFailure(String error, int statusCode, String errorBody) {
        LogService.getLogService().addLog(context, LogEvent.TOKEN_ERROR, "Error=" + error);
        Log.e("SearchTokenRequester", error);
        onFailure(error);
    }

    public abstract void onSuccess(String searchToken);

    public abstract void onFailure(String errorMessage);
}
