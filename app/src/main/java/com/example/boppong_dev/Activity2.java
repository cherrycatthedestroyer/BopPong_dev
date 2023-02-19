package com.example.boppong_dev;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.boppong_dev.Connectors.SongService;
import com.example.boppong_dev.Model.Song;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.ArrayList;

public class Activity2 extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        songService = new SongService(getApplicationContext());
        userView = (TextView) findViewById(R.id.user);
        songView = (TextView) findViewById(R.id.song);
        pauseBtn = (Button) findViewById(R.id.pause);

        SharedPreferences sharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
        userView.setText(sharedPreferences.getString("userid", "No User"));

        getTracks();
        connected();
    }


    private void getTracks() {
        songService.searchTrack(() -> {
            searchResults = songService.getSongs();
            updateSong();
        });
    }

    private void updateSong() {
        if (searchResults.size() > 0) {
            songView.setText(searchResults.get(0).getName());
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
}