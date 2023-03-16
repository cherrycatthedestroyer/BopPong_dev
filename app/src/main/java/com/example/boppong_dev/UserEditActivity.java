package com.example.boppong_dev;

import androidx.appcompat.app.AppCompatActivity;

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
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.boppong_dev.Connectors.DropDownAdapter;
import com.example.boppong_dev.Connectors.SongService;
import com.example.boppong_dev.Model.Song;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class UserEditActivity extends AppCompatActivity implements TextWatcher, AdapterView.OnItemClickListener {
    DatabaseHelper myDb;
    private SongService songService;
    AutoCompleteTextView songSub;
    ArrayList<Song> searchResults = new ArrayList<>();
    DropDownAdapter<String> adapter;
    TextView playerName;
    EditText editPlayerName;
    ImageView playerImage;
    int playerId;
    Song chosenSong;
    ImageView imageViewPhoto;
    private static final int img_id = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_edit_activity);

        //opens up sql database
        myDb = new DatabaseHelper(UserEditActivity.this);

        //variable that holds song selected from search
        chosenSong = new Song("","");

        //loading data from intent
        String inPlayerName = getIntent().getStringExtra("name");
        playerId = getIntent().getIntExtra("id",0);

        playerName = findViewById(R.id.playerNameTextView);
        playerImage = findViewById(R.id.selectedPlayerImageView);
        editPlayerName = findViewById(R.id.editNameTextView);
        imageViewPhoto = findViewById(R.id.selectedPlayerImageView);

        playerName.setText(inPlayerName);
        //setting the byte[] as bitmap image from the sql database by searching by id
        Cursor cursor = myDb.getProfileImage(Integer.toString(playerId));
        if (cursor.getCount()==0){
            Toast.makeText(UserEditActivity.this,"no data", Toast.LENGTH_SHORT).show();
        }
        else{
            while(cursor.moveToNext()){
                playerImage.setImageBitmap(Bytes2Bitmap(cursor.getBlob(0)));
            }
        }
        editPlayerName.setText(getIntent().getStringExtra("name"));

        songService = new SongService(getApplicationContext());

        songSub = findViewById(R.id.songSubEntry);
        songSub.addTextChangedListener(this);

        songSub.setText(getIntent().getStringExtra("song"));

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
    public void onSaveChanges(View view){
        String writePlayerName = playerName.getText().toString().trim();
        //only triggers if a song is selected
        if (chosenSong.getName().equals(songSub.getText().toString())){
            Intent intent = new Intent(this, LobbyActivity.class);
            if (editPlayerName.getText()!=null){
                writePlayerName = editPlayerName.getText().toString().trim();
            }

            //converting drawable into bitmap and then into byte[] to store in sql SOURCED

            Bitmap icon = ((BitmapDrawable)playerImage.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            icon.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] b = baos.toByteArray();
            String encodedImageString = Base64.encodeToString(b, Base64.DEFAULT);

            byte[] bytarray = Base64.decode(encodedImageString, Base64.DEFAULT);

            myDb.updateData(Integer.toString(playerId),writePlayerName,chosenSong.getName(),chosenSong.getId(),chosenSong.getArtist(),bytarray);
            startActivity(intent);
        }
        else{
            Toast.makeText(this,"please select  a song", Toast.LENGTH_SHORT).show();
            songSub.requestFocus();
        }
    }

    //selects song from dropdown search options
    public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
        Song selection = (Song) parent.getItemAtPosition(position);
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
        imageViewPhoto.setImageBitmap(photo);
        playerImage.setImageBitmap(photo);
    }

    //SOURCED function to convert bytes to bitmap
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