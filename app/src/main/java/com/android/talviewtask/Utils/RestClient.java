package com.android.talviewtask.Utils;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestClient {
    private static final String TEST_BASE_URL = "https://www.jasonbase.com/things/";
    private static final RestClient instance = new RestClient();
    private Retrofit retrofit;
    private APIInterface apiInterface;

    private RestClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(TEST_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }


    public static RestClient getInstance() {
        return instance;
    }

    public APIInterface getRetrofitInterface() {
        if (apiInterface == null)
            apiInterface = retrofit.create(APIInterface.class);

        return apiInterface;
    }
}
