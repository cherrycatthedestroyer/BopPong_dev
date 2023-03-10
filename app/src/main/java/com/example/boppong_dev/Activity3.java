package com.example.boppong_dev;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.boppong_dev.Connectors.KArrayAdapter;
import com.example.boppong_dev.Connectors.SongService;
import com.example.boppong_dev.Model.Player;
import com.example.boppong_dev.Model.Song;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Activity3 extends AppCompatActivity implements TextWatcher, AdapterView.OnItemClickListener {
    DatabaseHelper myDb;
    int[] playerProfilesDefault = {R.drawable.testprofile1,R.drawable.testprofile2
            ,R.drawable.testprofile3,R.drawable.testprofile4,R.drawable.testprofile5,R.drawable.testprofile6};
    private SongService songService;
    AutoCompleteTextView songSub;
    ArrayList<Song> searchResults = new ArrayList<>();
    KArrayAdapter<String> adapter;
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
        setContentView(R.layout.activity_3);

        chosenSong = new Song("","");

        String inPlayerName = getIntent().getStringExtra("name");
        //int inPlayerImageIndex = getIntent().getByteExtra("image",new Byte(Byte.MIN_VALUE));
        playerId = getIntent().getIntExtra("id",0);

        playerName = findViewById(R.id.playerNameTextView);
        playerImage = findViewById(R.id.selectedPlayerImageView);
        editPlayerName = findViewById(R.id.editNameTextView);
        imageViewPhoto = findViewById(R.id.selectedPlayerImageView);

        playerName.setText(inPlayerName);
        playerImage.setImageBitmap(getIntent().getParcelableExtra("image"));
        editPlayerName.setText(getIntent().getStringExtra("name"));

        songService = new SongService(getApplicationContext());

        songSub = findViewById(R.id.songSubEntry);
        songSub.addTextChangedListener(this);

        songSub.setText(getIntent().getStringExtra("song"));

        adapter = new KArrayAdapter<String>(this, android.R.layout.simple_list_item_1, searchResults);
        songSub.setAdapter(adapter);
        songSub.setThreshold(3);
        songSub.setOnItemClickListener(this);
        getTracks();

        myDb = new DatabaseHelper(Activity3.this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s){
        if (songSub.isFocused()){
            if (s.length()>3){
                getTracks();
            }
        }
    }

    private void getTracks() {
            songService.setInputSong(songSub.getText().toString());
            songService.searchTrack(() ->  {
                if (searchResults.size()>0){
                    searchResults.clear();
                }
                for (int i=0; i<songService.getSongs().size();i++){
                    searchResults.add(songService.getSongs().get(i));
                }
                songService.clearSongs();
            });
            songSub.setAdapter(adapter);
    }

    public void onSaveChanges(View view){
        if (chosenSong.getName().equals(songSub.getText().toString())){
            Intent intent = new Intent(this,Activity2.class);

            String writePlayerName = playerName.getText().toString().trim();
            if (editPlayerName.getText()!=null){
                writePlayerName = editPlayerName.getText().toString().trim();
            }

            Bitmap icon = BitmapFactory.decodeResource(Activity3.this.getResources(), playerImage.getId());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            icon.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] b = baos.toByteArray();
            String encodedImageString = Base64.encodeToString(b, Base64.DEFAULT);

            byte[] bytarray = Base64.decode(encodedImageString, Base64.DEFAULT);

            myDb.updateData(Integer.toString(playerId),writePlayerName,chosenSong.getName(),chosenSong.getId(),chosenSong.getArtist(),bytarray);
            startActivity(intent);
        }
        else{
            songSub.requestFocus();
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
        Song selection = (Song) parent.getItemAtPosition(position);
        chosenSong = selection;
    }

    public void goToCamera (View view){
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera_intent, img_id);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap photo = (Bitmap) data.getExtras().get("data");
        imageViewPhoto.setImageBitmap(photo);
        playerImage.setImageBitmap(photo);
    }


}