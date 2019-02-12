package com.sampleapp.net;

import com.sampleapp.net.model.AccountHolder;
import com.sampleapp.net.model.LoginResponse;
import com.sampleapp.net.model.ManagedDeviceList;
import com.sampleapp.net.model.Token;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProductCloudApi {
    @POST("create-account")
    Call<ResponseBody> createAccount(@Body AccountHolder accountHolder);

    @POST("api/login")
    Call<LoginResponse> login(@Query("username") String username, @Query("password") String password);

    @GET("cloud/token/search")
    Call<Token> getSearchToken(@Header("Authorization") String authorization);

    @GET("cloud/devices")
    Call<ManagedDeviceList> getProductCloudDevices(@Header("Authorization") String authorization);

    @POST("cloud/bind/{deviceId}")
    Call<Token> getBindToken(
            @Path("deviceId") String deviceId,
            @Header("Authorization") String authorization
    );

    @DELETE("cloud/reset/{deviceId}")
    Call<ResponseBody> resetDevice(
            @Path("deviceId") String deviceId,
            @Header("Authorization") String authorization
    );
}
