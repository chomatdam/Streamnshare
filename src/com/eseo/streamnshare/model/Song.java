package com.eseo.streamnshare.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable{

	private long id;
	private String title;
	private String artist;
	private String album_name;
	private String album_coverart;
	
	
	public Song(long songID, String songTitle, String songArtist){
		this.id=songID;
		this.title=songTitle;
		this.artist=songArtist;
	}
	
	public Song(long songID, String songTitle, String songArtist, String album_name, String album_coverart){
		this.id=songID;
		this.title=songTitle;
		this.artist=songArtist;
		this.album_name = album_name;
		this.album_coverart = album_coverart;
	}

	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getArtist() {
		return artist;
	}
	
	public String getAlbumName() {
		return album_name;
	}

	public String getCoverArt() {
		return album_coverart;
	}
	
	
    public void setAlbum_name(String album_name) {
		this.album_name = album_name;
	}

	public void setAlbum_coverart(String album_coverart) {
		this.album_coverart = album_coverart;
	}
	
	/* Parcelling part
	 * http://stackoverflow.com/questions/7181526/how-can-i-make-my-custom-objects-be-parcelable
	 */ 

	protected Song(Parcel in) {
        id = in.readLong();
        title = in.readString();
        artist = in.readString();
        album_name = in.readString();
        album_coverart = in.readString();
    }

	@Override
	public int describeContents() {
		return 0;
	}

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(album_name);
        dest.writeString(album_coverart);
    }
    
    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
	
	
}
