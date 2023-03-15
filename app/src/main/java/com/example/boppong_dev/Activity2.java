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
import android.media.Image;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.util.Base64;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class Activity2 extends AppCompatActivity implements RecyclerViewInterface, SensorEventListener {
    ArrayList<Player> players = new ArrayList<>();
    int[] playerProfilesDefault = {R.drawable.testprofile1,R.drawable.testprofile2
            ,R.drawable.testprofile3,R.drawable.testprofile4,R.drawable.testprofile5,R.drawable.testprofile6};

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
        setContentView(R.layout.activity_2);

        msharedPreferences = getSharedPreferences("GAME",MODE_PRIVATE);
        currentRound = msharedPreferences.getInt("currentRound",0);
        roundView = findViewById(R.id.roundView);
        roundView.setText("Current round: "+currentRound);

        myDb = new DatabaseHelper(Activity2.this);

        if (currentRound>roundLimit){
            reset();
        }

        RecyclerView recyclerView = findViewById(R.id.playerRecyclerView);
        players_recyclerViewAdapter adapter = new players_recyclerViewAdapter(this,players,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensors = new ArrayList<Sensor>(sensorManager.getSensorList(Sensor.TYPE_ALL));
        for (int i=0; i<sensors.size();i++){
            sensorList.add(sensors.get(i).getName());
            if (sensors.get(i).getStringType()=="android.sensor.device_orientation"){
                gyro = sensorManager.getDefaultSensor(sensors.get(i).getType());
            }
        }
    }

    private void reset(){
        editor = msharedPreferences.edit();
        editor.putInt("currentRound", 0);
        editor.commit();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
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

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(this,Activity3.class);

        //converting bitmap into byte[] to store as paracable extra

        Bitmap icon = players.get(position).getImage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String encodedImageString = Base64.encodeToString(b, Base64.DEFAULT);

        byte[] bytarray = Base64.decode(encodedImageString, Base64.DEFAULT);

        intent.putExtra("id", players.get(position).getId());
        intent.putExtra("name", players.get(position).getName());
        intent.putExtra("image", bytarray);
        intent.putExtra("song", players.get(position).getSongSubmission().getName());

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchPlayers();
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
                        Intent intent = new Intent(this,startRound.class);
                        startActivity(intent);
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

    protected void fetchPlayers(){
        Cursor cursor = myDb.readAllData();
        if (cursor.getCount()==0){
            Toast.makeText(Activity2.this,"no data", Toast.LENGTH_SHORT).show();
        }
        else{
            while(cursor.moveToNext()){
                //the image is converted from byte[] to bitmap using bytes2bitmap()
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