package com.example.boppong_dev.Model;

public class Player {
    int id;
    int image;
    String name;
    Song songSubmission;

    public Player(int id, String name, Song songSubmission, int image) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.songSubmission = songSubmission;
    }

    public int getId() {
        return id;
    }

    public int getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public Song getSongSubmission() {
        return songSubmission;
    }

    public void setSongSubmission(Song newSub){
        songSubmission = newSub;
    }
}
