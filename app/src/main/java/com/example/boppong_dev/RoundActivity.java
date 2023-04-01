package com.example.boppong_dev;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.boppong_dev.Connectors.Serializer;
import com.example.boppong_dev.Model.Player;
import com.example.boppong_dev.Model.Song;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

public class RoundActivity extends AppCompatActivity {
    DatabaseHelper myDb;
    ArrayList<Player> players = new ArrayList<>();
    ArrayList<Player> playersUnshuffled = new ArrayList<>();
    TextView clockTimeView, nameRevealView;
    String count, countLimit;
    int prevRound;
    private static final String CLIENT_ID = "a24f9f02a4fc4adb8138143d99bd8dc9";
    private static final String REDIRECT_URI = "https://www.youtube.com/";
    private SpotifyAppRemote mSpotifyAppRemote;
    private SharedPreferences.Editor editor;
    private ProgressBar progressBar;
    private TextView progressText;
    TextView roundView;
    private ImageView playerReveal;
    private CardView outerCard;
    ArrayList<String> prompts;
    TextView prompt;
    Dialog popup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.round_activity);

        //round number loaded from shared preferences
        getPrefs();

        //players loaded in from sql database
        myDb = new DatabaseHelper(RoundActivity.this);
        try {
            fetchPlayers();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        prevRound = getIntent().getIntExtra("round",0);

        clockTimeView = findViewById(R.id.clockTimeView);
        count = countLimit;
        nameRevealView = findViewById(R.id.nameRevealView);

        clockTimeView.setText(count);

        progressBar = findViewById(R.id.progress_bar);
        progressText = findViewById(R.id.progress_text);

        playerReveal = findViewById(R.id.playerImageRevealView);
        playerReveal.setVisibility(View.INVISIBLE);
        outerCard = findViewById(R.id.outerCard);
        outerCard.setVisibility(View.INVISIBLE);

        roundView = findViewById(R.id.roundView2);
        roundView.setText("Round "+(int)(prevRound+(int)1));

        prompts = getIntent().getStringArrayListExtra("prompts");
        prompt = findViewById(R.id.promptView2);
        prompt.setText(prompts.get(prevRound));

        popup = new Dialog(this);

        //This listener is responsible for changing the view data depending on what game state
        //It is being used as an event listener where view data is updated depending on the state its in
        //the app is in either GUESSING or LISTENING
        clockTimeView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            //resets the clock after GUESSING state is completed
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.toString().equals("-1")){
                    resetCount();
                    checkState();
                }
            }
        });
    }

    //thread responsible for calling counting functions and playing songs using spotify app
    private class CountingThread implements Runnable
    {
        private int number;
        public CountingThread(int playerNumber) {
            number=playerNumber;
        }

        @Override
        public void run() {
            for (int i=0;i<number;i++){
                //plays spotify track using spotify app
                mSpotifyAppRemote.getPlayerApi().play("spotify:track:"+players.get(i).getSongSubmission().getId());
                //GUESSING state countdown
                countDown(number,i);
                //GUESSING state player hidden
                togglePlayerName(i,0);
                togglePicture(i,1);
                //pauses spotify song at the end of GUESSING state
                mSpotifyAppRemote.getPlayerApi().pause();
                //REVEAL state for 5 seconds
                SystemClock.sleep(5000);
                togglePlayerName(i,0);
                togglePicture(i,0);
            }
            for (int i=0;i<number;i++) {
                Player currPlayer = playersUnshuffled.get(i);
                //converting drawable into bitmap and then into byte[] to store in sql
                Bitmap icon = currPlayer.getImage();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                icon.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] b = baos.toByteArray();
                String encodedImageString = Base64.encodeToString(b, Base64.DEFAULT);
                byte[] bytarray = Base64.decode(encodedImageString, Base64.DEFAULT);
                myDb.updateData(Integer.toString(currPlayer.getId()), currPlayer.getName(), null, null, null, bytarray);
            }
            backToLobby();
        }
    }

    //SOURCED from spotify sdk setup, modified to work for our needs
    protected void onStart() {
        super.onStart();
        //string that contains user authentication details to use spotify app as music player
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        //connected to spotify app to use as remote
        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("RoundActivity", "Connected");
                        // Now you can start interacting with App Remote
                        connected();

                    }

                    public void onFailure(Throwable throwable) {
                        Log.e("RoundActivity", throwable.getMessage(), throwable);
                        // didn't connect
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    //Using spotify app music player
    private void connected() {
        // start GUESSING state
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

    //Called in the listener for the
    protected String checkState(){
        String state = "guessing";
        nameRevealView.setText("Who picked it?");
        if (count.equals(countLimit)) {
            state = "reveal";
        }
        return state;
    }

    //Clock to countdown to time limit for GUESSING state
    protected int countDown(int playerNumber, int playerIndex){
        int i =Integer.parseInt(countLimit);
        while (i>-2){
            count = String.valueOf(i);
            int percentage = (int) ((Float.parseFloat(countLimit)-i-1)/Float.parseFloat(countLimit)*100);
            this.runOnUiThread(new Runnable() {
                //view is updated
                @Override
                public void run() {
                    clockTimeView.setText(count);
                    checkState();
                    if (Integer.parseInt(count)+(int)1==Integer.parseInt(countLimit)+1){
                        progressText.setText("...");
                    }
                    else{
                        progressText.setText("" + (Integer.parseInt(count)+(int)1));
                    }
                    progressBar.setProgress(percentage);
                }
            });
            SystemClock.sleep(1000);
            i --;
        }
        return i;
    }

    //function responsible for revealing player name when state is changed
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

    //increments round and stores it in shared pref internal memory
    protected void incrementRound(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editor = getSharedPreferences("GAME", 0).edit();
                editor.putInt("currentRound", prevRound+1);
                editor.commit();
            }
        });
    }

    protected void togglePicture(int playerPos, int toggle){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (toggle==0){
                    playerReveal.setVisibility(View.INVISIBLE);
                    outerCard.setVisibility(View.INVISIBLE);
                }
                else{
                    playerReveal.setImageBitmap(players.get(playerPos).getImage());
                    playerReveal.setVisibility(View.VISIBLE);
                    outerCard.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    protected void backToLobby(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent newIntent = new Intent(RoundActivity.this, LobbyActivity.class);
                newIntent.putStringArrayListExtra("prompts",prompts);
                newIntent.putExtra("round", prevRound+1);
                startActivity(newIntent);
            }
        });
    }

    //count variable and count view text is reset when Guessing state is over
    protected void resetCount(){
        count=countLimit;
        clockTimeView.setText(countLimit);
    }

    //starts a new thread to count down for guessing state and play music to prevent
    //main ui thread blockage
    public void startCounting (int playerNumber){
        Thread myThread = new Thread(new CountingThread(players.size()));
        myThread.start();
    }

    //loads in players from sql database
    protected void fetchPlayers() throws Exception {
        Cursor cursor = myDb.readAllData();
        if (cursor.getCount()==0){
            Toast.makeText(RoundActivity.this,"no data", Toast.LENGTH_SHORT).show();
        }
        else{
            while(cursor.moveToNext()){
                players.add(new Player(Integer.parseInt(cursor.getString(0)),cursor.getString(1)
                        ,new Song(cursor.getString(3),cursor.getString(4)), Serializer.deserializeBitmap(cursor.getBlob(2))));
            }
        }
        playersUnshuffled = players;
        Collections.shuffle(players);
    }

    private void reset(){
        myDb.wipe();
        Intent intent = new Intent(this, StartScreenActivity.class);
        startActivity(intent);
    }

    protected void getPrefs(){
        SharedPreferences sharedPrefs = getSharedPreferences("GAME",MODE_PRIVATE);
        countLimit= String.valueOf(sharedPrefs.getInt("roundLength",15));
    }

    public void onBackPressed() {
        popup.setContentView(R.layout.pop_up);
        Button yes = popup.findViewById(R.id.no);
        Button no = popup.findViewById(R.id.yes);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });
        popup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.show();
    }
}