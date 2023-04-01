package com.example.boppong_dev;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class StartRoundActivity extends AppCompatActivity {
    private ArrayList<String> prompts;
    int currentRound;
    Button button;
    MediaPlayer back,startSound;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_round_activity);

        startSound = MediaPlayer.create(this, R.raw.start);
        back = MediaPlayer.create(this, R.raw.back);

        //hides notification bar
        View decorView = getWindow().getDecorView();
        int options1 = View.SYSTEM_UI_FLAG_FULLSCREEN;
        int options2 = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(options1);
        decorView.setSystemUiVisibility(options2);

        prompts = getIntent().getStringArrayListExtra("prompts");
        currentRound = getIntent().getIntExtra("round",0);
        button = findViewById(R.id.button);

        ObjectAnimator animator = ObjectAnimator.ofFloat(button, "translationY", 0f, 20f);
        animator.setDuration(1000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.start();
    }

    //begins the guessing game round
    public void startGuessingRound(View view){
        startSound.start();
        Intent intent = new Intent(this, RoundActivity.class);
        intent.putStringArrayListExtra("prompts",prompts);
        intent.putExtra("round", currentRound);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        back.start();
        Intent intent = new Intent(this, LobbyActivity.class);
        intent.putStringArrayListExtra("prompts",prompts);
        startActivity(intent);
    }
}