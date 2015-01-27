package com.eseo.streamnshare.activities.fullscreen;

import java.util.ArrayList;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.eseo.streamnshare.R;
import com.eseo.streamnshare.activities.MusicService;
import com.eseo.streamnshare.activities.MusicService.MusicBinder;
import com.eseo.streamnshare.fragments.fullscreen.PlayingFragment;
import com.eseo.streamnshare.model.Song;

public class FullScreenActivity extends FragmentActivity{

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen);
		final ActionBar actionBar = getActionBar();
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC); 
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		/* Custom action bar */
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));


		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		simplePlayingMode(ft);

	}
	
	public void simplePlayingMode(FragmentTransaction ft){
		PlayingFragment simplePlayingFragment = new PlayingFragment();
		Bundle b = getIntent().getExtras();
		simplePlayingFragment.setArguments(b);
		ft.replace(R.id.fullscreen_fragment_placeholder, simplePlayingFragment);
		ft.commit();
	}
	
	
	/*
	 * Menu items
	 */
/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.fullscreen, menu);
		return super.onCreateOptionsMenu(menu);
	}
	*/
/*
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		//menu item selected
		switch(item.getItemId()){
		case R.id.action_shuffle:
			//shuffle
			//musicSrv.setShuffle();
			updateShuffleMenuItem(item);
			return true;
		case R.id.action_end:
			//stopService(playIntent);
			//musicSrv=null;
			System.exit(0);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
*/

}
