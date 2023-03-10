package com.example.boppong_dev;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import com.example.boppong_dev.Model.Song;

import java.util.ArrayList;

public class MainActivity4 extends AppCompatActivity {

    private ArrayList<String> songList, playerList;

    String[] playerNames = {"josh","nathan","ahmik"};
    String[] playerSongs = {"id1","id2","id3"};

    TextView clockTimeView, gameStateView;
    String count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        clockTimeView = findViewById(R.id.clockTimeView);
        count = "10";

        clockTimeView.setText(count);
        gameStateView = findViewById(R.id.gameStateView);
        gameStateView.setText(checkState());

        clockTimeView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.toString().equals("-1")){
                    resetCount();
                    gameStateView.setText(checkState());
                }
            }
        });


    }

    protected String checkState(){
        String state = "guessing";
        if (count.equals("10")) {
            state = "reveal";
        }
        return state;
    }

    protected int countDown(){
        int i =10;
        while (i>-2){
            count = String.valueOf(i);
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    clockTimeView.setText(count);
                }
            });
            SystemClock.sleep(500);
            i --;
        }
        return i;
    }

    protected void resetCount(){
        count="10";
        clockTimeView.setText("10");
    }

    public void startCounting (){
        Thread myThread = new Thread(new CountingThread());
        myThread.start();
    }

    private class CountingThread implements Runnable
    {
        @Override
        public void run() {
            countDown();
        }
    }
}