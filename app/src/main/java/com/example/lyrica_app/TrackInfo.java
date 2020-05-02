package com.example.lyrica_app;

public class TrackInfo {
    private String title, artist, lyrics, userId;
    private int id;

    public TrackInfo(int id, String title, String artist, String lyrics) {
        this.userId = "";
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.lyrics = lyrics;
    }


    public TrackInfo() {

    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getLyrics() {
        return lyrics;
    }

    public String getUserId() {
        return userId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
