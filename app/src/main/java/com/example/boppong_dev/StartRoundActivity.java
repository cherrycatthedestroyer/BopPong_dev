package com.example.boppong_dev;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class StartRoundActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_round_activity);
    }

    //begins the guessing game round
    public void startGuessingRound(View view){
        Intent intent = new Intent(this, RoundActivity.class);
        startActivity(intent);
    }
}