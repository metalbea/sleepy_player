package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.icu.text.DecimalFormat;
import android.icu.text.NumberFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.myapplication.databinding.ActivityMainBinding;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private Button buttonStart;
    private Button buttonStop;
    private Button buttonHelp;
    private TextView countdown;
    private AudioManager audioManager;
    private int currentVolume;
    private int minutes;
    private long minutesInMilliseconds;
    private boolean busy = false;
    private boolean quit = false;
    private CountDownTimer countDownTimer;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        countdown = findViewById(R.id.textViewCountdown);
        buttonHelp = findViewById(R.id.buttonHelp);
        buttonHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog("With Sleepy Player you can listen to music while you're trying to sleep.\n\n" +
                        "Start your music from any application on your phone.\n\n" +
                        "Decide how many minutes you want it to play.\n\n" +
                        "It will gradually be more silent and when the timer runs out, the music stops and your volume resets to how it was.", "Info");
            }
        });
        buttonStop = findViewById(R.id.buttonStop);
        EditText minutesEditText = findViewById(R.id.minuteTextbox);
        buttonStart = findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean ok = true;
                if (!busy) {
                    currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    busy = true;
                    try {
                        minutes = Integer.parseInt(minutesEditText.getText().toString());
                    } catch (Exception e) {
                        ok = false;
                    }
                    if (minutes < 1) {
                        ok = false;
                    }
                    if (ok) {
                        minutesInMilliseconds = TimeUnit.MINUTES.toMillis(minutes);
                        countDownTimer = new CountDownTimer(minutesInMilliseconds, 1000) {
                            public void onTick(long millisUntilFinished) {
                                // Used for formatting digit to be in 2 digits only
                                NumberFormat f = new DecimalFormat("00");
                                long hour = (millisUntilFinished / 3600000) % 24;
                                long min = (millisUntilFinished / 60000) % 60;
                                long sec = (millisUntilFinished / 1000) % 60;
                                countdown.setText(f.format(hour) + ":" + f.format(min) + ":" + f.format(sec));
                                double percentage = (double) millisUntilFinished / (double) minutesInMilliseconds;
                                setVolume(percentage);
                            }

                            // When the task is over it will print 00:00:00 there
                            public void onFinish() {
                                KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE);
                                audioManager.dispatchMediaKeyEvent(event);
                                while (audioManager.isMusicActive()) {
                                    System.out.println("music is still playing");

                                }
                                finishStopwatch();
                            }
                        }.start();
                    } else {
                        openDialog("must be a number higher then 0", "Warning");
                        busy = false;
                    }
                }
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(busy){
                    finishStopwatch();
                    countDownTimer.cancel();
                }
            }
        });
    }

    public void openDialog(String message, String title) {
        ExampleDialog exampleDialog = new ExampleDialog(message, title);
        exampleDialog.show(getSupportFragmentManager(), message);
    }

    private void setVolume(double percent) {
        int volume = ((int) (currentVolume * percent));
        if(percent != 1){
            volume += 1;
        }
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    private void finishStopwatch() {
        busy = false;
        countdown.setText("00:00:00");
        setVolume(1);
    }
}