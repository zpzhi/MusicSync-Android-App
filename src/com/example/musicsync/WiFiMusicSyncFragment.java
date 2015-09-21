package com.example.musicsync;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * This fragment handles chat related UI which includes a list view for messages
 * and a message entry field with send button.
 */
public class WiFiMusicSyncFragment extends Fragment {

	private View view;
	private MusicSyncManager musicSyncManager;
	private TextView chatLine;
	private ListView listView;
	private Button playButton, sendButton;
	ChatMessageAdapter adapter = null;
	private List<String> items = new ArrayList<String>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_chat, container, false);
		chatLine = (TextView) view.findViewById(R.id.txtChatLine);
		listView = (ListView) view.findViewById(android.R.id.list);
		playButton = (Button) view.findViewById(R.id.button2);
		playButton.setVisibility(8);
		playButton.setEnabled(false);
		adapter = new ChatMessageAdapter(getActivity(), android.R.id.text1,
				items);
		listView.setAdapter(adapter);
		sendButton = (Button) view.findViewById(R.id.button1);
		sendButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (musicSyncManager != null) {
					musicSyncManager.write();
					// pushMessage("Me: " + chatLine.getText().toString());
					// chatLine.setText("");
					// chatLine.clearFocus();
				}
			}
		});

		playButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (musicSyncManager != null) {
					try {
						Calendar c = Calendar.getInstance();
						c.setTime(new Date()); /* whatever */
						// c.setTimeZone(...); if necessary
						int mills = c.get(Calendar.MILLISECOND);
						int seconds = c.get(Calendar.SECOND);
						musicSyncManager.clientPlayStart(((seconds + 2) % 60)
								* 1000 + mills);
						musicSyncManager.playMusic(((seconds + 2) % 60) * 1000
								+ mills);
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		});
		return view;
	}

	public interface MessageTarget {
		public Handler getHandler();
	}

	public void setMusicSyncManager(MusicSyncManager obj) {
		musicSyncManager = obj;
	}

	public void pushMessage(String readMessage) {

		adapter.add(readMessage);
		adapter.notifyDataSetChanged();

		if (readMessage.contains("Send Complete")) {
			sendButton.setVisibility(8);
			sendButton.setEnabled(false);

			playButton.setVisibility(0);
			playButton.setEnabled(true);

		}
	}

	/**
	 * ArrayAdapter to manage chat messages.
	 */
	public class ChatMessageAdapter extends ArrayAdapter<String> {

		List<String> messages = null;

		public ChatMessageAdapter(Context context, int textViewResourceId,
				List<String> items) {
			super(context, textViewResourceId, items);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(android.R.layout.simple_list_item_1, null);
			}
			String message = items.get(position);
			if (message != null && !message.isEmpty()) {
				TextView nameText = (TextView) v
						.findViewById(android.R.id.text1);

				if (nameText != null) {
					nameText.setText(message);
					if (message.startsWith("Me: ")) {
						nameText.setTextAppearance(getActivity(),
								R.style.normalText);
					} else {
						nameText.setTextAppearance(getActivity(),
								R.style.boldText);
					}
				}
			}
			return v;
		}
	}
}
