package com.example.boppong_dev;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.boppong_dev.Model.Song;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;

import java.util.ArrayList;

public class MainActivity4 extends AppCompatActivity {

    private ArrayList<String> songList, playerList;

    String[] playerNames = {"josh","nathan","ahmik"};
    String[] playerSongs = {"4sFzG7iUlyPmuyASCkre9A","6ljK8pycxEv0Yqn1cdxvME","0JXXNGljqupsJaZsgSbMZV"};

    TextView clockTimeView, gameStateView, nameRevealView;
    String count,countLimit="2";
    int prevRound;

    private static final String CLIENT_ID = "a24f9f02a4fc4adb8138143d99bd8dc9";
    private static final String REDIRECT_URI = "https://www.youtube.com/";
    private SpotifyAppRemote mSpotifyAppRemote;
    private SharedPreferences.Editor editor;
    private SharedPreferences msharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        clockTimeView = findViewById(R.id.clockTimeView);
        count = countLimit;
        nameRevealView = findViewById(R.id.nameRevealView);

        clockTimeView.setText(count);
        gameStateView = findViewById(R.id.gameStateView);
        gameStateView.setText("guessing");

        msharedPreferences = getSharedPreferences("GAME",MODE_PRIVATE);
        prevRound = msharedPreferences.getInt("currentRound",0);

        clockTimeView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

            }

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

    protected void onStart() {
        super.onStart();
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        // Now you can start interacting with App Remote
                        connected();

                    }

                    public void onFailure(Throwable throwable) {
                        Log.e("MyActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    private void connected() {
        // Play a playlist
        startCounting(playerNames.length);

        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("MainActivity", track.name + " by " + track.artist.name);
                    }
                });
    }

    protected String checkState(){
        String state = "guessing";
        nameRevealView.setText("Who picked it?");
        if (count.equals(countLimit)) {
            state = "reveal";
        }
        return state;
    }

    protected int countDown(int playerNumber, int playerIndex){
        int i =Integer.parseInt(countLimit);
        while (i>-2){
            count = String.valueOf(i);
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    clockTimeView.setText(count);
                    gameStateView.setText(checkState());
                }
            });
            SystemClock.sleep(1000);
            i --;
        }
        return i;
    }

    protected void togglePlayerName(int playerIndex,int toggle){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (toggle==0){
                    nameRevealView.setText(playerNames[playerIndex]);
                }
                else{
                    nameRevealView.setText("Who picked it?");
                }
            }
        });
    }

    protected void incrementRound(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editor = msharedPreferences.edit();
                editor.putInt("currentRound", prevRound+1);
                editor.commit();
            }
        });
    }

    protected void resetCount(){
        count=countLimit;
        clockTimeView.setText(countLimit);
    }

    public void startCounting (int playerNumber){
        Thread myThread = new Thread(new CountingThread(playerNames.length));
        myThread.start();
    }

    private class CountingThread implements Runnable
    {
        private int number;
        public CountingThread(int playerNumber) {
            number=playerNumber;
        }

        @Override
        public void run() {
            for (int i=0;i<number;i++){
                mSpotifyAppRemote.getPlayerApi().play("spotify:track:"+playerSongs[i]);
                countDown(number,i);
                togglePlayerName(i,0);
                mSpotifyAppRemote.getPlayerApi().pause();
                SystemClock.sleep(1000);
                togglePlayerName(i,0);
            }
            incrementRound();
            Intent newIntent = new Intent(MainActivity4.this, Activity2.class);
            startActivity(newIntent);
        }
    }
}