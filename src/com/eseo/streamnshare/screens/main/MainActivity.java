package com.eseo.streamnshare.screens.main;

import java.util.ArrayList;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
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
import com.eseo.streamnshare.fragments.OnItemSelectedFragmentListener;
import com.eseo.streamnshare.fragments.main.AlbumGridFragment;
import com.eseo.streamnshare.fragments.main.ArtistListFragment;
import com.eseo.streamnshare.fragments.main.SongListFragment;
import com.eseo.streamnshare.managers.MusicManager;
import com.eseo.streamnshare.model.Song;
import com.eseo.streamnshare.screens.fullscreen.FullScreenActivity;

public class MainActivity extends FragmentActivity implements OnItemSelectedFragmentListener{

	//NavigationDrawer
	private String[] navDrawerItems;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mTitle;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final ActionBar actionBar = getActionBar();
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC); 

		initActionBarDrawer();
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		/* Navigation drawer */
		navDrawerItems = getResources().getStringArray(R.array.nav_drawer_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerList.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_list_item, navDrawerItems));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		/* Custom action bar */
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));

		/* Fragments */
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.fragment_placeholder, new SongListFragment());
		ft.commit();
	}

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

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				getActionBar().setTitle(R.string.app_name);
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getActionBar().setTitle(R.string.app_name);
			}
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
			ft.replace(R.id.fragment_placeholder, new ArtistListFragment());
			ft.commit();
		}
		else if(choix.equals("Titres")){
			MusicManager.getInstance(this).sortByTitle();
			ft.replace(R.id.fragment_placeholder, new SongListFragment());
			ft.commit();
		}
		else if(choix.equals("Albums")){
			ft.replace(R.id.fragment_placeholder, new AlbumGridFragment());
			ft.commit();
		}

		// Highlight the selected item, update the title, and close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(navDrawerItems[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
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


	/*
	 * Fragment interface to launch a music
	 */


	@Override
	public void onSongItemSelected(int position) {
		SongListFragment fragment = (SongListFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder);
		ArrayList<Song> songList = fragment.getSongList();
		
		Intent intent = new Intent(this, FullScreenActivity.class);
		Bundle b = new Bundle();
		b.putInt("positionSong", position);
		b.putParcelableArrayList("songListKey",songList);
		intent.putExtras(b);
		startActivity(intent);
	}

	
	
	/*
	 * Menu items
	 */
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		// TODO: SearchManager / SearchView / AppManifest / searchable.xml / etc...
		return true/*super.onCreateOptionsMenu(menu)*/;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		//menu item selected
		switch(item.getItemId()){
		case R.id.action_search:
			// TODO: searchItem selected
			return true;
		case R.id.action_end:
			System.exit(0);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}


