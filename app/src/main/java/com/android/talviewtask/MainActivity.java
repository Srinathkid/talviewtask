package com.android.talviewtask;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.talviewtask.Model.Songs;
import com.android.talviewtask.Utils.RestClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;


import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {
    Songs songsModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getSongsList();
    }

    private void getSongsList() {
        Call<JsonObject> get_songs_list_call = RestClient.getInstance().getRetrofitInterface().getSongsList();
        get_songs_list_call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                System.out.println("API Response : " + response.body().toString());
                songsModel = new Gson().fromJson(response.body(), Songs.class);
                System.out.println("API Response : "+songsModel.getSongs().size());

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                System.out.println("API Response Error : " + t.getMessage());
            }
        });

        /*get_songs_list_call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                System.out.println("API Response : " + response.body());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                System.out.println("API Response : " + t.getMessage());
            }
        });*/
    }
}
