package com.sampleapp.net.requester;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.sampleapp.net.RetrofitClient;
import com.sampleapp.net.model.LoginErrorResponse;
import com.sampleapp.net.model.LoginResponse;

import java.lang.reflect.Type;

public abstract class LoginRequester extends BaseRequester<LoginResponse> {
    private Context context;

    public LoginRequester(Context context, String username, String password) {
        super(RetrofitClient.getCloudApi().login(username, password), context);
        this.context = context;
    }

    @Override
    public void onSuccess(LoginResponse response) {
        final int accountId = response.getAccountId();
        if (accountId == 0) {
            final String msg = "Can't get account ID";
            onFailure(msg, 0, "");
        } else {
            onSuccess(String.valueOf(accountId));
        }
    }

    @Override
    public void onFailure(String error, int statusCode, String errorBody) {
        if (errorBody != null && !errorBody.isEmpty()) {
            Toast.makeText(context, getError(errorBody), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
        }
    }

    public abstract void onSuccess(String accountId);

    private String getError(String errorBody) {
        final String tag = "JSON_DESERIALIZER";

        Type type = new TypeToken<LoginErrorResponse>() {
        }.getType();

        LoginErrorResponse errorResponse;
        try {
            errorResponse = new Gson().fromJson(errorBody, type);
        } catch (JsonSyntaxException e) {
            Log.e(tag, e.getMessage());
            return errorBody;
        }

        return errorResponse.getData();
    }
}
