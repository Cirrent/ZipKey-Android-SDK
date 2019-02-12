package com.sampleapp.net;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    public static final String TOKEN_BASE_URL = "https://go.cirrent.com/";
    private static ProductCloudApi CLOUD_API;

    public static ProductCloudApi getCloudApi() {
        if (CLOUD_API == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(interceptor).build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(TOKEN_BASE_URL)
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            CLOUD_API = retrofit.create(ProductCloudApi.class);
        }

        return CLOUD_API;
    }
}
