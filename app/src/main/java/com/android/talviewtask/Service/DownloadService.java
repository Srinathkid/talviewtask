package com.android.talviewtask.Service;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.android.talviewtask.Model.SongsLists;
import com.android.talviewtask.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class DownloadService extends IntentService {
    public static final int UPDATE_PROGRESS = 8344;
    private int lastupdate = 0;
    private NotificationManager nm;
    private NotificationCompat.Builder mBuilder;
    public static final String RESULT = "result";
    public static final String NOTIFICATION = "service receiver";
    public static final String COUNT = "count";
    public static int count = 0;
    public static final String SONG_LIST = "song_list";
    ArrayList<SongsLists> songsLists;

    public DownloadService(String name) {
        super(name);
    }

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        songsLists = (ArrayList<SongsLists>) extras.getSerializable(SONG_LIST);
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/AudioFile");
        System.out.println("Download Path : " + root);
        if (!root.exists())
            root.mkdir();
        System.out.println("Download Service : " + songsLists.size());

        for (int i = 0; i < songsLists.size(); i++) {
            String file_name = songsLists.get(i).getTitle();
            mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setContentTitle(
                    file_name)
                    .setContentText("Download in progress")
                    .setSmallIcon(R.drawable.ic_launcher_background).setContentInfo("0%");

            mBuilder.setOngoing(true);

            String urlToDownload = songsLists.get(i).getLink();
            // ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra("receiver");

            try {
                URL url = new URL(urlToDownload);
                URLConnection connection = url.openConnection();
                connection.connect();
                // this will be useful so that you can show a typical 0-100% progress bar
                int fileLength = connection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(new File(root.getPath(),
                        songsLists.get(i).getTitle() + ".mp3"));

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;

                    progressChange((int) (total * 100) / fileLength);
                    // publishing the progress....
                    //   Bundle resultData = new Bundle();
                    //   resultData.putInt("progress" ,(int) (total * 100 / fileLength));
                    //   receiver.send(UPDATE_PROGRESS, resultData);
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    void progressChange(int progress) {


       /* if (lastupdate != progress) {
            lastupdate = progress;*/
        // not.contentView.setProgressBar(R.id.status_progress,
        // 100,Integer.valueOf(progress[0]), false);
        // inform the progress bar of updates in progress
        // nm.notify(42, not);
        if (progress < 100) {
            mBuilder.setProgress(100, Integer.valueOf(progress),
                    false).setContentInfo(progress + "%");
            nm.notify(12, mBuilder.build());
            Intent i = new Intent().putExtra(NOTIFICATION, progress + "%");
            this.sendBroadcast(i);
        } else {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    mBuilder.setContentText("Download complete")
                            .setProgress(0, 0, false).setOngoing(false).setContentInfo("");
                    ;

                    nm.notify(12, mBuilder.build());
                    Intent i = new Intent(NOTIFICATION);
                    i.putExtra(RESULT, Activity.RESULT_OK);
                    i.putExtra(COUNT, count);
                    sendBroadcast(i);
                    count++;
                }
            };

            Thread t = new Thread(r);
            t.start();


        }

//        }

    }
}
