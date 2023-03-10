package com.example.boppong_dev;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.example.boppong_dev.Connectors.UserService;
import com.example.boppong_dev.Model.Song;
import com.example.boppong_dev.Model.User;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String CLIENT_ID = "a24f9f02a4fc4adb8138143d99bd8dc9";
    private static final String REDIRECT_URI = "https://www.youtube.com/";
    private SpotifyAppRemote mSpotifyAppRemote;

    protected RequestQueue mRequestQueue;
    protected StringRequest mStringRequest;
    protected String url = "https://api.spotify.com/v1/search?q=hair%20down&type=track&limit=3&offset=5";

    private static final int REQUEST_CODE = 1337;
    private SharedPreferences.Editor editor;
    protected SharedPreferences msharedPreferences;
    protected AuthorizationRequest.Builder builder =
            new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

    protected Button minus,plus,start;
    protected TextView playerCount;
    int[] playerProfilesDefault = {R.drawable.testprofile1,R.drawable.testprofile2
            ,R.drawable.testprofile3,R.drawable.testprofile4,R.drawable.testprofile5,R.drawable.testprofile6};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerCount = findViewById(R.id.playerCountView);
        plus = findViewById(R.id.increaseGroupView);
        minus = findViewById(R.id.reduceGroupView);
        start = findViewById(R.id.startGame);
        plus.setOnClickListener(this);
        minus.setOnClickListener(this);
        start.setOnClickListener(this);

        playerCount.setText("0");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    editor = getSharedPreferences("SPOTIFY", 0).edit();
                    editor.putString("token", response.getAccessToken());
                    Log.d("STARTING", "GOT AUTH TOKEN");
                    editor.apply();
                    waitForUserInfo();
                    System.out.println("working");
                    break;

                // Auth flow returned an error
                case ERROR:
                    System.out.println("bad auth");
                    System.out.println(response.getCode());
                    System.out.println(response.getError());

                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }

    private void waitForUserInfo() {
        UserService userService = new UserService(mRequestQueue, msharedPreferences);
        userService.get(() -> {
            User user = userService.getUser();
            editor = getSharedPreferences("SPOTIFY", 0).edit();
            editor.putString("userid", user.id);
            Log.d("STARTING", "GOT USER INFORMATION");
            // We use commit instead of apply because we need the information stored immediately
            editor.commit();
            startActivity2();
        });
    }

    private void startActivity2() {
        Intent newIntent = new Intent(MainActivity.this, Activity2.class);
        startActivity(newIntent);
    }

    @Override
    public void onClick(View v) {
        int value = Integer.parseInt(playerCount.getText().toString());
        if (v.getId()==plus.getId()&&value<6){
            value++;
        }
        else if (value>0){
            if (v.getId()==minus.getId()){
                value--;
            }
            else if (v.getId()==start.getId()){
                DatabaseHelper playerDb = new DatabaseHelper(MainActivity.this);

                for (int i=0;i<value;i++){
                    Bitmap icon = BitmapFactory.decodeResource(MainActivity.this.getResources(),
                            playerProfilesDefault[i]);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    icon.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] b = baos.toByteArray();
                    String encodedImageString = Base64.encodeToString(b, Base64.DEFAULT);

                    byte[] bytarray = Base64.decode(encodedImageString, Base64.DEFAULT);
                    playerDb.addPlayer(i,bytarray,"null","null","null");
                }

                builder.setScopes(new String[]{"streaming"});
                AuthorizationRequest request = builder.build();
                AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
                msharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
                mRequestQueue = Volley.newRequestQueue(this);
            }
        }
        playerCount.setText(Integer.toString(value));
    }
}