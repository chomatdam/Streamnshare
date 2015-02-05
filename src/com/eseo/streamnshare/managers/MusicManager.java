package com.eseo.streamnshare.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.eseo.streamnshare.R;
import com.eseo.streamnshare.model.Song;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class MusicManager {

	private static MusicManager instance;
	private Context context;
	private ArrayList<Song> songList;


	protected MusicManager(Context c){
		context = c;
		songList = initSongList();
	}

	/* Pourquoi Application context ? http://possiblemobile.com/2013/06/context */
	public static MusicManager getInstance(Context c){
		if(instance == null){
			instance = new MusicManager(c.getApplicationContext());
		}
		return instance;
	}

	public ArrayList<Song> initSongList(){
		ArrayList<Song> songList = new ArrayList<Song>();

		//Resolver: provide access to Content Provider (We ask to Provider a result(data) as a client)
		ContentResolver musicResolver = context.getContentResolver();
		Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI; //define URI
		Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null); // result

		while(musicCursor.moveToNext()){
			long id = musicCursor.getLong(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
			String title = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
			String artist = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
			String albumName = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
			long albumId = musicCursor.getLong(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
			long duration = musicCursor.getLong(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
			Uri externalCovertArtUri = Uri.parse("content://media/external/audio/albumart");
            Uri albumArtUri = ContentUris.withAppendedId(externalCovertArtUri, albumId);
			songList.add(new Song(id, title, artist, albumName,duration,albumArtUri.toString(),null));

		}

		return songList;
	}

		public ArrayList<Song> getSongList() {
			return songList;
		}

		public HashMap<String,ArrayList<Song>> getAlbumList(){
			HashMap<String,ArrayList<Song>> map = new HashMap<String,ArrayList<Song>>();
			ArrayList<Song> songAlbumList;
			for(Song currentSong : songList){
				String albumName = currentSong.getAlbumName();
				if(!map.containsKey(albumName))
				{
					songAlbumList = new ArrayList<Song>();
					songAlbumList.add(currentSong);
					map.put(albumName, songAlbumList);
				}
				else{
					songAlbumList = map.get(albumName);
					songAlbumList.add(currentSong);
				}
			}
			return map;
		}

		public HashMap<String,ArrayList<Song>> getArtistList(){
			HashMap<String,ArrayList<Song>> map = new HashMap<String,ArrayList<Song>>();
			ArrayList<Song> songArtistList;
			for(Song currentSong : songList){
				String artist = currentSong.getArtist();
				if(!map.containsKey(artist))
				{
					songArtistList = new ArrayList<Song>();
					songArtistList.add(currentSong);
					map.put(artist, songArtistList);
				}
				else{
					songArtistList = map.get(artist);
					songArtistList.add(currentSong);
				}
			}
			return map;
		}

		public ArrayList<String> sortByArtist(ArrayList<String> list){
			Collections.sort(list, new Comparator<String>(){
				public int compare(String a, String b){
					return a.compareTo(b);
				}
			});
			return list;
		}

		public void sortByTitle(){
			Collections.sort(songList, new Comparator<Song>(){
				public int compare(Song a, Song b){
					return a.getTitle().compareTo(b.getTitle());
				}
			});
		}
		
		public void getCovertArt(Song currentSong, final ImageView imageView){
			String request = Requests.getInstance().trackRequest(currentSong.getArtist(), currentSong.getTitle());

			Ion.with(context)
					.load(request)
					.asJsonObject()
					.setCallback(new FutureCallback<JsonObject>() {
						@Override
						public void onCompleted(Exception e, JsonObject result) {
							try {
								if (e != null)
									throw e;
								// find the results and populate
								int statusCode = result.getAsJsonObject("message").getAsJsonObject("header").get("status_code").getAsInt();
								if(statusCode == 200){
									JsonObject track = result.getAsJsonObject("message").getAsJsonObject("body").getAsJsonObject("track");
									String coverArtURL = track.get("album_coverart_350x350").getAsString();
									Ion.with(imageView)
									.placeholder(R.drawable.error)
									.error(R.drawable.error)
									.load(coverArtURL);
								}

							}catch (Exception ex) {
							}

						}
					});
		}
		
		
		
		

	}
