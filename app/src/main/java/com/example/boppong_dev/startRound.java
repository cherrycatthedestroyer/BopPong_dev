package com.example.boppong_dev;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class startRound extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_round);
    }

    public void startGuessingRound(View view){
        Intent intent = new Intent(this,MainActivity4.class);
        startActivity(intent);
    }
}