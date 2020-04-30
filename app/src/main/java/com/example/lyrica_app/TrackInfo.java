package com.example.lyrica_app;

import androidx.annotation.NonNull;

public class TrackInfo {
    private String titleName, artistName;
    private int trackId;

    public TrackInfo(int track_id, String title_name, String artist_name){
        trackId = track_id;
        titleName = title_name;
        artistName = artist_name;
    }

    public TrackInfo(){

    }

    public int getTrackId() {
        return trackId;
    }

    public String getTitleName() {
        return titleName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

}
