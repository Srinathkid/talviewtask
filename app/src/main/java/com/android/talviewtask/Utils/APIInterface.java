package com.android.talviewtask.Utils;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface APIInterface {
    @GET("zKWW.json")
    Call<JsonObject> getSongsList();
}
