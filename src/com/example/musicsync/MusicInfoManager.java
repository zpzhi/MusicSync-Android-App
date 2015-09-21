package com.example.musicsync;

import java.util.ArrayList;

public class MusicInfoManager {
	private ArrayList<MusicInfo> mMusicList;

	public MusicInfoManager() {
		mMusicList = new ArrayList<MusicInfo>();
	}

	public ArrayList<MusicInfo> getMusicInfoList() {
		return mMusicList;
	}

	public void addMusic(MusicInfo mi) {
		mMusicList.add(mi);
	}

}
