package com.android.talviewtask.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.talviewtask.Model.SongsLists;
import com.android.talviewtask.R;
import com.android.talviewtask.Service.MediaPlayerService;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class SongsDetails extends AppCompatActivity implements View.OnClickListener {

    public static final String MEDIA_PATH = new String("/storage/emulated/0/AudioFile/");
    //    private ArrayList<SongsDirectory> songs = new ArrayList<>();
    private ArrayList<String> songs = new ArrayList<>();
    private MediaPlayer mp = new MediaPlayer();
    private int currentPosition = 0;


    private Button play_btn, pause_btn, forward_btn, rewind_btn;
    private ImageView iv;
    private MediaPlayer mediaPlayer;

    private double startTime = 0;
    private double finalTime = 0;

    private Handler myHandler = new Handler();
    ;
    private int forwardTime = 5000;
    private int backwardTime = 5000;
    private SeekBar seekbar;
    private TextView tx1, tx2, tx3, song_title_txt;

    public static int oneTimeOnly = 0;
    ArrayList<SongsLists> songsArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs_details);

        Bundle extras = getIntent().getExtras();
        if (extras == null)
            finish();

        songsArrayList = extras.getParcelableArrayList("audio_list");
        updateSongList();

        play_btn = (Button) findViewById(R.id.button);
        pause_btn = (Button) findViewById(R.id.button2);
        forward_btn = (Button) findViewById(R.id.button3);
        rewind_btn = (Button) findViewById(R.id.button4);
        iv = (ImageView) findViewById(R.id.imageView);

        tx1 = (TextView) findViewById(R.id.textView2);
        tx2 = (TextView) findViewById(R.id.textView3);
        tx3 = (TextView) findViewById(R.id.textView4);
        song_title_txt = (TextView) findViewById(R.id.song_title);
        tx3.setText("");

        mediaPlayer = new MediaPlayer();
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        seekbar.setClickable(false);
        pause_btn.setEnabled(false);

        play_btn.setOnClickListener(this);
        pause_btn.setOnClickListener(this);
        forward_btn.setOnClickListener(this);
        rewind_btn.setOnClickListener(this);
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

        if (id == R.id.button) {
            currentPosition = 0;

            playSong(songs.get(currentPosition));
        } else if (id == R.id.button2) {
            pauseSong();
        } else if (id == R.id.button3) {
            forwardSong();
        } else if (id == R.id.button4) {
            rewindSong();
        }
    }

    private void rewindSong() {
        int temp = (int) startTime;

        if ((temp - backwardTime) > 0) {
            startTime = startTime - backwardTime;
            mediaPlayer.seekTo((int) startTime);
            Toast.makeText(getApplicationContext(), "You have Jumped backward 5 seconds", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Cannot jump backward 5 seconds", Toast.LENGTH_SHORT).show();
        }
    }

    private void forwardSong() {
        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        intent.setAction(MediaPlayerService.ACTION_NEXT);
        intent.putParcelableArrayListExtra(MediaPlayerService.SONGS_LISTS, songsArrayList);
        intent.putExtra(MediaPlayerService.CURRENT_SONG_INDEX, currentPosition);

        startService(intent);

       /* int temp = (int) startTime;

        if ((temp + forwardTime) <= finalTime) {
            startTime = startTime + forwardTime;
            mediaPlayer.seekTo((int) startTime);
            Toast.makeText(getApplicationContext(), "You have Jumped forward 5 seconds", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Cannot jump forward 5 seconds", Toast.LENGTH_SHORT).show();
        }*/
    }

    private void pauseSong() {
        Toast.makeText(getApplicationContext(), "Pausing sound", Toast.LENGTH_SHORT).show();
        mediaPlayer.pause();
        pause_btn.setEnabled(false);
        play_btn.setEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBroadcastReceiver, new IntentFilter("Broadcast"));
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
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

    private void setSeekBar(final double percentage, double duration, int firsttime, String song_title) {
//        seekbar.setMax((int) duration);
//        myHandler.postDelayed(UpdateSongTime,100);
        /*if(firsttime==0){
            seekbar.setMax((int)duration);
        }*/
        seekbar.setMax((int) duration);
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
        song_title_txt.setText(song_title);
      /*  final Handler mHandler = new Handler();
//Make sure you update Seekbar on UI thread
        SongsDetails.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                seekbar.setProgress((int) percentage/1000);

                mHandler.postDelayed(this, 1000);
            }
        });*/
    }

    private void playSong(String s) {

        Toast.makeText(getApplicationContext(), "Playing sound", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        intent.setAction(MediaPlayerService.ACTION_PLAY);
        intent.putParcelableArrayListExtra(MediaPlayerService.SONGS_LISTS, songsArrayList);
        intent.putExtra(MediaPlayerService.CURRENT_SONG_INDEX, currentPosition);

        startService(intent);

       /* try {
//            mediaPlayer.release();
//            mediaPlayer.reset();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(MEDIA_PATH + s);

            mediaPlayer.prepare();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Media Player Error : " + e.getMessage());
        }
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();

            }
        });

        finalTime = mediaPlayer.getDuration();
        startTime = mediaPlayer.getCurrentPosition();

        if (oneTimeOnly == 0) {
            seekbar.setMax((int) finalTime);
            oneTimeOnly = 1;
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                nextSong();
            }
        });
        tx2.setText(String.format("%d : %d ",
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
*/
        /*seekbar.setProgress((int) startTime);

//        myHandler.postDelayed(UpdateSongTime, 100);
        pause_btn.setEnabled(true);
        play_btn.setEnabled(false);*/
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
            playSong(songs.get(currentPosition));
        }
    }

    class Mp3Filter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".mp3"));
        }
    }

    private Runnable UpdateSongTime = new Runnable() {
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


}