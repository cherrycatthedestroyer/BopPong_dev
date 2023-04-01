package com.example.boppong_dev;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.boppong_dev.Connectors.DropDownAdapter;
import com.example.boppong_dev.Connectors.Serializer;
import com.example.boppong_dev.Connectors.SongService;
import com.example.boppong_dev.Model.Player;
import com.example.boppong_dev.Model.Song;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class UserEditActivity extends AppCompatActivity implements TextWatcher, AdapterView.OnItemClickListener {
    DatabaseHelper myDb;
    private SongService songService;
    Player currentPlayer;
    AutoCompleteTextView songSub;
    ArrayList<Song> searchResults = new ArrayList<>();
    DropDownAdapter<String> adapter;
    TextView playerName;
    EditText editPlayerName;
    ImageView playerImage;
    int playerId, currentRound;
    Song chosenSong;
    private static final int img_id = 123;
    private ArrayList<String> prompts;
    private InputMethodManager imm;
    private boolean isKeyboardEnabled = true;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_edit_activity);

        currentRound = getIntent().getIntExtra("round",0);
        //opens up sql database
        myDb = new DatabaseHelper(UserEditActivity.this);

        //variable that holds song selected from search
        chosenSong = new Song("","");

        //loading data from intent
        playerId = getIntent().getIntExtra("id",0);
        prompts = getIntent().getStringArrayListExtra("prompts");

        try {
            currentPlayer = fetchPlayer(playerId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        playerName = findViewById(R.id.playerNameTextView);
        playerImage = findViewById(R.id.selectedPlayerImageView);
        editPlayerName = findViewById(R.id.editNameTextView);

        playerName.setText(currentPlayer.getName());
        editPlayerName.setText(currentPlayer.getName());
        playerImage.setImageBitmap(currentPlayer.getImage());

        if (currentPlayer.getSongSubmission().getId()!=null){
            chosenSong=currentPlayer.getSongSubmission();
            songSub.setText(chosenSong.getName());
        }

        songService = new SongService(getApplicationContext());

        songSub = findViewById(R.id.songSubEntry);
        songSub.addTextChangedListener(this);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        songSub.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (!isKeyboardEnabled) {
                        imm.showSoftInput(songSub, InputMethodManager.SHOW_IMPLICIT);
                    }
                    isKeyboardEnabled = true;
                }
                return false;
            }
        });

        //custom dropdown box that requires custom view to inflate results with
        adapter = new DropDownAdapter<String>(this, android.R.layout.simple_list_item_1, searchResults);
        songSub.setAdapter(adapter);
        //how many characters type before entrybox prompt is used for song search
        songSub.setThreshold(3);
        songSub.setOnItemClickListener(this);
        //song search
        getTracks();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    //triggering the search function when 3 characters or more are entered in song search textbox
    @Override
    public void afterTextChanged(Editable s){
        if (songSub.isFocused()){
            if (s.length()>3){
                getTracks();
            }
        }
    }

    //search for songs based on text entry prompt
    private void getTracks() {
            //prompt sent to song search class
            songService.setInputSong(songSub.getText().toString());
            //song search results returns an array of song objects that match the text entry prompt
            songService.searchTrack(() ->  {
                if (searchResults.size()>0){
                    searchResults.clear();
                    adapter.notifyDataSetChanged();
                }
                for (int i=0; i<songService.getSongs().size();i++){
                    searchResults.add(songService.getSongs().get(i));
                }
                //clears search results, ready for next search if it happens so it can happy like autocorrect
                songService.clearSongs();
            });
            //dropdown box updated with search results
            songSub.setAdapter(adapter);
    }

    //updating edited player data to database then sends them back to the lobby screen
    public void onSaveChanges(View view) throws Exception {
        //only triggers if a song is selected
        if (chosenSong.getName().equals(songSub.getText().toString())&&editPlayerName.getText().toString()!=null&&chosenSong.getName()!=""){
            Intent intent = new Intent(this, LobbyActivity.class);
            if (editPlayerName.getText()!=null){
                currentPlayer.setName(editPlayerName.getText().toString().trim());
            }

            currentPlayer.setSongSubmission(chosenSong);

            myDb.updateData(Integer.toString(currentPlayer.getId()),
                    currentPlayer.getName(),
                    chosenSong.getName(),
                    chosenSong.getId(),
                    chosenSong.getArtist(),
                    Serializer.serializeBitmap(currentPlayer.getImage()));

            intent.putStringArrayListExtra("prompts",prompts);
            intent.putExtra("round", currentRound);
            startActivity(intent);
        }
        else{
            if (!chosenSong.getName().equals(songSub.getText().toString())) {
                songSub.setText("");
                songSub.requestFocus();
            }
            else{
                editPlayerName.requestFocus();
            }
        }
    }

    //selects song from dropdown search options
    public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
        Song selection = (Song) parent.getSelectedItem();
        chosenSong = selection;
    }

    //intent redirects user to camera
    public void goToCamera (View view){
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera_intent, img_id);
    }

    //sets picture from camera as the player image
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap photo = (Bitmap) data.getExtras().get("data");
        playerImage.setImageBitmap(photo);
        currentPlayer.setImage(photo);
    }

    protected Player fetchPlayer(int in_id) throws Exception {
        Cursor cursor = myDb.getPlayer(Integer.toString(in_id));
        Player temp_player= new Player(-1,null,null,null);
        if (cursor.getCount()==0){
            Toast.makeText(UserEditActivity.this,"no data", Toast.LENGTH_SHORT).show();
        }
        else{
            while(cursor.moveToNext()){
                temp_player = new Player(
                        Integer.parseInt(cursor.getString(0)),cursor.getString(1)
                        ,new Song(cursor.getString(3),cursor.getString(4)),
                        Serializer.deserializeBitmap(cursor.getBlob(2)));
            }
        }
        return temp_player;
    }
    public void onBackPressed() {
        Intent intent = new Intent(this, LobbyActivity.class);
        intent.putStringArrayListExtra("prompts",prompts);
        startActivity(intent);
    }
}