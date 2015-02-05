package com.eseo.streamnshare.fragments.main;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.eseo.streamnshare.R;
import com.eseo.streamnshare.adapters.SongAdapter;
import com.eseo.streamnshare.managers.MusicManager;
import com.eseo.streamnshare.model.Song;

/* Fragments: https://github.com/codepath/android_guides/wiki/Creating-and-Using-Fragments */
public class SongListFragment extends ListFragment {

	private OnItemSelectedFragmentListener listener;
	private ArrayList<Song> songList;

	public SongListFragment(){
	}

	public static SongListFragment newInstance(ArrayList<Song> songList) {
		SongListFragment fragment = new SongListFragment();
		fragment.songList = songList;
		return fragment;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(songList == null)
			songList = MusicManager.getInstance(getActivity()).getSongList();
		
		SongAdapter adapter = new SongAdapter(getActivity(),songList);
		setListAdapter(adapter);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// This makes sure that the container activity has implemented
		try {
			listener = (OnItemSelectedFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnItemSelectedListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_list, container, false);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		listener.onSongItemSelected(l.getPositionForView(v));
	}
	
	public ArrayList<Song> getSongList(){
		return songList;
	}

}
