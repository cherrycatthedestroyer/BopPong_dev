package com.example.boppong_dev.Model;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class Player {
    int id;
    Bitmap image;
    String name;
    Song songSubmission;

    public Player(int id, String name, Song songSubmission, Bitmap image) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.songSubmission = songSubmission;
    }

    public int getId() {
        return id;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public Song getSongSubmission() {
        return songSubmission;
    }

    public void setSongSubmission(Song songSubmission) {
        this.songSubmission = songSubmission;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public void setName(String in_name){
        this.name = in_name;
    }
}
