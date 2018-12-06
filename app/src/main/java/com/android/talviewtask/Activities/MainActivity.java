package com.android.talviewtask.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
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

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {
    Songs songsModel;
    ArrayList<SongsLists> songsArrayList;
    RecyclerView audio_list_rv;
    AudioPlaylistAdapter audioPlaylistAdapter;
    int song_size = 0;
    ProgressDialog progressDialog, downloadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Paper.init(this);
        setContentView(R.layout.activity_main);

        Paper.book().write("isFirstTime", false);
        audio_list_rv = (RecyclerView) findViewById(R.id.rv_audio_list);

        songsArrayList = new ArrayList<>();
        audioPlaylistAdapter = new AudioPlaylistAdapter(MainActivity.this, songsArrayList);
        audio_list_rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false));
        audio_list_rv.setAdapter(audioPlaylistAdapter);
        if (isStoragePermissionGranted())
            getSongsList();

    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
//                Log.v(TAG,"Permission is granted");
                return true;
            } else {

//                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
//            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(
                DownloadService.NOTIFICATION));


    }

    private void getSongsList() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading List...");
        Call<JsonObject> get_songs_list_call = RestClient.getInstance().getRetrofitInterface().getSongsList();
        get_songs_list_call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                songsModel = new Gson().fromJson(response.body(), Songs.class);
                for (int i = 0; i < songsModel.getSongs().size(); i++)
                    songsArrayList.add(songsModel.getSongs().get(i));

                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                System.out.println("API Response Size : " + songsArrayList.size());
                boolean isFirstTime = Paper.book().read("isFirstTime");
                Paper.book().write("song_size", songsArrayList.size());
                setAdapter();

                if (!isFirstTime) {
                    downloadingDialog = new ProgressDialog(MainActivity.this);
                    downloadingDialog.setMessage("Downloading ");
                    /*Intent intent = new Intent(MainActivity.this, DownloadService.class);
                    intent.putParcelableArrayListExtra(DownloadService.SONG_LIST, (ArrayList<? extends Parcelable>) songsArrayList);
                    startService(intent);*/
                } else {
                    setAdapter();
                }


            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                System.out.println("API Response Error : " + t.getMessage());
            }
        });


    }

    private void setAdapter() {
        audioPlaylistAdapter = new AudioPlaylistAdapter(MainActivity.this, songsArrayList);
        audio_list_rv.setAdapter(audioPlaylistAdapter);
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
//                String string = bundle.getString(DownloadService.FILEPATH);
                int resultCode = bundle.getInt(DownloadService.RESULT);
                int count = bundle.getInt(DownloadService.COUNT);
                if (resultCode == RESULT_OK) {
                    song_size = Paper.book().read("song_size");
                    if (song_size == count) {
                        if (downloadingDialog.isShowing())
                            downloadingDialog.dismiss();

                        setAdapter();
                    }
                    Toast.makeText(MainActivity.this,
                            "Download complete.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Download failed",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    };
}
