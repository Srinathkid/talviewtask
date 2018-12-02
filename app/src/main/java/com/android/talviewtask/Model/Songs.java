package com.android.talviewtask.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Songs implements Parcelable {
    @SerializedName("songs")
    @Expose
    private List<SongsLists> songs = null;

    protected Songs(Parcel in) {
        songs = in.createTypedArrayList(SongsLists.CREATOR);
    }

    public static final Creator<Songs> CREATOR = new Creator<Songs>() {
        @Override
        public Songs createFromParcel(Parcel in) {
            return new Songs(in);
        }

        @Override
        public Songs[] newArray(int size) {
            return new Songs[size];
        }
    };

    public List<SongsLists> getSongs() {
        return songs;
    }

    public void setSongs(List<SongsLists> songs) {
        this.songs = songs;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(songs);
    }
}
