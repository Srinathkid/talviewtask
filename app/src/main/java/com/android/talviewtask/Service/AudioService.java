package com.android.talviewtask.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Lenovo on 04-12-2018.
 */

public class AudioService extends Service {
    private AudioServiceBinder audioServiceBinder = new AudioServiceBinder();

    public AudioService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return audioServiceBinder;
    }
}
