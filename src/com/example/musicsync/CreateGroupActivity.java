package com.example.musicsync;

import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.Gson;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CreateGroupActivity extends Activity {

	private static final String LOGGING_TAG = "MusicSyncPlaylists";
	private String m_fullpath = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_creategroup);

		// set spinner begin
		Spinner sp_GroupNum = (Spinner) findViewById(R.id.input_groupnum);
		Spinner sp_MusicList = (Spinner) findViewById(R.id.input_musiclist);
		// sp_MusicList.(Color.parseColor("#FFFFFF"));
		ArrayAdapter<String> adp_GroupNum = new ArrayAdapter<String>(this,
				R.layout.spinner_item, new String[] { "1", "2", "3", "4" });

		ArrayList<String> al_MusicList = getPlayTracks();
		if (al_MusicList == null) {
			al_MusicList = new ArrayList() {
			};
		}
		ArrayAdapter<String> adp_MusicList = new ArrayAdapter<String>(this,
				R.layout.spinner_item, al_MusicList);

		adp_GroupNum
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp_GroupNum.setAdapter(adp_GroupNum);

		adp_MusicList
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp_MusicList.setAdapter(adp_MusicList);

		SharedPreferences myPreference = getSharedPreferences("MyGroup", 0);
		if (myPreference.getString("GroupName", null) != null) {
			String GroupName = myPreference.getString("GroupName", null);
			int gr_Position = myPreference.getInt("groupnumPosition", 0);
			int ms_Position = myPreference.getInt("musiclistPosition", 0);

			EditText ed_GroupName = (EditText) findViewById(R.id.input_groupname);
			ed_GroupName.setText(GroupName);
			sp_GroupNum.setSelection(gr_Position);
			sp_MusicList.setSelection(ms_Position);
		}
		// set spinner end
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_creategroup, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.confirm:

			// get text begin
			EditText ed_GroupName = (EditText) findViewById(R.id.input_groupname);
			String GroupName = ed_GroupName.getText().toString();

			Spinner sp_GroupNum = (Spinner) findViewById(R.id.input_groupnum);
			int GroupNum = Integer.parseInt(sp_GroupNum.getSelectedItem()
					.toString());

			Spinner sp_MusicList = (Spinner) findViewById(R.id.input_musiclist);
			Object ml = sp_MusicList.getSelectedItem();
			int ms_position = -1;
			int gr_position = sp_GroupNum.getSelectedItemPosition();
			String MusicList = null;
			if (ml != null) {
				MusicList = sp_MusicList.getSelectedItem().toString();
				ms_position = sp_MusicList.getSelectedItemPosition();
			}
			if (GroupName.isEmpty()) // No GroupName
			{
				showToast(this, "Please enter group name", 3);
				return true;
			} else if (MusicList == null) // No GroupList
			{
				showToast(this,
						"There is no music list, please create one in your music"
								+ "player", 5);

				return true;
			} else // save information and start NSD
			{
				// save information begin
				SharedPreferences myPreference = getSharedPreferences(
						"MyGroup", 0);
				SharedPreferences.Editor editor = myPreference.edit();

				editor.putString("GroupName", GroupName);
				editor.putInt("GroupNum", GroupNum);
				editor.putInt("groupnumPosition", gr_position);
				editor.putString("MusicList", MusicList);
				editor.putInt("musiclistPosition", ms_position);

				editor.commit();
				// save information end

				Intent intent = new Intent(this, MyGroupActivity.class);
				startActivity(intent);
				return true;
			}

		case R.id.cancel:
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

	public ArrayList<String> getPlayTracks() {
		ArrayList<String> playlistsName = new ArrayList<String>();

		// Get a cursor over all playlists.
		final ContentResolver resolver = this.getContentResolver();
		final Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
		final String idKey = MediaStore.Audio.Playlists._ID;
		final String nameKey = MediaStore.Audio.Playlists.NAME;
		final String[] columns = { idKey, nameKey };
		final Cursor playLists = resolver.query(uri, columns, null, null, null);
		if (playLists == null) {
			Log.e(LOGGING_TAG, "Found no playlists.");
			return null;
		}

		// Log a list of the playlists.
		Log.i(LOGGING_TAG, "Playlists:");
		String playListName = null;
		for (boolean hasItem = playLists.moveToFirst(); hasItem; hasItem = playLists
				.moveToNext()) {
			playListName = playLists.getString(playLists
					.getColumnIndex(nameKey));
			Log.i(LOGGING_TAG, playListName);
			playlistsName.add(playListName);
		}
		// Close the cursor.
		if (playLists != null) {
			playLists.close();
		}

		return playlistsName;
	}
}