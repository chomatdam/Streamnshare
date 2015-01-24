package com.eseo.backup.deadcode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.eseo.streamnshare.managers.Requests;
import com.eseo.streamnshare.model.Song;
import com.google.gson.JsonObject;
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
		Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI; //define URI
		Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null); // result

		if(musicCursor!=null && musicCursor.moveToFirst()){
			//récup colonnes
			int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
			int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
			int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
			int albumNameColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM);
			// remplissage liste
			do{
				long thisId = musicCursor.getLong(idColumn);
				String thisTitle = musicCursor.getString(titleColumn);
				String thisArtist = musicCursor.getString(artistColumn);
				String thisAlbumName = musicCursor.getString(albumNameColumn);
				songList.add(new Song(thisId, thisTitle, thisArtist, thisAlbumName, null));

			}while(musicCursor.moveToNext());

		}

		return songList;
	}

	public ArrayList<Song> initDetailedSongList(){
		ArrayList<Song> songList = new ArrayList<Song>();
		Requests ws = Requests.getInstance();

		//Resolver: provide access to Content Provider (We ask to Provider a result(data) as a client)
		ContentResolver musicResolver = context.getContentResolver();
		Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI; //define URI
		Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null); // result

		if(musicCursor!=null && musicCursor.moveToFirst()){
			//récup colonnes
			int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
			int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
			int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
			int albumNameColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM);
			// remplissage liste
			do{
				long thisId = musicCursor.getLong(idColumn);
				String thisTitle = musicCursor.getString(titleColumn);
				String thisArtist = musicCursor.getString(artistColumn);
				String thisAlbumName = musicCursor.getString(albumNameColumn);

				try {
					String request = ws.trackRequest(thisArtist, thisTitle);

					JsonObject json = Ion.with(context)
							.load(request)
							.asJsonObject()
							.get();
					int statusCode = json.getAsJsonObject("message").getAsJsonObject("header").get("status_code").getAsInt();
					if(statusCode == 200){
						JsonObject track = json.getAsJsonObject("message").getAsJsonObject("body").getAsJsonObject("track");
						String albumName = track.get("album_name").getAsString();
						String covertArtURL = track.get("album_coverart_350x350").getAsString();
						songList.add(new Song(thisId, thisTitle, thisArtist, albumName, covertArtURL));
					}
					else
						songList.add(new Song(thisId, thisTitle, thisArtist, thisAlbumName, null));

				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}

			}while(musicCursor.moveToNext());

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

	}
