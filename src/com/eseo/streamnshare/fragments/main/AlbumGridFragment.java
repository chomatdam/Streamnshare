package com.eseo.streamnshare.fragments.main;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.eseo.streamnshare.R;
import com.eseo.streamnshare.adapters.AlbumAdapter;
import com.eseo.streamnshare.managers.MusicManager;
import com.eseo.streamnshare.managers.Requests;
import com.eseo.streamnshare.model.Song;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class AlbumGridFragment extends Fragment implements OnItemClickListener{

	private HashMap<String,ArrayList<Song>> map;
	private ArrayList<String> albumViewList;
	private ArrayList<String> albumList;
	private ArrayList<Song> songList;
	private AlbumAdapter mAdapter;
	private int indexLoading = -1;

	public AlbumGridFragment(){
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		map = MusicManager.getInstance(getActivity()).getAlbumList();
		albumList = new ArrayList<String>();
		for (String artist : map.keySet())  
			albumList.add(artist);
		GridView grid = (GridView)getActivity().findViewById(R.id.album_grid);
		grid.setOnItemClickListener(this);

		albumViewList = new ArrayList<String>();
		mAdapter = new AlbumAdapter(getActivity(),albumViewList,this);
		grid.setAdapter(mAdapter);
		loadMore();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { 
		return inflater.inflate(R.layout.fragment_grid, container, false);
	}


	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
		String album = albumList.get(((GridView)v.getParent()).getPositionForView(v));
		songList = map.get(album);
		FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.fragment_placeholder, SongListFragment.newInstance(songList));
		ft.commit();
	}

	Future<JsonObject> loading;
	public boolean loadMore(){
		if(indexLoading==albumList.size()) return false;
		else if(indexLoading==-1){
			Song firstSongAlbum = map.get(albumList.get(0)).get(0);
			callCoverArt(firstSongAlbum);
			indexLoading=1;
			return true;
		}
		else{
			Song firstSongAlbum = map.get(albumList.get(indexLoading)).get(0);
			callCoverArt(firstSongAlbum);
			indexLoading++;
			return true;
		}
	}

	public void callCoverArt(Song firstSongAlbum){
		String request = Requests.getInstance().trackRequest(firstSongAlbum.getArtist(), firstSongAlbum.getTitle());

		if (loading != null && !loading.isDone() && !loading.isCancelled())
			return;

		loading  = Ion.with(this)
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
								mAdapter.add(coverArtURL);
								/* When you have updated your list: mAdapter.notifyDataSetChanged(); */
							}
							else mAdapter.add("");
						}catch (Exception ex) {
						}

					}
				});
	}

	public ArrayList<String> getAlbumList() {
		return albumList;
	}

	public HashMap<String, ArrayList<Song>> getMap() {
		return map;
	}



}
