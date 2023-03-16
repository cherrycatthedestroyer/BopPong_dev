package com.example.boppong_dev;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.nfc.Tag;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.database.sqlite.SQLiteDatabaseKt;

public class DatabaseHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME= "bpDevData.db";
    private static int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "players";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_IMAGE = "image";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_SONG_ID = "song_id";
    private static final String COLUMN_SONG_NAME = "song_name";
    private static final String COLUMN_SONG_ARTIST = "song_artist";


    DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " STRING, "
                + COLUMN_IMAGE + " BLOB, "
                + COLUMN_SONG_ID + " STRING, "
                + COLUMN_SONG_NAME + " STRING, "
                + COLUMN_SONG_ARTIST + " STRING);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int il) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    //adds player to database with all its information
    void addPlayer(int playerIndex, byte[] playerImage, String playerSongId, String playerSong, String playerSongArtist){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_NAME,"player "+playerIndex);
        cv.put(COLUMN_IMAGE,playerImage);
        cv.put(COLUMN_SONG_ID,playerSongId);
        cv.put(COLUMN_SONG_NAME,playerSong);
        cv.put(COLUMN_SONG_ARTIST,playerSongArtist);
        long result = db.insert(TABLE_NAME,null,cv);
        if (result==-1){
            Toast.makeText(context,"add failed", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(context,"success", Toast.LENGTH_SHORT).show();
        }
    }

    //clears table of player data
    protected void wipe(){
        getWritableDatabase().execSQL("delete from "+ TABLE_NAME);
    }

    //retrieves all player data in table
    protected Cursor readAllData(){
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        if (db != null){
            cursor= db.rawQuery(query, null);
        }
        return cursor;
    }

    //updates edited player data
    void updateData(String row_id, String playerName, String playerSong, String playerSongId, String playerSongArtist, byte[] playerImage){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_NAME,playerName);
        cv.put(COLUMN_IMAGE,playerImage);
        cv.put(COLUMN_SONG_ID,playerSongId);
        cv.put(COLUMN_SONG_NAME,playerSong);
        cv.put(COLUMN_SONG_ARTIST,playerSongArtist);

        long result = db.update(TABLE_NAME,cv,"id=?",new String[]{row_id});
        if (result==-1){
            //Toast.makeText(context,"update failed", Toast.LENGTH_SHORT).show();
        }
        else{
            //Toast.makeText(context,"success", Toast.LENGTH_SHORT).show();
        }
    }

    // theres alot of layers in the song
    //how many?
    //its very prince, if he llived in a cyber punk city
    //whats the game?
    //risk of rain 2
    //is there electric guitar yet?
    //oh yeah it came and went, we're on the second lull

    //retrieves player image based on id
    Cursor getProfileImage(String row_id){
        String query = "SELECT image FROM " + TABLE_NAME + " WHERE id = ? LIMIT 1";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        if (db != null){
            cursor= db.rawQuery(query,new String[]{row_id});
        }
        return cursor;
    }
}
