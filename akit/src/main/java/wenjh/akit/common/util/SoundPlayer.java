package wenjh.akit.common.util;

import android.media.AudioManager;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.Map;

public class SoundPlayer {
	private static SoundPlayer soundPlayer = null;
	private SoundPool soundPool = null;
	
	private Map<Integer, Integer> soundMap = null;
	LogUtil log = new LogUtil(this);
	
	private SoundPlayer() {
		soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
		soundMap = new HashMap<Integer, Integer>();
	}
	
	public static SoundPlayer getInstance() {
		synchronized (SoundPlayer.class) {
			if(soundPlayer == null) {
				soundPlayer = new SoundPlayer();
			}
		}
		
		return soundPlayer;
	}
	
	public static void init() {
		getInstance();
	}
	
	public void play(int resId) {
		int id = soundMap.get(resId) == null ? 0 : soundMap.get(resId);
		if(id <= 0) return;
		soundPool.play(id, 1.0f, 1.0f, 1, 0, 1.0f);
	}
	
	public void stop(int resId) {
		int id = soundMap.get(resId) == null ? 0 : soundMap.get(resId);
		if(id <= 0) return;
		
		soundPool.stop(id);
	}
	
	public void stopAll() {
		for (Map.Entry<Integer, Integer> entry : soundMap.entrySet()) {
			stop(entry.getValue());
		}
	}
}
