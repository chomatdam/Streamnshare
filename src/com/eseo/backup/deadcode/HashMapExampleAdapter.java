package com.eseo.backup.deadcode;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eseo.streamnshare.R;
import com.eseo.streamnshare.model.Song;
import com.koushikdutta.ion.Ion;

public class HashMapExampleAdapter extends BaseAdapter{

	private HashMap<String,ArrayList<Song>> mData;
	private String[] mKeys;
	private LayoutInflater songInf;


	public HashMapExampleAdapter(Context c, HashMap<String,ArrayList<Song>> albumList){
		mData=albumList;
		mKeys=mData.keySet().toArray(new String[mData.size()]);
		songInf = LayoutInflater.from(c);
	}

	static class ViewHolderItem {
		ImageView coverArtItem;
		TextView albumNameItem;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public ArrayList<Song> getItem(int position) {
		return mData.get(mKeys[position]);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolderItem viewHolder;

		String keyAlbum = mKeys[position];
		Song firstSong = getItem(position).get(0);

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
		.load(firstSong.getCoverArt());

		viewHolder.albumNameItem.setText(keyAlbum);

		return convertView;
	}
}

