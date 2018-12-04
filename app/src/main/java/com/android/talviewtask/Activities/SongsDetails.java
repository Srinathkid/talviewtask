package com.android.talviewtask.Activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.talviewtask.Model.SongsLists;
import com.android.talviewtask.R;
import com.android.talviewtask.Service.AudioService;
import com.android.talviewtask.Service.AudioServiceBinder;
import com.android.talviewtask.Service.MediaPlayerService;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class SongsDetails extends AppCompatActivity implements View.OnClickListener {

    public static final String MEDIA_PATH = new String("/storage/emulated/0/AudioFile/");
    //    private ArrayList<SongsDirectory> songs = new ArrayList<>();
    private ArrayList<String> songs = new ArrayList<>();
    private int currentPosition = 0;
    int currProgress;

    private AudioServiceBinder audioServiceBinder = null;

    private Handler audioProgressUpdateHandler = null;

    // Show played audio progress.
    private ProgressBar backgroundAudioProgress;

    private TextView audioFileUrlTextView;
    SeekBar seekbar;
    ImageView play_btn, pause_btn, next_btn, prev_btn, song_img;
    TextView curr_txt, total_txt, title_txt;
    // This service connection object is the bridge between activity and background service.
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // Cast and assign background service's onBind method returned iBander object.
            audioServiceBinder = (AudioServiceBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };


    private double startTime = 0;
    private double finalTime = 0;

    private Handler myHandler = new Handler();
    int total_dur;

    private int forwardTime = 5000;
    private int backwardTime = 5000;
    ArrayList<SongsLists> songsArrayList;
    final String audioFileUrl = "/storage/emulated/0/AudioFile/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs_details);
        bindAudioService();

        Bundle extras = getIntent().getExtras();
        if (extras == null)
            finish();

        setResources();
        songsArrayList = extras.getParcelableArrayList("audio_list");
        currentPosition = extras.getInt("current_index");
        updateSongList();

        setControls(false);

//        final String audioFileUrl = "http://www.dev2qa.com/demo/media/test.mp3";


    }

    private void setControls(boolean isPlaying) {
        if (isPlaying) {
            play_btn.setVisibility(View.GONE);
            pause_btn.setVisibility(View.VISIBLE);
        } else {
            play_btn.setVisibility(View.VISIBLE);
            pause_btn.setVisibility(View.GONE);
        }
    }

    private void setResources() {
        seekbar = (SeekBar) findViewById(R.id.seekbar);
        play_btn = (ImageView) findViewById(R.id.play);
        pause_btn = (ImageView) findViewById(R.id.pause);
        next_btn = (ImageView) findViewById(R.id.next);
        prev_btn = (ImageView) findViewById(R.id.previous);
        curr_txt = (TextView) findViewById(R.id.curr_time_txt);
        total_txt = (TextView) findViewById(R.id.total_time_txt);
        title_txt = (TextView) findViewById(R.id.title_txt);
        song_img = (ImageView) findViewById(R.id.song_img);
        play_btn.setOnClickListener(this);
        pause_btn.setOnClickListener(this);
        next_btn.setOnClickListener(this);
        prev_btn.setOnClickListener(this);
    }

    private void bindAudioService() {
        if (audioServiceBinder == null) {
            Intent intent = new Intent(SongsDetails.this, AudioService.class);

            // Below code will invoke serviceConnection's onServiceConnected method.
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void unBoundAudioService() {
        if (audioServiceBinder != null) {
            unbindService(serviceConnection);
        }
    }

    @Override
    protected void onDestroy() {
        // Unbound background audio service when activity is destroyed.
        unBoundAudioService();
        super.onDestroy();
    }

    public void updateSongList() {
        File home = new File(MEDIA_PATH);
        if (home.listFiles(new Mp3Filter()).length > 0) {
            for (File file : home.listFiles(new Mp3Filter())) {
                songs.add(file.getName());

            }

            for (int i = 0; i < songs.size(); i++)
                System.out.println("Songs Name : " + songs.get(i));
            ArrayAdapter<String> songList = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, songs);
//            setListAdapter(songList);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.play) {

            playSong(songsArrayList.get(currentPosition).getTitle());

        } else if (id == R.id.pause) {
            pauseSong();
        } else if (id == R.id.next) {
            nextSong();
        } else if (id == R.id.previous) {
            prevSong();
        }
    }

    private void playSong(String audio_name) {
        setUI();
        setControls(true);
        audioServiceBinder.setAudioFileUrl(audioFileUrl + audio_name + ".mp3");
//        audioServiceBinder.setAudioFileUrl("/storage/emulated/0/AudioFile/Transformers.mp3");

        // Web audio is a stream audio.
        audioServiceBinder.setStreamAudio(false);

        // Set application context.
        audioServiceBinder.setContext(getApplicationContext());

        // Initialize audio progress bar updater Handler object.
        createAudioProgressbarUpdater();
        audioServiceBinder.setAudioProgressUpdateHandler(audioProgressUpdateHandler);

        // Start audio in background service.
        audioServiceBinder.startAudio();

//        backgroundAudioProgress.setVisibility(ProgressBar.VISIBLE);
        int total_dur = audioServiceBinder.getTotalAudioDuration();
        seekbar.setMax(total_dur);
        myHandler.postDelayed(updateRunnable, 100);

    }

    private void setUI() {
        title_txt.setText(songsArrayList.get(currentPosition).getTitle() + ".mp3");
        Glide.with(getApplicationContext())
                .load(songsArrayList.get(currentPosition).getThumbnail())
                .asBitmap()
                .placeholder(R.drawable.thumb_img)
                .error(R.drawable.thumb_img)
                .into(song_img);
    }

    private void createAudioProgressbarUpdater() {
        if (audioProgressUpdateHandler == null) {
            audioProgressUpdateHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    total_dur = audioServiceBinder.getTotalAudioDuration();
                    seekbar.setMax(total_dur);

                    // The update process message is sent from AudioServiceBinder class's thread object.
                    if (msg.what == audioServiceBinder.UPDATE_AUDIO_PROGRESS_BAR) {

                        if (audioServiceBinder != null) {
                            // Calculate the percentage.
                            currProgress = audioServiceBinder.getAudioProgress();

                            // Update progressbar. Make the value 10 times to show more clear UI change.
//                            backgroundAudioProgress.setProgress(currProgress * 10);

//                            updateSeekBar(currProgress, total_dur);


//                            myHandler.postDelayed(updateRunnable,100);

                        }
                    }
                }
            };
        }
    }

    private void updateSeekBar(int currProgress, int total_dur) {
//        myHandler.postDelayed(updateRunnable, 10);
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                updateUI();

            }
        });
    }

    final Runnable updateRunnable = new Runnable() {
        public void run() {
            //call the activity method that updates the UI
            seekbar.setProgress((int) currProgress);
            if (total_dur == currProgress)
                nextSong();
            curr_txt.setText(String.format("%d : %d ",
                    TimeUnit.MILLISECONDS.toMinutes((long) currProgress),
                    TimeUnit.MILLISECONDS.toSeconds((long) currProgress) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) currProgress)))
            );
