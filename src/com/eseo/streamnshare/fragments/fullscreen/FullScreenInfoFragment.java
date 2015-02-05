package com.eseo.streamnshare.fragments.fullscreen;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.eseo.streamnshare.R;
import com.eseo.streamnshare.managers.MusicManager;
import com.eseo.streamnshare.managers.Requests;
import com.eseo.streamnshare.model.Song;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;


public class FullScreenInfoFragment extends Fragment {

	private ImageView coverArtImageView ;
	private TextView titleTextView ;
	private TextView artistAndAlbumNameTextView ;
	
	public FullScreenInfoFragment(){
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_fullscreen_info, container, false);
		coverArtImageView = (ImageView) view.findViewById(R.id.cover_art_imageView);
		titleTextView = (TextView) view.findViewById(R.id.title_textView);
		artistAndAlbumNameTextView = (TextView) view.findViewById(R.id.artist_album_textView);
		return view ;
	}
	
	public void setSong(Song currentSong){
		Bitmap oldBitmap = coverArtImageView.getDrawingCache();
		if(oldBitmap != null){
			oldBitmap.recycle();
		}
		
		Bitmap bitmap = currentSong.getBitmap(getActivity());
		if(bitmap != null){
			coverArtImageView.setImageBitmap(bitmap);
		}
		else {
			MusicManager.getInstance(getActivity()).getCovertArt(currentSong,coverArtImageView);
		}
		titleTextView.setText(currentSong.getTitle());
		artistAndAlbumNameTextView.setText(currentSong.getArtist()+" - "+currentSong.getAlbum_name());
	}
	
	
	
}