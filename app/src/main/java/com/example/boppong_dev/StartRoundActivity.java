package com.example.boppong_dev;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

public class StartRoundActivity extends AppCompatActivity {
    private ArrayList<String> prompts;
    int currentRound;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_round_activity);
        prompts = getIntent().getStringArrayListExtra("prompts");
        currentRound = getIntent().getIntExtra("round",0);
    }

    //begins the guessing game round
    public void startGuessingRound(View view){
        Intent intent = new Intent(this, RoundActivity.class);
        intent.putStringArrayListExtra("prompts",prompts);
        intent.putExtra("round", currentRound);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, LobbyActivity.class);
        intent.putStringArrayListExtra("prompts",prompts);
        startActivity(intent);
    }
}