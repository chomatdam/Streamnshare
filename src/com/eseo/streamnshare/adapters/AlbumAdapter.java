package com.eseo.streamnshare.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eseo.streamnshare.R;
import com.eseo.streamnshare.fragments.main.AlbumGridFragment;
import com.koushikdutta.ion.Ion;

public class AlbumAdapter extends ArrayAdapter<String>{

	private AlbumGridFragment fragment;
	private ArrayList<String> albumList;
	private LayoutInflater songInf;
	private boolean hasNext = true;


	public AlbumAdapter(Context c, ArrayList<String> urlList, AlbumGridFragment fragment){
		super(c,0,urlList);
		this.fragment = fragment;
		this.albumList = fragment.getAlbumList();
		this.songInf = LayoutInflater.from(c);
	}

	static class ViewHolderItem {
		ImageView coverArtItem;
		TextView albumNameItem;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if(position > getCount() - 2 && hasNext)
			hasNext = fragment.loadMore();

		ViewHolderItem viewHolder;

		String keyAlbum = albumList.get(position);
		String URLCoverArt = getItem(position);

		if (convertView == null) {
			convertView = songInf.inflate(R.layout.album_grid_cell, parent, false);

			viewHolder = new ViewHolderItem();
			viewHolder.coverArtItem = (ImageView)convertView.findViewById(R.id.cover_art);
			viewHolder.albumNameItem = (TextView)convertView.findViewById(R.id.album_name); 

			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolderItem) convertView.getTag();
		}

		Ion.with(viewHolder.coverArtItem)
		.placeholder(R.drawable.error)
		.error(R.drawable.error)
		.load(URLCoverArt);

		viewHolder.albumNameItem.setText(keyAlbum);

		return convertView;
	}

}

