package com.example.musicsync;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DeviceListFragment extends ListFragment {

	DevicesAdapter listAdapter = null;

	interface DeviceClickListener {
		public void connectP2p(GroupInfo GroupInfo);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.devices_list, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		listAdapter = new DevicesAdapter(this.getActivity(),
				R.layout.device_list_view, android.R.id.text1,
				new ArrayList<GroupInfo>());
		setListAdapter(listAdapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		((DeviceClickListener) getActivity()).connectP2p((GroupInfo) l
				.getItemAtPosition(position));
		((TextView) v.findViewById(R.id.groupNameView)).setText("Connecting");

	}

	public class DevicesAdapter extends ArrayAdapter<GroupInfo> {

		private List<GroupInfo> items;

		public DevicesAdapter(Context context, int resource,
				int textViewResourceId, List<GroupInfo> items) {
			super(context, resource, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.device_list_view, null);
			}
			GroupInfo service = items.get(position);
			if (service != null) {
				TextView nameText = (TextView) v
						.findViewById(R.id.groupNameView);
				
				TextView numText = (TextView) v
						.findViewById(R.id.groupNumView);
				
				ListView list = (ListView) v.findViewById(R.id.musicListView);

				if (nameText != null) {
					//String info = "Group: " + service.groupName + "  " + ""
					if (service.groupName != null) {
						nameText.setText("Group: " +service.groupName);
					}
				}
				if (numText != null){
					if (service.groupMemberAllowed != 0){
						numText.setText("Allowed Connections: "+String.valueOf(service.groupMemberAllowed));
					}
				}
				if (list != null){
					if (service.songsName != null){
						String[] songs = service.songsName.split("$^%");
						ArrayAdapter<String> musicListAdapter = new ArrayAdapter<String>(this.getContext(),
						android.R.layout.simple_list_item_1, songs);
						list.setAdapter(musicListAdapter);
					}
				}
			}
			return v;
		}

	}
}
