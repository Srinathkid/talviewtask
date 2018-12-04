package com.android.talviewtask.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.android.talviewtask.Adapters.AudioPlaylistAdapter;
import com.android.talviewtask.Model.Songs;
import com.android.talviewtask.Model.SongsLists;
import com.android.talviewtask.R;
import com.android.talviewtask.Service.DownloadService;
import com.android.talviewtask.Utils.RestClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;


import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {
    Songs songsModel;
    ArrayList<SongsLists> songsArrayList;
    RecyclerView audio_list_rv;
AudioPlaylistAdapter audioPlaylistAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        audio_list_rv = (RecyclerView) findViewById(R.id.rv_audio_list);

        songsArrayList = new ArrayList<>();
        audioPlaylistAdapter = new AudioPlaylistAdapter(MainActivity.this, songsArrayList);
        audio_list_rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false));
        audio_list_rv.setAdapter(audioPlaylistAdapter);
        getSongsList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(
                DownloadService.NOTIFICATION));



    }

    private void getSongsList() {
        Call<JsonObject> get_songs_list_call = RestClient.getInstance().getRetrofitInterface().getSongsList();
        get_songs_list_call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                songsModel = new Gson().fromJson(response.body(), Songs.class);
                for (int i = 0; i < songsModel.getSongs().size(); i++)
                    songsArrayList.add(songsModel.getSongs().get(i));


                System.out.println("API Response Size : " + songsArrayList.size());
               /* Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.putParcelableArrayListExtra(DownloadService.SONG_LIST, (ArrayList<? extends Parcelable>) songsArrayList);
                startService(intent);*/

                audioPlaylistAdapter = new AudioPlaylistAdapter(MainActivity.this, songsArrayList);
                audio_list_rv.setAdapter(audioPlaylistAdapter);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                System.out.println("API Response Error : " + t.getMessage());
            }
        });


    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
//                String string = bundle.getString(DownloadService.FILEPATH);
                int resultCode = bundle.getInt(DownloadService.RESULT);
                if (resultCode == RESULT_OK) {
                    Toast.makeText(MainActivity.this,
                            "Download complete." ,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Download failed",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    };
}
