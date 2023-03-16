package com.example.boppong_dev;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Base64;
import android.widget.TextView;
import android.widget.Toast;

import com.example.boppong_dev.Model.Player;
import com.example.boppong_dev.Model.Song;
import com.example.boppong_dev.Model.players_recyclerViewAdapter;
import com.example.boppong_dev.Model.RecyclerViewInterface;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class LobbyActivity extends AppCompatActivity implements RecyclerViewInterface, SensorEventListener {
    ArrayList<Player> players = new ArrayList<>();
    SensorManager sensorManager;
    ArrayList<Sensor> sensors;
    ArrayList<String> sensorList = new ArrayList<String>();
    Sensor gyro;
    private SharedPreferences msharedPreferences;
    private SharedPreferences.Editor editor;
    int currentRound, roundLimit=3;
    TextView roundView;
    DatabaseHelper myDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lobby_activity);

        //opening internal storage to get current round and set it
        msharedPreferences = getSharedPreferences("GAME",MODE_PRIVATE);
        currentRound = msharedPreferences.getInt("currentRound",0);
        roundView = findViewById(R.id.roundView);
        roundView.setText("Current round: "+currentRound);

        myDb = new DatabaseHelper(LobbyActivity.this);

        //ends game if current round reaches the limit
        if (currentRound>roundLimit){
            reset();
        }

        //inflating recycler view with user data
        RecyclerView recyclerView = findViewById(R.id.playerRecyclerView);
        players_recyclerViewAdapter adapter = new players_recyclerViewAdapter(this,players,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //setting up gyroscope using the orientation sensor (doesn't require data filtering)
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensors = new ArrayList<Sensor>(sensorManager.getSensorList(Sensor.TYPE_ALL));
        for (int i=0; i<sensors.size();i++){
            sensorList.add(sensors.get(i).getName());
            if (sensors.get(i).getStringType()=="android.sensor.device_orientation"){
                gyro = sensorManager.getDefaultSensor(sensors.get(i).getType());
            }
        }
    }

    //resets internal and sql memory and brings user back to start screen
    private void reset(){
        myDb.wipe();
        editor = msharedPreferences.edit();
        editor.putInt("currentRound", 0);
        editor.commit();
        Intent intent = new Intent(this, StartScreenActivity.class);
        startActivity(intent);
    }

    //checks to see if all players have submitted a song
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

    //triggered every time a player row is clicked and takes user to the edit player screen
    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(this, UserEditActivity.class);

        //converting bitmap into byte[] to store as paracable extra

        Bitmap icon = players.get(position).getImage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String encodedImageString = Base64.encodeToString(b, Base64.DEFAULT);

        byte[] bytarray = Base64.decode(encodedImageString, Base64.DEFAULT);

        //technically don't need all of this, just the id and the rest can be retrieved from sql
        //loads selected player data into the edit user screen
        intent.putExtra("id", players.get(position).getId());
        intent.putExtra("name", players.get(position).getName());
        intent.putExtra("image", bytarray);
        intent.putExtra("song", players.get(position).getSongSubmission().getName());

        startActivity(intent);
    }

    //updating user data after it has been edited in user edit activity
    @Override
    protected void onResume() {
        super.onResume();
        //updating users via sql database
        fetchPlayers();
        //gyroscope activated
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
        //we dont need this as fetchplayers updates the users from sql
        //old method of passing back edit user data from user edit activity
        if (getIntent().hasExtra("songid")){
            String songUpdate = getIntent().getStringExtra("song");
            String songUpdateId = getIntent().getStringExtra("songid");
            int userPos = getIntent().getIntExtra("id",0);
            if (songUpdate.length()>0){
                players.get(userPos).setSongSubmission(new Song(songUpdateId,songUpdate));
            }
        }
    }

    //when phone is placed on table, players are checked to see if they have all submitted a song and if so
    //game prompt is started
    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        if (gyro!=null){
            if (type==gyro.getType()){
                if (event.values[0]==-1.0){
                    if (playersReady()){
                        Intent intent = new Intent(this, StartRoundActivity.class);
                        startActivity(intent);
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //disable sensor when activity is not in use
    protected void onPause(){
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    //updates player array using player data from sql database
    protected void fetchPlayers(){
        Cursor cursor = myDb.readAllData();
        if (cursor.getCount()==0){
            Toast.makeText(LobbyActivity.this,"no data", Toast.LENGTH_SHORT).show();
        }
        else{
            while(cursor.moveToNext()){
                //the image is converted from byte[] to bitmap using bytes2bitmap()
                players.add(new Player(Integer.parseInt(cursor.getString(0)),cursor.getString(1)
                ,new Song(cursor.getString(3),cursor.getString(4)),Bytes2Bitmap(cursor.getBlob(2))));
            }
        }
    }

    //sourced function that converts byte array to bitmap image
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