//        if (firsttime == 0) {
            total_txt.setText(String.format("%d : %d ",
                    TimeUnit.MILLISECONDS.toMinutes((long) total_dur),
                    TimeUnit.MILLISECONDS.toSeconds((long) total_dur) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    total_dur)))
            );
//            myHandler.postDelayed(this, 100);
        }
    };

    private void updateUI() {
        seekbar.setProgress((int) currProgress);
        curr_txt.setText(String.format("%d : %d ",
                TimeUnit.MILLISECONDS.toMinutes((long) currProgress),
                TimeUnit.MILLISECONDS.toSeconds((long) currProgress) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                toMinutes((long) currProgress)))
        );
//        if (firsttime == 0) {
        total_txt.setText(String.format("%d : %d ",
                TimeUnit.MILLISECONDS.toMinutes((long) total_dur),
                TimeUnit.MILLISECONDS.toSeconds((long) total_dur) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                total_dur)))
        );

    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            seekbar.setProgress((int) currProgress);
            myHandler.postDelayed(this, 100);
        }
    };

    private void prevSong() {
        if (currentPosition >= 1) {
            stopSong();
            playSong(songsArrayList.get(--currentPosition).getTitle());
        } else {
            stopSong();
            playSong(songsArrayList.get(currentPosition).getTitle());
        }

    }

    private void forwardSong() {


    }

    private void stopSong() {
        audioServiceBinder.stopAudio();
//        backgroundAudioProgress.setVisibility(ProgressBar.INVISIBLE);
    }

    private void pauseSong() {
        setControls(false);
        Toast.makeText(getApplicationContext(), "Pausing sound", Toast.LENGTH_SHORT).show();
        audioServiceBinder.pauseAudio();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBroadcastReceiver, new IntentFilter("Broadcast"));
    }

    /* private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             String intentType = intent.getStringExtra("INTENT_TYPE");
             if (intentType.equalsIgnoreCase("SEEKBAR_RESULT")) {
                 double percentage = intent.getDoubleExtra("PERCENTAGE", 0);
                 double duration = intent.getDoubleExtra("DURATION", 0);
                 int firsttime = intent.getIntExtra("INIT", 0);
                 currentPosition = intent.getIntExtra("CURRENT_POSITION", 0);
                 String song_title = intent.getStringExtra("SONG_NAME");
                 setSeekBar(percentage, duration, firsttime, song_title);
             }
         }
     };
 */
    private void setSeekBar(final double percentage, double duration, int firsttime, String song_title) {

     /*   seekbar.setMax((int) duration);
        tx1.setText(String.format("%d : %d ",
                TimeUnit.MILLISECONDS.toMinutes((long) percentage),
                TimeUnit.MILLISECONDS.toSeconds((long) percentage) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                toMinutes((long) percentage)))
        );
//        if (firsttime == 0) {
        tx2.setText(String.format("%d : %d ",
                TimeUnit.MILLISECONDS.toMinutes((long) duration),
                TimeUnit.MILLISECONDS.toSeconds((long) duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                duration)))
        );

        seekbar.setProgress((int) percentage);
        song_title_txt.setText(song_title);*/

    }


    private void nextSong() {
       /* if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        } else if (mediaPlayer != null) {
            mediaPlayer.reset();
        }*/
        if (++currentPosition >= songs.size()) {
            currentPosition = 0;
        } else {
            stopSong();
            playSong(songsArrayList.get(currentPosition).getTitle());
        }
    }


    class Mp3Filter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".mp3"));
        }
    }

   /* private Runnable UpdateSongTime = new Runnable() {
        public void run() {
//            startTime = mediaPlayer.getCurrentPosition();
            tx1.setText(String.format("%d : %d ",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) startTime)))
            );
            seekbar.setProgress((int) startTime);
            myHandler.postDelayed(this, 100);
        }
    };
*/

}