package com.example.boppong_dev;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.boppong_dev.Connectors.SongService;
import com.example.boppong_dev.Model.Player;
import com.example.boppong_dev.Model.Song;
import com.example.boppong_dev.Model.players_recyclerViewAdapter;
import com.example.boppong_dev.Model.RecyclerViewInterface;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.ArrayList;

public class Activity2 extends AppCompatActivity implements RecyclerViewInterface, SensorEventListener {
    ArrayList<Player> players = new ArrayList<>();
    int[] playerProfilesDefault = {R.drawable.testprofile1,R.drawable.testprofile2
            ,R.drawable.testprofile3,R.drawable.testprofile4,R.drawable.testprofile5,R.drawable.testprofile6};
    private TextView userView;
    private TextView songView;
    private Button pauseBtn;
    private Song song;

    private SongService songService;
    private ArrayList<Song> searchResults;

    private static final String CLIENT_ID = "a24f9f02a4fc4adb8138143d99bd8dc9";
    private static final String REDIRECT_URI = "https://www.youtube.com/";
    private SpotifyAppRemote mSpotifyAppRemote;
    ConnectionParams connectionParams =
            new ConnectionParams.Builder(CLIENT_ID)
                    .setRedirectUri(REDIRECT_URI)
                    .showAuthView(true)
                    .build();

    SensorManager sensorManager;
    ArrayList<Sensor> sensors;
    ArrayList<String> sensorList = new ArrayList<String>();
    Sensor gyro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        songService = new SongService(getApplicationContext());

        SharedPreferences sharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
        //userView.setText(sharedPreferences.getString("userid", "No User"));

        RecyclerView recyclerView = findViewById(R.id.playerRecyclerView);
        setUpPlayers();
        players_recyclerViewAdapter adapter = new players_recyclerViewAdapter(this,players,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //getTracks();
        //connected();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensors = new ArrayList<Sensor>(sensorManager.getSensorList(Sensor.TYPE_ALL));
        for (int i=0; i<sensors.size();i++){
            sensorList.add(sensors.get(i).getName());
            if (sensors.get(i).getStringType()=="android.sensor.device_orientation"){
                gyro = sensorManager.getDefaultSensor(sensors.get(i).getType());
            }
        }
    }

    private void setUpPlayers(){
        String[] playerNames = getResources().getStringArray(R.array.testPlayers);
        for (int i=0;i<playerNames.length;i++){
            players.add(new Player(i,playerNames[i],new Song("i","y"),playerProfilesDefault[i]));
        }
    }

    private boolean playersReady(){
        boolean areReady = true;
        for (int i=0;i<players.size();i++){
            if (players.get(i).getSongSubmission().getName()==null){
                areReady = false;
                break;
            }
        }
        return areReady;
    }

    private void getTracks() {
        songService.searchTrack(() -> {
            searchResults = songService.getSongs();
            updateSong();
        });
    }

    private void updateSong() {
        if (searchResults.size() > 0) {
            //songView.setText(searchResults.get(0).getName());
            song = searchResults.get(0);
        }
    }

    private void connected() {
        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        mSpotifyAppRemote.getPlayerApi().play("spotify:track:"+song.getId());
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);
                    }
                });
    }

    public void songStop(View v){
        mSpotifyAppRemote.getPlayerApi().pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(this,Activity3.class);

        intent.putExtra("id", players.get(position).getId());
        intent.putExtra("name", players.get(position).getName());
        intent.putExtra("image", position);
        intent.putExtra("song", players.get(position).getSongSubmission().getName());

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
        if (getIntent().hasExtra("songid")){
            String songUpdate = getIntent().getStringExtra("song");
            String songUpdateId = getIntent().getStringExtra("songid");
            int userPos = getIntent().getIntExtra("id",0);
            if (songUpdate.length()>0){
                players.get(userPos).setSongSubmission(new Song(songUpdateId,songUpdate));
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        if (gyro!=null){
            if (type==gyro.getType()){
                if (event.values[0]==-1.0){
                    if (playersReady()){
                        //Intent intent = new Intent(this,startRound.class);
                        //startActivity(intent);
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause(){
        sensorManager.unregisterListener(this);
        super.onPause();
    }
}