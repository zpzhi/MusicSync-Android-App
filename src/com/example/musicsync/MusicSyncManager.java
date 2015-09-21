package com.example.musicsync;

import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Handles reading and writing of messages with socket buffers. Uses a Handler
 * to post messages to UI thread for UI updates.
 */
public class MusicSyncManager implements Runnable {

	private Socket socket = null;
	private Handler handler;
	private MusicInfoManager miM;
	String MusicName = "";

	// private String m_musicName = "12345";
	public MusicSyncManager(Socket socket, Handler handler, MusicInfoManager m) {
		this.socket = socket;
		this.handler = handler;
		this.miM = m;
	}

	private InputStream iStream;
	private OutputStream oStream;
	private static final String TAG = "MusicSyncHandler";
	private String info_server_ready = "Are you Ready?";
	private String info_client_ready = "I'm Ready.";
	private String info_music_name = "Name:";
	private String info_get_music_name = "Get MusicName successfully";
	private String info_send_complete = "Music sent complete";
	private String info_get_music = "Get Music successfully";
	private String info_all_complete = "All Music sent completed";
	private String info_thanks = "Thank you.";
	private String start_play = "Start to play music.";

	private ArrayList<String> musicList = new ArrayList<String>();

	@Override
	public void run() {
		try {

			iStream = socket.getInputStream();
			oStream = socket.getOutputStream();
			byte[] buf;
			int bytes;
			boolean receiveMusic = false;
			int song_num = 0;

			handler.obtainMessage(GroupListActivity.MY_HANDLE, this)
					.sendToTarget();

			while (true) {
				try {
					buf = new byte[1024];
					bytes = iStream.read(buf);
					if (bytes == -1) {
						break;
					}
					if (receiveMusic) {
						String name = null;

						if (miM != null) {
							// String path =
							// miM.getMusicInfoList().get(0).musicPath;
							name = miM.getMusicInfoList().get(song_num).musicName;
						} else {
							name = "unKnown";
						}
						Log.d("Receive and Create Music name is process 5: ",
								name);
						String path = Environment.getExternalStorageDirectory()
								+ "/" + name + ".mp3";
						final File f = new File(path);
						musicList.add(path);

						OutputStream out = new FileOutputStream(f);

						int len;

						try {

							while ((len = iStream.read(buf)) != -1) {
								String contact = new String(buf);
								if (contact.contains(info_send_complete)) {
									break;
								} else {
									out.write(buf, 0, len);
								}
							}
							receiveMusic = false;
						} catch (IOException e) {
							Log.d("File copy Error: ", e.toString());
						}

						song_num++;

						try {
							oStream.write(info_get_music.getBytes());
						} catch (IOException e) {
							Log.e(TAG, "Exception during write", e);
						}
					} else {
						String contact = new String(buf);
						Log.d("Contact is : ", contact);

						if (contact.contains(info_server_ready)) { // client
																	// Ready?
																	// process 1
							try {
								oStream.write(info_client_ready.getBytes());
							} catch (IOException e) {
								Log.e(TAG, "Exception during write", e);
							}
						} else if (contact.contains(info_client_ready)
								|| contact.contains(info_get_music)) { // send
																		// music
																		// name
																		// process
																		// 2
							if (song_num == miM.getMusicInfoList().size()) { // send
																				// complete
								try {
									oStream.write(info_all_complete.getBytes());
								} catch (IOException e) {
									Log.e(TAG, "Exception during write", e);
								}
							} else {
								try {

									if (miM != null) {
										MusicName = miM.getMusicInfoList().get(
												song_num).musicName;
									} else {
										MusicName = "unKnown name";
									}
									Log.d("Send Music Name: ", MusicName);
									oStream.write((info_music_name + MusicName + "#")
											.getBytes());
								} catch (IOException e) {
									Log.e(TAG, "Exception during write", e);
								}
							}
						} else if (contact.contains(info_music_name)) { // get
																		// music
																		// name
																		// process
																		// 3
							int i = 0;
							while (i < 100) {
								if (contact.charAt(++i) == '#') {
									break;
								}
							}
							MusicName = contact.substring(5, i);
							MusicInfo newMusic = new MusicInfo();
							newMusic.musicName = MusicName;
							miM.addMusic(newMusic);

							receiveMusic = true;

							Log.d("Receive Music Name: ", MusicName);

							try {
								oStream.write(info_get_music_name.getBytes());
							} catch (IOException e) {
								Log.e(TAG, "Exception during write", e);
							}
						} else if (contact.contains(info_get_music_name)) { // send
																			// music
																			// process
																			// 4
																			// and
																			// 6

							InputStream in = null;
							String path = miM.getMusicInfoList().get(song_num).musicPath;
							musicList.add(path);
							// String name =
							// miM.getMusicInfoList().get(song_num).musicName;

							// final File file = new
							// File(Environment.getExternalStorageDirectory() +
							// "/Music/123.mp3");
							Log.d("Send Music Full file path: ", path);
							final File file = new File(path);
							Log.d("file path:", file.getPath());

							try {
								in = new FileInputStream(file);
							} catch (FileNotFoundException e) {
								Log.d("File Error: ", e.toString());
							}

							song_num++;

							try {
								byte[] buffer = new byte[1024];
								int read = 0;
								while ((read = in.read(buffer)) != -1) {
									oStream.write(buffer, 0, read);
								}
								oStream.write(info_send_complete.getBytes());
							} catch (IOException e) {
								Log.d("IOException ", e.toString());
							}

						} else if (contact.contains(info_all_complete)) { // send
																			// thank
																			// you

							try {
								oStream.write(info_thanks.getBytes());
							} catch (IOException e) {
								Log.e(TAG, "Exception during write", e);
							}
							song_num = 0;
						} else if (contact.contains(info_thanks)) { // send
																	// music
																	// complete
																	// process
																	// final
							// do nothing or send time
							handler.obtainMessage(
									GroupListActivity.MESSAGE_READ,
									"Send Complete".getBytes()).sendToTarget();
							song_num = 0;
						} else if (contact.contains(start_play)) { // send music
																	// complete
																	// process
																	// final
							int i = 0;
							while (i < 100) {
								if (contact.charAt(++i) == '#') {
									break;
								}
							}
							String play_time = contact.substring(0, i);
							int time = Integer.parseInt(play_time);
							// while time is not equal to the deal time
							// { block }
							playMusic(time);
						} else {
							// do nothing
						}
					}
				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void write() {
		try {
			oStream.write(info_server_ready.getBytes());
		} catch (IOException e) {
			Log.e(TAG, "Exception during write", e);
		}
	}

	public void clientPlayStart(int playtime) {
		try {
			String s_playtime = String.valueOf(playtime);
			String message = s_playtime + "#" + start_play;
			oStream.write(message.getBytes());
		} catch (IOException e) {
			Log.e(TAG, "Exception during write", e);
		}
	}

	public void playMusic(int playtime) throws IllegalArgumentException,
			SecurityException, IllegalStateException, IOException {

		final MediaPlayer mediaPlayer = new MediaPlayer();

		mediaPlayer.setDataSource(musicList.get(0));
		mediaPlayer.prepare();

		while (true) {
			Log.d("send play time: ", String.valueOf(playtime));
			Calendar c = Calendar.getInstance();
			c.setTime(new Date()); /* whatever */
			// c.setTimeZone(...); if necessary
			int mills = c.get(Calendar.MILLISECOND);
			int seconds = c.get(Calendar.SECOND);
			int currentPlaytime = (seconds % 60) * 1000 + mills;
			Log.d("Current Play time: ", String.valueOf(currentPlaytime));
			if (currentPlaytime == playtime)
				break;
		}

		mediaPlayer.start();

		mediaPlayer
				.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {

						int i = 1;
						if (i < musicList.size()) {
							mediaPlayer.reset();
							/* load the new source */
							try {
								mediaPlayer.setDataSource(musicList.get(i));
								/* Prepare the mediaplayer */
								mediaPlayer.prepare();
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

							/* start */
							mediaPlayer.start();
						} else {
							/* release mediaplayer */
							mediaPlayer.release();
						}
					}
				});
	}

}
