package com.android.talviewtask.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Songs {
    @SerializedName("songs")
    @Expose
    private List<SongsLists> songs = null;

    public List<SongsLists> getSongs() {
        return songs;
    }

    public void setSongs(List<SongsLists> songs) {
        this.songs = songs;
    }
}
