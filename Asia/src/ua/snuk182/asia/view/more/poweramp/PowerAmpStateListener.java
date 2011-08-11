package ua.snuk182.asia.view.more.poweramp;

import java.util.HashMap;
import java.util.Map;

import ua.snuk182.asia.services.RuntimeService;
import ua.snuk182.asia.services.ServiceUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public class PowerAmpStateListener {
	
	private final RuntimeService service;
	
	private final Map<Integer, IPlayerStateListener> listeners = new HashMap<Integer, IPlayerStateListener>();
	
	private final BroadcastReceiver playStatusReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean paused = true;
			
			int status = intent.getIntExtra(PowerAMPiAPI.STATUS, -1);
			int playerState = IPlayerStateListener.STOPPED;
			
			switch(status) {
				case PowerAMPiAPI.Status.TRACK_PLAYING:
					paused = intent.getBooleanExtra(PowerAMPiAPI.PAUSED, false);
					if (!paused){
						playerState = IPlayerStateListener.STARTED;
					}
					break;
		
				case PowerAMPiAPI.Status.TRACK_ENDED:
				case PowerAMPiAPI.Status.PLAYING_ENDED:
					playerState = IPlayerStateListener.STOPPED;
					break;
			}	
			
			Bundle currentTrack = intent.getBundleExtra(PowerAMPiAPI.TRACK);
			if(currentTrack == null) {
				for (IPlayerStateListener listener : listeners.values()){
					listener.onStateChanged(playerState);
				}
				return;
			}
			
			String title = currentTrack.getString(PowerAMPiAPI.Track.TITLE);
			String artist = currentTrack.getString(PowerAMPiAPI.Track.ARTIST);
			String album = currentTrack.getString(PowerAMPiAPI.Track.ALBUM);
			
			StringBuilder sb = new StringBuilder();
			sb.append(artist);
			sb.append(" - ");
			sb.append(title);
			if (album != null && album.length()>0){
				sb.append(" (");
				sb.append(album);
				sb.append(")");
			}
			
			ServiceUtils.log(sb.toString());
			
			for (IPlayerStateListener listener : listeners.values()){
				listener.onStateChanged(playerState, sb.toString());
			}
		}
	};
	
	private final BroadcastReceiver trackReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent != null) {
				Bundle currentTrack = intent.getBundleExtra(PowerAMPiAPI.TRACK);
				if(currentTrack == null) {
					return;
				}
				
				String title = currentTrack.getString(PowerAMPiAPI.Track.TITLE);
				String artist = currentTrack.getString(PowerAMPiAPI.Track.ARTIST);
				String album = currentTrack.getString(PowerAMPiAPI.Track.ALBUM);
				
				StringBuilder sb = new StringBuilder();
				sb.append(artist);
				sb.append(" - ");
				sb.append(title);
				if (album != null && album.length()>0){
					sb.append(" (");
					sb.append(album);
					sb.append(")");
				}
				
				ServiceUtils.log(sb.toString());
				
				for (IPlayerStateListener listener : listeners.values()){
					listener.onStateChanged(IPlayerStateListener.TRACK, sb.toString());
				}
			}
		}
	};
	
	public PowerAmpStateListener(RuntimeService runtimeService) {
		this.service = runtimeService;
	}

	private void register() {
		service.registerReceiver(playStatusReceiver, new IntentFilter(PowerAMPiAPI.ACTION_STATUS_CHANGED));
		service.registerReceiver(trackReceiver, new IntentFilter(PowerAMPiAPI.ACTION_TRACK_CHANGED));
	}

	public void unregister(){
		service.unregisterReceiver(playStatusReceiver);
		service.unregisterReceiver(trackReceiver);
	}
	
	public void addPlayerStateListener(int serviceId, IPlayerStateListener listener){
		if (listeners.size() < 1){
			register();
		}
		
		listeners.put(serviceId, listener);
	}
	
	public void removePlayerStateListener(int serviceId){
		listeners.remove(serviceId);
		
		if (listeners.size() < 1){
			unregister();
		}
	}
}
