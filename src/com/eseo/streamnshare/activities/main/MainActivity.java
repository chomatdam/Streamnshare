package com.eseo.streamnshare.activities.main;

import java.util.ArrayList;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.eseo.streamnshare.R;
import com.eseo.streamnshare.activities.fullscreen.FullScreenActivity;
import com.eseo.streamnshare.fragments.main.AlbumGridFragment;
import com.eseo.streamnshare.fragments.main.ArtistListFragment;
import com.eseo.streamnshare.fragments.main.MainControllerFragment;
import com.eseo.streamnshare.fragments.main.ModeChoiceFragment;
import com.eseo.streamnshare.fragments.main.OnItemSelectedFragmentListener;
import com.eseo.streamnshare.fragments.main.SongListFragment;
import com.eseo.streamnshare.managers.MusicManager;
import com.eseo.streamnshare.model.Song;
import com.eseo.streamnshare.services.MusicService;
import com.eseo.streamnshare.services.MusicService.MusicBinder;
import com.eseo.streamnshare.services.NormalService;
import com.eseo.streamnshare.services.SinkService;
import com.eseo.streamnshare.services.SourceService;
import com.eseo.streamnshare.utils.StateUtils;

public class MainActivity extends FragmentActivity implements OnItemSelectedFragmentListener,OnModeChangeListener{

	private String[] navDrawerItems;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private MusicService musicService ;

	private PlayMode playMode = PlayMode.NORMAL ;

	////////////////////////////////////////////////////////////////////////
	//CYCLE DE VIE
	////////////////////////////////////////////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final ActionBar actionBar = getActionBar();
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC); 

		initActionBarDrawer();

		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setTitle("Titres");
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));

		/* Navigation drawer */
		navDrawerItems = getResources().getStringArray(R.array.nav_drawer_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerList.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_list_item, navDrawerItems));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());


		/* Fragments */
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.fragment_placeholder, new SongListFragment());
		ft.commit();

		if(!StateUtils.loadState(this)){
			startService(new Intent(this,NormalService.class));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		playMode = StateUtils.loadMode(this);
		switch(playMode){
		case NORMAL:
			bindService(new Intent(this,NormalService.class),musicConnection,0);
			break;
		case SOURCE :
			bindService(new Intent(this,SourceService.class),musicConnection,0);
		case SINK:
			bindService(new Intent(this,SinkService.class),musicConnection,0);
			break;
		}
		ModeChoiceFragment modeChoiceFragment = (ModeChoiceFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_mode);
		modeChoiceFragment.updateView(playMode);
	}

	@Override
	protected void onPause() {
		super.onPause();
		StateUtils.saveMode(this,playMode);
		unbindService(musicConnection);
		musicService = null ;
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu) ;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		//menu item selected
		switch(item.getItemId()){
		case R.id.action_search:
			return true;
		case R.id.action_end:
			switch(playMode){
			case NORMAL:
				stopService(new Intent(this,NormalService.class));
				break;
			case SOURCE :
				stopService(new Intent(this,SourceService.class));
				break;
			case SINK:
				stopService(new Intent(this,SinkService.class));
				break;
			}
			playMode = PlayMode.NORMAL ;
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}


	////////////////////////////////////////////////////////////////////////
	//SERVICE CONNECTIONS
	////////////////////////////////////////////////////////////////////////

	private ServiceConnection musicConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service){
			MusicBinder binder = (MusicBinder)service;
			musicService = binder.getService();
			
			if(!(musicService instanceof SinkService)){
				Song currentSong = musicService.getCurrentSong();
				if(currentSong == null){
					SongListFragment songListFragment = (SongListFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder);
					ArrayList<Song> songList = songListFragment.getSongList();
					musicService.setList(songList);
					musicService.setCurrentSong(0);
					currentSong = musicService.getCurrentSong();
				}
				MainControllerFragment mainControllerFragment = (MainControllerFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_controller);
				mainControllerFragment.setSong(currentSong);
				mainControllerFragment.setPlaying(musicService.isPlaying());	
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name){
		}
	};

	////////////////////////////////////////////////////////////////////////
	//Drawer Management
	/////////////////////////////////////////////////////////////////////////

	public void initActionBarDrawer(){
		/* ActionBar Drawer */
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(
				this,                  /* host Activity */
				mDrawerLayout,         /* DrawerLayout object */
				R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
				R.string.drawer_open,  /* "open drawer" description */
				R.string.drawer_close  /* "close drawer" description */
				) {

		};
	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
		}
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		String choix = getResources().getStringArray(R.array.nav_drawer_array)[position];
		if(choix.equals("Artistes")){
			getActionBar().setTitle("Artistes");
			ft.replace(R.id.fragment_placeholder, new ArtistListFragment());
			ft.commit();
		}
		else if(choix.equals("Titres")){
			getActionBar().setTitle("Titres");
			MusicManager.getInstance(this).sortByTitle();
			ft.replace(R.id.fragment_placeholder, new SongListFragment());
			ft.commit();
		}
		else if(choix.equals("Albums")){
			getActionBar().setTitle("Albums");
			ft.replace(R.id.fragment_placeholder, new AlbumGridFragment());
			ft.commit();
		}

		// Highlight the selected item, update the title, and close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(navDrawerItems[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}


	///////////////////////////////////////////////////////////////////////////////
	// LISTENER
	///////////////////////////////////////////////////////////////////////////////

	@Override
	public void onSongItemSelected(int position) {
		SongListFragment songListFragment = (SongListFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder);
		ArrayList<Song> songList = songListFragment.getSongList();

		if(musicService != null){

			if(musicService.isPlaying()){
				musicService.pausePlayer();
			}

			musicService.setList(songList);
			musicService.setCurrentSong(position);
			musicService.playSong();
		}
	}


	///////////////////////////////////////////////////////////////////////////////
	// OTHERS
	///////////////////////////////////////////////////////////////////////////////
	public PlayMode getPlayMode() {
		return playMode;
	}

	public void setPlayMode(PlayMode playMode) {
		this.playMode = playMode;

		if(playMode == PlayMode.SINK){
			Intent intent = new Intent(this, FullScreenActivity.class);
			Bundle b = new Bundle();
			b.putSerializable("mode",playMode);
			intent.putExtras(b);
			startActivity(intent);
		}

	}


	@Override
	public void onModeChange(PlayMode lastMode) {
		unbindService(musicConnection);
		switch(lastMode){
		case NORMAL:
			stopService(new Intent(this,NormalService.class));
			break;
		case SINK:
			stopService(new Intent(this,SinkService.class));
			break;
		case SOURCE:
			stopService(new Intent(this,SourceService.class));
			break;
		}

		switch(playMode){
		case NORMAL:
			startService(new Intent(this,NormalService.class));
			bindService(new Intent(this,NormalService.class),musicConnection,0);
			break;
		case SINK:
			startService(new Intent(this,SinkService.class));
			bindService(new Intent(this,SinkService.class),musicConnection,0);
			break;
		case SOURCE :
			startService(new Intent(this,SourceService.class));
			bindService(new Intent(this,SourceService.class),musicConnection,0);
			break ;
		}
	}

	public MusicService getMusicService() {
		return musicService;
	}

	public void setMusicService(MusicService musicService) {
		this.musicService = musicService;
	}





}


