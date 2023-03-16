package com.example.boppong_dev;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.boppong_dev.Model.Player;
import com.example.boppong_dev.Model.Song;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity4 extends AppCompatActivity {
    DatabaseHelper myDb;
    ArrayList<Player> players = new ArrayList<>();

    TextView clockTimeView, gameStateView, nameRevealView;
    String count,countLimit="15";
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

        myDb = new DatabaseHelper(MainActivity4.this);
        fetchPlayers();

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
        startCounting(players.size());

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
                    nameRevealView.setText(players.get(playerIndex).getName());
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
        Thread myThread = new Thread(new CountingThread(players.size()));
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
                mSpotifyAppRemote.getPlayerApi().play("spotify:track:"+players.get(i).getSongSubmission().getId());
                countDown(number,i);
                togglePlayerName(i,0);
                mSpotifyAppRemote.getPlayerApi().pause();
                SystemClock.sleep(5000);
                togglePlayerName(i,0);
            }
            incrementRound();
            Intent newIntent = new Intent(MainActivity4.this, Activity2.class);
            startActivity(newIntent);
        }
    }

    protected void fetchPlayers(){
        Cursor cursor = myDb.readAllData();
        if (cursor.getCount()==0){
            Toast.makeText(MainActivity4.this,"no data", Toast.LENGTH_SHORT).show();
        }
        else{
            while(cursor.moveToNext()){
                players.add(new Player(Integer.parseInt(cursor.getString(0)),cursor.getString(1)
                        ,new Song(cursor.getString(3),cursor.getString(4)),Bytes2Bitmap(cursor.getBlob(2))));
            }
        }
    }

    public final static Bitmap Bytes2Bitmap(byte[] b) {
        if (b == null) {
            return null;
        }
        if (b.length != 0) {
            InputStream is = new ByteArrayInputStream(b);
            Bitmap bmp = BitmapFactory.decodeStream(is);
            return bmp;
        } else {
            return null;
        }
    }
}