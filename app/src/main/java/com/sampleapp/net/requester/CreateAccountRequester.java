package com.sampleapp.net.requester;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.sampleapp.net.RetrofitClient;
import com.sampleapp.net.model.AccountHolder;
import com.sampleapp.net.model.ErrorHolder;
import com.sampleapp.net.model.ResponseError;

import java.lang.reflect.Type;

import okhttp3.ResponseBody;

public abstract class CreateAccountRequester extends BaseRequester<ResponseBody> {
    private Context context;

    public CreateAccountRequester(Context context, String firstName, String lastName,
                                  String companyName, String email, String password) {

        super(RetrofitClient.getCloudApi().createAccount(
                new AccountHolder(firstName, lastName, companyName, email, password)), context);
        this.context = context;
    }

    @Override
    public void onSuccess(ResponseBody result) {
        Log.i("CreateAccountRequester", "Account has been successfully created");
        onSuccess();
    }

    @Override
    public void onFailure(String error, int statusCode, String errorBody) {
        if (errorBody != null && !errorBody.isEmpty()) {
            Toast.makeText(context, getError(errorBody), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
        }
    }

    public abstract void onSuccess();

    private String getError(String errorBody) {
        final String tag = "JSON_DESERIALIZER";

        Type errorHolderType = new TypeToken<ErrorHolder>() {
        }.getType();
        Type responseErrorType = new TypeToken<ResponseError>() {
        }.getType();

        ErrorHolder errorHolder = null;
        ResponseError responseError = null;
        try {
            errorHolder = new Gson().fromJson(errorBody, errorHolderType);
            responseError = new Gson().fromJson(errorHolder.getData(), responseErrorType);
        } catch (JsonSyntaxException e) {
            Log.e(tag, e.getMessage());
            return errorBody;
        }

        return responseError.getMessage();
    }
}
