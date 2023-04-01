package com.example.boppong_dev;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


import com.example.boppong_dev.Connectors.UserService;
import com.example.boppong_dev.Model.User;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class StartScreenActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String CLIENT_ID = "a24f9f02a4fc4adb8138143d99bd8dc9";
    private static final String REDIRECT_URI = "https://www.youtube.com/";
    protected RequestQueue mRequestQueue;
    private static final int REQUEST_CODE = 1337;
    private SharedPreferences.Editor editor;
    protected SharedPreferences msharedPreferences;
    protected AuthorizationRequest.Builder builder =
            new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);
    protected Button start;
    protected ImageButton settingsButton;
    ImageView minus,plus;
    protected TextView playerCount;
    int[] playerProfilesDefault = {R.drawable.testprofile1,R.drawable.testprofile2
            ,R.drawable.testprofile3,R.drawable.testprofile4,R.drawable.testprofile5,R.drawable.testprofile6};
    Dialog settings;
    int roundLimit, roundLength, tempRoundLimit, tempRoundLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen);

        //wipe database before creation of new lobby (prevents old users from existing)
        DatabaseHelper playerDb = new DatabaseHelper(StartScreenActivity.this);
        playerDb.wipe();

        settings = new Dialog(this);
        settingsButton = findViewById(R.id.settingsButton);

        getPrefs();

        //setting up view objects
        playerCount = findViewById(R.id.playerCountView);
        plus = findViewById(R.id.increaseGroupView);
        minus = findViewById(R.id.reduceGroupView);
        start = findViewById(R.id.startGame);
        plus.setOnClickListener(this);
        minus.setOnClickListener(this);
        start.setOnClickListener(this);

        playerCount.setText("0");
    }


    // spotify boiler plate code for establishing user to connect to spotify server for api usage
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

    //function called within onActivityResult to authenticate spotify user making use of api
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

    //Takes user to the lobby
    private void startActivity2() {
        Intent newIntent = new Intent(StartScreenActivity.this, LobbyActivity.class);
        ArrayList<String> prompts = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.prompts)));;
        Collections.shuffle(prompts);
        newIntent.putStringArrayListExtra("prompts",prompts);
        setPrefs(roundLimit,roundLength);
        Bundle b = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
        startActivity(newIntent,b);
    }

    //selecting how many players will be in the game and generating their objects and adding that to database
    //and opening up the settings dialog and saving those value to shared prefs
    @Override
    public void onClick(View v) {
        int value = Integer.parseInt(playerCount.getText().toString());
        if (v.getId()==settingsButton.getId()){
            tempRoundLimit = roundLimit == 3 ? 0 : (roundLimit == 15 ? 1 : 2);
            tempRoundLength = roundLength == 15 ? 0 : (roundLength == 30 ? 1 : 2);

            settings.setContentView(R.layout.settings);
            Button yes = settings.findViewById(R.id.yes);
            Button no = settings.findViewById(R.id.no);
            RadioGroup rounds = settings.findViewById(R.id.radioGroupRounds);
            RadioGroup length = settings.findViewById(R.id.radioGroupLength);

            int [] posRound = {settings.findViewById(R.id.button3).getId(),settings.findViewById(R.id.button6).getId(),
                    settings.findViewById(R.id.button12).getId()};
            int [] posLength = {settings.findViewById(R.id.button15).getId(),settings.findViewById(R.id.button30).getId(),
                    settings.findViewById(R.id.button45).getId()};

            rounds.check(posRound[tempRoundLimit]);
            length.check(posLength[tempRoundLength]);
            rounds.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == posRound[0]){
                        roundLimit = 3;
                    }
                    else if (checkedId == posRound[1]){
                        roundLimit = 6;
                    }
                    else if (checkedId == posRound[2]){
                        roundLimit = 12;
                    }
                }
            });
            length.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == posLength[0]){
                        roundLength= 15;
                    }
                    else if (checkedId == posLength[1]){
                        roundLength= 30;
                    }
                    else if (checkedId == posLength[2]){
                        roundLength= 45;
                    }
                }
            });
            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    settings.dismiss();
                }
            });
            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    settings.dismiss();
                }
            });
            settings.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            settings.show();
        }

        else if (v.getId()==plus.getId()&&value<6){
            value++;
        }
        else if (value>0){
            if (v.getId()==minus.getId()){
                value--;
            }
            else if (v.getId()==start.getId()){
                DatabaseHelper playerDb = new DatabaseHelper(StartScreenActivity.this);

                for (int i=0;i<value;i++){
                    //converting drawable into bitmap and then into byte[] to store in sql
                    Bitmap icon = BitmapFactory.decodeResource(StartScreenActivity.this.getResources(),
                            playerProfilesDefault[i]);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    icon.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] b = baos.toByteArray();
                    String encodedImageString = Base64.encodeToString(b, Base64.DEFAULT);

                    byte[] bytarray = Base64.decode(encodedImageString, Base64.DEFAULT);

                    //adding player object to database
                    try {
                        playerDb.addPlayer(i,bytarray,null,null,null);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                //initiates the spotify connection process that calls the functions above
                builder.setScopes(new String[]{"streaming"});
                AuthorizationRequest request = builder.build();
                AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
                msharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
                mRequestQueue = Volley.newRequestQueue(this);
            }
        }
        playerCount.setText(Integer.toString(value));
    }

    protected void setPrefs(int limit, int length){
        SharedPreferences sharedPrefs = getSharedPreferences("GAME",MODE_PRIVATE);
        SharedPreferences.Editor sharedEditor= sharedPrefs.edit();
        sharedEditor.putInt("roundLimit", limit);
        sharedEditor.putInt("roundLength", length);
        sharedEditor.commit();
    }

    protected void getPrefs(){
        SharedPreferences sharedPrefs = getSharedPreferences("GAME",MODE_PRIVATE);
        roundLimit = sharedPrefs.getInt("roundLimit",3);
        roundLength = sharedPrefs.getInt("roundLength",15);
    }
}