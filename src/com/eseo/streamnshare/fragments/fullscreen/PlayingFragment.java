package com.eseo.streamnshare.fragments.fullscreen;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eseo.streamnshare.R;
import com.eseo.streamnshare.managers.MusicManager;
import com.eseo.streamnshare.model.Song;


public class PlayingFragment extends Fragment {
	
	private ArrayList<Song> songList;

	public PlayingFragment(){
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(songList == null)
			songList = MusicManager.getInstance(getActivity()).getSongList();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_fullscreen_playing, container, false);
	}

}
