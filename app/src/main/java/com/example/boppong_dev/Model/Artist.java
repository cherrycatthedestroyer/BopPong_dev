package com.example.boppong_dev.Model;

public class Artist implements java.io.Serializable{
    String name;

    public Artist(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return "Artist{" +
                "name='" + name + '\'' +
                '}';
    }
}
