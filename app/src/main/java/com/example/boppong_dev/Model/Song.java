package com.example.boppong_dev.Model;

import java.lang.reflect.Array;
import java.sql.Struct;
import java.util.ArrayList;

public class Song {
    private String uri;
    private String name;
    private ArrayList<Artist> artists;

    public Song(String id, String name) {
        this.name = name;
        this.uri = id.replace("spotify:track:","");
        artists = new ArrayList<Artist>();
    }

    public void setArtists(ArrayList<Artist> inArray){
        artists=inArray;
    }

    public String getArtist(){
        return artists.get(0).getName();
    }

    public String getId() {
        return uri;
    }

    public void setId(String id) {
        this.uri = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
