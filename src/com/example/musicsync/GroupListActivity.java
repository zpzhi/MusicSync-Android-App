package com.example.musicsync;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.example.musicsync.DeviceListFragment.DeviceClickListener;
import com.example.musicsync.DeviceListFragment.DevicesAdapter;
import com.example.musicsync.WiFiMusicSyncFragment.MessageTarget;
import com.google.gson.Gson;

import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class GroupListActivity extends Activity implements DeviceClickListener,
		Handler.Callback, ConnectionInfoListener, MessageTarget {
	private final IntentFilter intentFilter = new IntentFilter();
	private WifiP2pManager manager;
	private Channel channel;
	private SharedPreferences myPreference;

	private WifiP2pDnsSdServiceRequest serviceRequest;
	private DeviceListFragment servicesList;
	private WiFiMusicSyncFragment musicSyncFragment;

	private BroadcastReceiver receiver = null;

	private Handler handler = new Handler(this);

	public static final String TAG = "networkdistest";

	public static final int MY_HANDLE = 0x400 + 2;
	public static final int MESSAGE_READ = 0x400 + 1;

	static final int SERVER_PORT = 4545;

	public static final String TXTRECORD_PROP_AVAILABLE = "available";
	public static final String SERVICE_INSTANCE = "_networkdistest";
	public static final String SERVICE_REG_TYPE = "_presence._tcp";

	private static final String LOGGING_TAG = "NETWORK DISCOVERY TEST";

	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grouplist);

		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(this, getMainLooper(), null);

		startRegistrationAndDiscovery();

		servicesList = new DeviceListFragment();
		getFragmentManager().beginTransaction()
				.add(R.id.container_root, servicesList, "services").commit();
	}

	@Override
	protected void onRestart() {
		Fragment frag = getFragmentManager().findFragmentByTag("services");
		if (frag != null) {
			getFragmentManager().beginTransaction().remove(frag).commit();
		}
		super.onRestart();
	}

	@Override
	protected void onStop() {
		if (manager != null && channel != null) {
			manager.removeGroup(channel, new ActionListener() {

				@Override
				public void onFailure(int reasonCode) {
					Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
				}

				@Override
				public void onSuccess() {
				}

			});
		}
		super.onStop();
	}

	private void startRegistrationAndDiscovery() {
		Map<String, String> record = new HashMap<String, String>();
		record.put(TXTRECORD_PROP_AVAILABLE, "visible");
		myPreference = getSharedPreferences("MyGroup", 0);
		if (myPreference.getString("GroupName", null) != null) {
			// get information from preference begin
			String GroupName = myPreference.getString("GroupName", null);
			int GroupNum = myPreference.getInt("GroupNum", 0);
			String MusicList = myPreference.getString("MusicList", null);
			String SongNames = myPreference.getString("SongNames", null);
			record.put("group_name", GroupName);
			record.put("group_member_num", Integer.toString(GroupNum));
			record.put("playlist_name", MusicList);
			record.put("song_names", SongNames);

		}
		WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
				SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
		manager.addLocalService(channel, service, new ActionListener() {

			@Override
			public void onSuccess() {
				// appendStatus("Added Local Service");
				Log.e(LOGGING_TAG, "Added local service successfully");
			}

			@Override
			public void onFailure(int error) {
				// appendStatus("Failed to add a service");
				Log.e(LOGGING_TAG, "Failed to add a service");
			}
		});

		discoverService();

	}

	private void discoverService() {

		final HashMap<String, String> groupinfos = new HashMap<String, String>();
		/*
		 * Register listeners for DNS-SD services. These are callbacks invoked
		 * by the system when a service is actually discovered.
		 */

		DnsSdTxtRecordListener txtListener = new DnsSdTxtRecordListener() {
			@Override
			/*
			 * Callback includes: fullDomain: full domain name: e.g
			 * "printer._ipp._tcp.local." record: TXT record dta as a map of
			 * key/value pairs. device: The device running the advertised
			 * service.
			 */
			public void onDnsSdTxtRecordAvailable(String fullDomain, Map

			record, WifiP2pDevice device) {
				Log.d(TAG,
						device.deviceName + " is "
								+ record.get(TXTRECORD_PROP_AVAILABLE));
				groupinfos.put("groupname", (String) record.get("group_name"));
				groupinfos.put("groupmembernum",
						(String) record.get("group_member_num"));
				groupinfos.put("playlistname",
						(String) record.get("playlist_name"));
				groupinfos.put("songnames",
						(String) record.get("song_names"));			
			}
		};

		DnsSdServiceResponseListener servListener = new DnsSdServiceResponseListener() {
			@Override
			public void onDnsSdServiceAvailable(String instanceName,
					String registrationType, WifiP2pDevice srcDevice) {

				// A service has been discovered. Is this our app?

				if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {

					// update the UI and add the item the discovered
					// device.
					DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
							.findFragmentByTag("services");
					if (fragment != null) {

						DevicesAdapter adapter = ((DevicesAdapter) fragment
								.getListAdapter());
						GroupInfo service = new GroupInfo();
						service.device = srcDevice;
						service.instanceName = instanceName;
						service.serviceRegistrationType = registrationType;
						service.groupName = groupinfos.get("groupname");
						if (groupinfos.get("groupmembernum") != null) {
							service.groupMemberAllowed = Integer
									.parseInt(groupinfos.get("groupmembernum"));

						}
						service.musicTrackName = groupinfos.get("playlistname");
						service.songsName = groupinfos.get("songnames");
						
						adapter.add(service);
						adapter.notifyDataSetChanged();
						Log.d(TAG, "onBonjourServiceAvailable " + instanceName);
					}
				}

			}
		};
		manager.setDnsSdResponseListeners(channel, servListener, txtListener);
		// After attaching listeners, create a service request and initiate
		// discovery.
		serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
		manager.addServiceRequest(channel, serviceRequest,
				new ActionListener() {

					@Override
					public void onSuccess() {
						Log.e(LOGGING_TAG, "Added service discovery request");
					}

					@Override
					public void onFailure(int arg0) {
						Log.e(LOGGING_TAG,
								"Failed adding service discovery request");
					}
				});
		manager.discoverServices(channel, new ActionListener() {

			@Override
			public void onSuccess() {
				Log.e(LOGGING_TAG, "Service discovery initiated");
			}

			@Override
			public void onFailure(int arg0) {
				Log.e(LOGGING_TAG, "Service discovery failed");

			}
		});
	}

	@Override
	public void connectP2p(GroupInfo service) {
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = service.device.deviceAddress;
		config.wps.setup = WpsInfo.PBC;
		if (serviceRequest != null)
			manager.removeServiceRequest(channel, serviceRequest,
					new ActionListener() {

						@Override
						public void onSuccess() {
						}

						@Override
						public void onFailure(int arg0) {
						}
					});

		manager.connect(channel, config, new ActionListener() {

			@Override
			public void onSuccess() {
				// appendStatus("Connecting to service");
				Log.e(LOGGING_TAG, "Connecting to service");
			}

			@Override
			public void onFailure(int errorCode) {
				// appendStatus("Failed connecting to service");
				Log.e(LOGGING_TAG, "Failed connecting to service");

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_navigationbar, menu);
		return true;
	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {

		Gson gson = new Gson();
		SharedPreferences mySharedPreference = getSharedPreferences("MyGroup",
				0);
		String savedObjStr = mySharedPreference.getString("musicgroup", null);
		MusicInfoManager miM = null;
		if (savedObjStr != null) {
			miM = gson.fromJson(savedObjStr, MusicInfoManager.class);
		} else {
			miM = new MusicInfoManager();
		}

		// TODO Auto-generated method stub
		Thread handler = null;
		/*
		 * The group owner accepts connections using a server socket and then
		 * spawns a client socket for every client. This is handled by {@code
		 * GroupOwnerSocketHandler}
		 */

		if (p2pInfo.isGroupOwner) {
			Log.d(TAG, "Connected as group owner");
			try {

				handler = new GroupOwnerSocketHandler(
						((MessageTarget) this).getHandler(), miM);
				handler.start();
			} catch (IOException e) {
				Log.d(TAG,
						"Failed to create a server thread - " + e.getMessage());
				return;
			}
		} else {
			Log.d(TAG, "Connected as peer");
			handler = new ClientSocketHandler(
					((MessageTarget) this).getHandler(),
					p2pInfo.groupOwnerAddress, miM);
			handler.start();
		}
		musicSyncFragment = new WiFiMusicSyncFragment();
		getFragmentManager().beginTransaction()
				.replace(R.id.container_root, musicSyncFragment).commit();
		// statusTxtView.setVisibility(View.GONE);

	}

	@Override
	public void onResume() {
		super.onResume();
		receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
		registerReceiver(receiver, intentFilter);
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}

	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case MESSAGE_READ:
			byte[] readBuf = (byte[]) msg.obj;
			// construct a string from the valid bytes in the buffer
			String readMessage = new String(readBuf);
			Log.d(TAG, readMessage);
			(musicSyncFragment).pushMessage("Buddy: " + readMessage);
			// if (readMessage.equals(""))
			break;

		case MY_HANDLE:
			Object obj = msg.obj;
			(musicSyncFragment).setMusicSyncManager((MusicSyncManager) obj);
		}
		return true;

	}
}
