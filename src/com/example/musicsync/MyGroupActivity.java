package com.example.musicsync;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MyGroupActivity extends Activity {

	private SharedPreferences myPreference;
	private static final String LOGGING_TAG = "MusicSyncPlaylists";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mygroup);

		// get preference
		myPreference = getSharedPreferences("MyGroup", 0);

		if (myPreference.getString("GroupName", null) == null) { // no
																	// preference
																	// exist,
																	// create
																	// new one

			// disable Text GroupName, GroupNum, List_MusicList begin
			TextView Text_GroupName = (TextView) findViewById(R.id.label_groupname);
			Text_GroupName.setVisibility(8);
			TextView Text_GroupNum = (TextView) findViewById(R.id.label_groupnum);
			Text_GroupNum.setVisibility(8);
			TextView Text_MusicList = (TextView) findViewById(R.id.label_musiclist);
			Text_MusicList.setVisibility(8);
			ListView List_MusicList = (ListView) findViewById(R.id.list_musictrack);
			List_MusicList.setVisibility(8);

			Button ActivateGroup = (Button) findViewById(R.id.b_activategroup);
			ActivateGroup.setEnabled(false);
			ActivateGroup.setVisibility(8);
			// disable Text GroupName, GroupNum, List_MusicList end

			// set create group button begin
			Button CreateGroup = (Button) findViewById(R.id.b_creategroup);
			CreateGroup.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Intent i_CreateGroup = new Intent(MyGroupActivity.this,
							CreateGroupActivity.class);
					MyGroupActivity.this.startActivity(i_CreateGroup);
				}
			});
			// set create group button end
		} else { // preference exists, set information

			// disable button, warning begin
			Button CreateGroup = (Button) findViewById(R.id.b_creategroup);
			CreateGroup.setEnabled(false);
			CreateGroup.setVisibility(8);
			TextView Warning_NoGroup = (TextView) findViewById(R.id.warning_nogroup);
			Warning_NoGroup.setVisibility(8);
			// disable button, warning begin

			// get information from preference begin
			String GroupName = myPreference.getString("GroupName", null);
			int GroupNum = myPreference.getInt("GroupNum", 0);
			String MusicList = myPreference.getString("MusicList", null);
			// get information from preference end

			// set information begin
			TextView Text_GroupName = (TextView) findViewById(R.id.label_groupname);
			Text_GroupName.setText("Group Name: " + GroupName);
			TextView Text_GroupNum = (TextView) findViewById(R.id.label_groupnum);
			Text_GroupNum.setText(String.valueOf("Member Number: " + GroupNum));
			TextView Text_MusicList = (TextView) findViewById(R.id.label_musiclist);
			Text_MusicList.setText("Music Track: " + MusicList);

			ListView List_MusicTrack = (ListView) findViewById(R.id.list_musictrack);

			ArrayList<String> myList = new ArrayList<String>();
			myList = getPlayTrackSongs(MusicList);
			
			// which used for device list view to show song names
			int flag = 0;
			if (myList.size() == 0) {
				myList.add("No music in this track...., please choose another music track.");
				flag = 1;
			}else{
				String combineSongnames = myList.get(0);
				for (int i = 1; i < myList.size(); i++){
					combineSongnames = combineSongnames + "$^%" + myList.get(i);
				}
				SharedPreferences.Editor editor = myPreference.edit();
				editor.putString("SongNames", combineSongnames);
				editor.commit();			
			}

			List_MusicTrack.setAdapter(new MusicArrayAdapter(this,
					R.layout.music_list_view, myList));
			// set information end

			Button ActivateGroup = (Button) findViewById(R.id.b_activategroup);
			if (flag == 1) {
				ActivateGroup.setVisibility(8);
				ActivateGroup.setEnabled(false);
			} else {
				ActivateGroup.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Intent i_GroupList = new Intent(MyGroupActivity.this,
								GroupListActivity.class);
						MyGroupActivity.this.startActivity(i_GroupList);
					}
				});
			}
		}

	}

	private class MusicArrayAdapter extends ArrayAdapter<String> {

		private Context context;

		public MusicArrayAdapter(Context context, int layout,
				ArrayList<String> songlists) {
			super(context, layout, songlists);
			this.context = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;

			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.music_list_view, null);
			}

			String song = getItem(position);
			if (song != null) {
				if (song.contains("please choose another music track")) {
					TextView musicName = (TextView) view
							.findViewById(R.id.listItemTextView);
					musicName.setText(song);
					ImageView flagImageView = (ImageView) view
							.findViewById(R.id.listItemImageView);
					flagImageView.setImageDrawable(getResources().getDrawable(
							R.drawable.ic_unhappy));
				} else {
					TextView musicName = (TextView) view
							.findViewById(R.id.listItemTextView);
					musicName.setText(song);
					ImageView flagImageView = (ImageView) view
							.findViewById(R.id.listItemImageView);
					flagImageView.setImageDrawable(getResources().getDrawable(
							R.drawable.ic_songicon_sm));
				}
			}

			return view;

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if (myPreference.getString("GroupName", null) != null) {
			getMenuInflater().inflate(R.menu.menu_mygroup, menu);
		}
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.editGroup:
			Intent intent = new Intent(this, CreateGroupActivity.class);
			startActivity(intent);
			return true;

		case R.id.removeGroup:
			SharedPreferences preferences = getSharedPreferences("MyGroup", 0);
			preferences.edit().clear().commit();
			showToast(this, "Group has been cleared", 3);
			Intent intent1 = new Intent(this, MyGroupActivity.class);
			startActivity(intent1);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public static void showToast(Context context, String message, int duration) {
		Toast toast = Toast.makeText(context, message, duration);
		toast.show();
	}

	@SuppressWarnings("deprecation")
	public ArrayList<String> getPlayTrackSongs(String playlistName) {
		Cursor cursor = null;
		ArrayList<String> musiclist = new ArrayList<String>();

		String[] projection1 = { MediaStore.Audio.Playlists._ID,
				MediaStore.Audio.Playlists.NAME };

		cursor = this.managedQuery(
				MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, projection1,
				MediaStore.Audio.Playlists.NAME + "=\"" + playlistName + "\"",
				null, null);
		startManagingCursor(cursor);
		cursor.moveToFirst();
		Long playlist_id2 = cursor.getLong(0);

		if (playlist_id2 > 0) {
			String[] projection = {
					MediaStore.Audio.Playlists.Members.AUDIO_ID,
					MediaStore.Audio.Playlists.Members.ARTIST,
					MediaStore.Audio.Playlists.Members.TITLE,
					MediaStore.Audio.Playlists.Members._ID,
					MediaStore.Audio.Playlists.Members.DATA

			};
			cursor = null;
			cursor = getContentResolver().query(
					MediaStore.Audio.Playlists.Members.getContentUri(
							"external", playlist_id2), projection, null, null,
					null);
			if (cursor != null && cursor.getCount() > 0) {
				int numSongs = cursor.getCount();
				Log.d("number of songs: ", "numSongs: " + numSongs); // Correctly
																		// outputs
																		// 3

				myPreference = getSharedPreferences("MyGroup", 0);
				SharedPreferences.Editor editor = myPreference.edit();

				MusicInfoManager miM = new MusicInfoManager();

				for (boolean hasItem = cursor.moveToFirst(); hasItem; hasItem = cursor
						.moveToNext()) {
					String musicName = cursor
							.getString(cursor
									.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE));
					Log.i(LOGGING_TAG, musicName);
					String musicAuthor = cursor
							.getString(cursor
									.getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST));

					musiclist.add(musicName + " -- Artist:" + musicAuthor);

					String fullpath = cursor
							.getString(cursor
									.getColumnIndex(MediaStore.Audio.Playlists.Members.DATA));
					Log.i(LOGGING_TAG, fullpath);

					MusicInfo mi = new MusicInfo();
					mi.musicName = musicName;
					mi.musicPath = fullpath;

					miM.addMusic(mi);
				}

				Gson gson = new Gson();
				String objStr = gson.toJson(miM);

				editor.putString("musicgroup", objStr);
				editor.commit();
			}
		}

		return musiclist;
	}

}
