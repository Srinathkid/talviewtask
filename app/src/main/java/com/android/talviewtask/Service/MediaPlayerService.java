package com.android.talviewtask.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Rating;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.talviewtask.Model.SongsLists;
import com.android.talviewtask.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Lenovo on 03-12-2018.
 */

public class MediaPlayerService extends Service {
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_REWIND = "action_rewind";
    public static final String ACTION_FAST_FORWARD = "action_fast_foward";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";

    public static final String SONGS_LISTS = "songs_list";
    public static final String CURRENT_SONG_INDEX = "song_index";

    public static final String MEDIA_PATH = new String("/storage/emulated/0/AudioFile/");

    private MediaPlayer mMediaPlayer;
    private MediaSessionManager mManager;
    private MediaSession mSession;
    private MediaController mController;
    private Handler myHandler = new Handler();
    int currentposition;
    List<String> songs = new ArrayList<>();
    ArrayList<SongsLists> songsArrayList;

    private double startTime = 0;
    private double finalTime = 0;
    public static int oneTimeOnly = 0;
    private int forwardTime = 5000;
    private int backwardTime = 5000;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null)
            return;


        songsArrayList = intent.getParcelableArrayListExtra(SONGS_LISTS);
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        currentposition = extras.getInt(CURRENT_SONG_INDEX);

        if (action.equalsIgnoreCase(ACTION_PLAY)) {
            mController.getTransportControls().play();
        } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {
            mController.getTransportControls().pause();
        } else if (action.equalsIgnoreCase(ACTION_FAST_FORWARD)) {
            mController.getTransportControls().fastForward();
        } else if (action.equalsIgnoreCase(ACTION_REWIND)) {
            mController.getTransportControls().rewind();
        } else if (action.equalsIgnoreCase(ACTION_PREVIOUS)) {
            mController.getTransportControls().skipToPrevious();
        } else if (action.equalsIgnoreCase(ACTION_NEXT)) {
            mController.getTransportControls().skipToNext();
        } else if (action.equalsIgnoreCase(ACTION_STOP)) {
            mController.getTransportControls().stop();
        }
    }

    private Notification.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder(icon, title, pendingIntent).build();
    }

    private void buildNotification(Notification.Action action) {
        Notification.MediaStyle style = new Notification.MediaStyle();

        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        intent.setAction(ACTION_STOP);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(songsArrayList.get(currentposition).getTitle())
                .setDeleteIntent(pendingIntent)
                .setStyle(style);

        builder.addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS));
        builder.addAction(generateAction(android.R.drawable.ic_media_rew, "Rewind", ACTION_REWIND));
        builder.addAction(action);
        builder.addAction(generateAction(android.R.drawable.ic_media_ff, "Fast Foward", ACTION_FAST_FORWARD));
        builder.addAction(generateAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT));
        style.setShowActionsInCompactView(0, 1, 2, 3, 4);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mManager == null) {
            initMediaSessions();
        }

        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void initMediaSessions() {
        mMediaPlayer = new MediaPlayer();

        mSession = new MediaSession(getApplicationContext(), "simple player session");
        mController = new MediaController(getApplicationContext(), mSession.getSessionToken());

        mSession.setCallback(new MediaSession.Callback() {
                                 @Override
                                 public void onPlay() {
                                     super.onPlay();
                                     Log.e("MediaPlayerService", "onPlay");
                                     playSong(currentposition);
                                 }

                                 @Override
                                 public void onPause() {
                                     super.onPause();
                                     Log.e("MediaPlayerService", "onPause");
                                     buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
                                 }

                                 @Override
                                 public void onSkipToNext() {
                                     super.onSkipToNext();
                                     Log.e("MediaPlayerService", "onSkipToNext");
                                     //Change media here
                                     nextSong();
                                     buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
                                 }

                                 @Override
                                 public void onSkipToPrevious() {
                                     super.onSkipToPrevious();
                                     Log.e("MediaPlayerService", "onSkipToPrevious");
                                     //Change media here
                                     buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
                                 }

                                 @Override
                                 public void onFastForward() {
                                     super.onFastForward();
                                     Log.e("MediaPlayerService", "onFastForward");
//                                     forwardSong();
                                     //Manipulate current media here
                                 }

                                 @Override
                                 public void onRewind() {
                                     super.onRewind();
                                     Log.e("MediaPlayerService", "onRewind");
                                     //Manipulate current media here
                                 }

                                 @Override
                                 public void onStop() {
                                     super.onStop();
                                     Log.e("MediaPlayerService", "onStop");
                                     //Stop media player here
                                     stopService();
                                 }

                                 @Override
                                 public void onSeekTo(long pos) {
                                     super.onSeekTo(pos);
                                 }

                                 @Override
                                 public void onSetRating(Rating rating) {
                                     super.onSetRating(rating);
                                 }
                             }
        );
    }

    private void stopService() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        stopService(intent);

        mMediaPlayer.stop();
    }

    private void forwardSong() {
        int temp = (int) startTime;

        if ((temp + forwardTime) <= finalTime) {
            startTime = startTime + forwardTime;
            mMediaPlayer.seekTo((int) startTime);
            Toast.makeText(getApplicationContext(), "You have Jumped forward 5 seconds", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Cannot jump forward 5 seconds", Toast.LENGTH_SHORT).show();
        }

        myHandler.postDelayed(UpdateSongTime, 100);

    }

    private void playSong(int position) {
        try {
//            mediaPlayer.release();
//            mediaPlayer.reset();
            mMediaPlayer.reset();
            try {
                mMediaPlayer.setDataSource(MEDIA_PATH + songsArrayList.get(position).getTitle() + ".mp3");
            } catch (IOException e) {
                e.printStackTrace();
            }

            mMediaPlayer.prepare();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Media Player Error : " + e.getMessage());
        }
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();

            }
        });

        finalTime = mMediaPlayer.getDuration();
        startTime = mMediaPlayer.getCurrentPosition();

                                     /*if (oneTimeOnly == 0) {
                                         seekbar.setMax((int) finalTime);
                                         oneTimeOnly = 1;
                                     }*/

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                nextSong();
            }
        });

                                     /*tx2.setText(String.format("%d : %d ",
                                             TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                                             TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                                                     TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                                             finalTime)))
                                     );

                                     tx1.setText(String.format("%d : %d ",
                                             TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                                             TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                                     TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                                             startTime)))
                                     );

                                     seekbar.setProgress((int) startTime);

                                     myHandler.postDelayed(UpdateSongTime, 100);
                                     pause_btn.setEnabled(true);
                                     play_btn.setEnabled(false);*/
        myHandler.postDelayed(UpdateSongTime, 100);
        buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mSession.release();
        return super.onUnbind(intent);
    }

    private void nextSong() {
       /* if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        } else if (mediaPlayer != null) {
            mediaPlayer.reset();
        }*/
        mSession.release();
        stopService();
//        mMediaPlayer.stop();
        mMediaPlayer.reset();
        /*if (++currentposition >= songsArrayList.size()) {
            currentposition = 0;
        } else {
            playSong(currentposition);
        }*/
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = mMediaPlayer.getCurrentPosition();
            finalTime = mMediaPlayer.getDuration();

            String song_title = songsArrayList.get(currentposition).getTitle();
            if (oneTimeOnly == 0) {
//                seekbar.setMax((int) finalTime);
                oneTimeOnly = 1;
            }
           /* tx1.setText(String.format("%d : %d ",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) startTime)))
            );*/
//            seekbar.setProgress((int) startTime);
            myHandler.postDelayed(this, 100);
            publishResult(getApplicationContext(), startTime, finalTime, oneTimeOnly, song_title);
        }
    };

    public void publishResult(Context context, double startTime, double finalTime, int oneTimeOnly, String song_title) {
        Intent intent = new Intent("Broadcast");
        intent.putExtra("INTENT_TYPE", "SEEKBAR_RESULT");
        intent.putExtra("PERCENTAGE", startTime);
        intent.putExtra("DURATION", finalTime);
        intent.putExtra("SONG_NAME", song_title);
        intent.putExtra("INIT", oneTimeOnly);
        intent.putExtra("CURRENT_POSITION", currentposition);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
