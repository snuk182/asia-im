package ua.snuk182.asia.view.more.musiccontrol.poweramp;

import ua.snuk182.asia.services.RuntimeService;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.view.more.musiccontrol.AbstractPlayerStateListener;
import ua.snuk182.asia.view.more.musiccontrol.IPlayerStateListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public class PowerAmpStateListener extends AbstractPlayerStateListener {
	
	public PowerAmpStateListener(RuntimeService runtimeService) {
		super(runtimeService);
	}

	private final BroadcastReceiver playStatusReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean paused = true;
			
			int status = intent.getIntExtra(PowerAMPiAPI.STATUS, -1);
			int playerState = IPlayerStateListener.NONE;
			
			switch(status) {
				case PowerAMPiAPI.Status.TRACK_PLAYING:
					paused = intent.getBooleanExtra(PowerAMPiAPI.PAUSED, false);
					if (!paused){
						playerState = IPlayerStateListener.STARTED;
					} else {
						playerState = IPlayerStateListener.STOPPED;
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
			
			ServiceUtils.log("Poweramp plays "+title+" / "+artist+ " / "+album);
			
			notifyTrack(title, artist, album);
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
				
				PowerAmpStateListener.this.notifyTrack(title, artist, album);
			}
		}
	};
	
	public void register() {
		service.registerReceiver(playStatusReceiver, new IntentFilter(PowerAMPiAPI.ACTION_STATUS_CHANGED));
		service.registerReceiver(trackReceiver, new IntentFilter(PowerAMPiAPI.ACTION_TRACK_CHANGED));
	}

	public void unregister(){
		service.unregisterReceiver(playStatusReceiver);
		service.unregisterReceiver(trackReceiver);
	}	
}
