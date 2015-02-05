package com.eseo.streamnshare.model;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.eseo.streamnshare.R;

public class Song implements Parcelable{

	private long id;
	private String title;
	private String artist;
	private String album_name;
	private String album_internal_covertart ; 
	private String album_coverart;
	private long duration ;
	
	public Song(){}
	
	public Song(long songID, String songTitle, String songArtist){
		this.id=songID;
		this.title=songTitle;
		this.artist=songArtist;
	}
	
	public Song(long songID, String songTitle, String songArtist, String album_name, long duration, String album_internal_coverart, String album_coverart){
		this.id=songID;
		this.title=songTitle;
		this.artist=songArtist;
		this.duration = duration ;
		this.album_name = album_name;
		this.album_internal_covertart =album_internal_coverart;
		this.album_coverart = album_coverart;
	}
	
	public Bitmap getBitmap(Context c){
		Bitmap bitmap = null;
		try { 
			bitmap = MediaStore.Images.Media.getBitmap(c.getContentResolver(),Uri.parse(album_internal_covertart));
		} 
		catch (FileNotFoundException exception) {} 
		catch (IOException e) {}
		catch(NullPointerException e){}
		
		return bitmap ;
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
	
	
    public String getAlbum_name() {
		return album_name;
	}

	public String getAlbum_coverart() {
		return album_coverart;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public void setAlbum_name(String album_name) {
		this.album_name = album_name;
	}

	public void setAlbum_coverart(String album_coverart) {
		this.album_coverart = album_coverart;
	}

	public String getAlbum_internal_covertart() {
		return album_internal_covertart;
	}

	public void setAlbum_internal_covertart(String album_internal_covertart) {
		this.album_internal_covertart = album_internal_covertart;
	}

	protected Song(Parcel in) {
        id = in.readLong();
        title = in.readString();
        artist = in.readString();
        album_name = in.readString();
        album_internal_covertart = in.readString();
        album_coverart = in.readString();
        duration = in.readLong();
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
        dest.writeString(album_internal_covertart);
        dest.writeString(album_coverart);
        dest.writeLong(duration);
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
