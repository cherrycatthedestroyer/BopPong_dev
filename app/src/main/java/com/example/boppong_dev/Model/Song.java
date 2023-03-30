package com.example.boppong_dev.Model;

import java.lang.reflect.Array;
import java.sql.Struct;
import java.util.ArrayList;

public class Song implements java.io.Serializable{
    private String uri;
    private String name;
    private ArrayList<Artist> artists;

    public Song(String id, String name) {
        this.name = name;
        if (id != null) {
            this.uri = id.replace("spotify:track:","");
        }
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

    @Override
    public String toString() {
        return "Song{" +
                "name='" + name + '\'' +
                ", uri='" + uri + '\'' +
                convertArraytoString() +
                '}';
    }

    public String convertArraytoString(){
        String start = ", artists=[";

        start += artists.get(0).toString();
        if (artists.size()>1){
            for (int i=1;i<artists.size();i++){
                start += ", "+artists.get(i).toString();
            }
        }

        start+="]";

        return start;
    }
}
