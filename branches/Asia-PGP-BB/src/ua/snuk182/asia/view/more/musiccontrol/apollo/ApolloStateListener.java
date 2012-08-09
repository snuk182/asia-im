package ua.snuk182.asia.view.more.musiccontrol.apollo;

import ua.snuk182.asia.services.RuntimeService;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.view.more.musiccontrol.AbstractPlayerStateListener;
import ua.snuk182.asia.view.more.musiccontrol.IPlayerStateListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class ApolloStateListener extends AbstractPlayerStateListener {
	
	public static final String PLAYSTATE_CHANGED = "com.andrew.apollo.playstatechanged";
	public static final String META_CHANGED = "com.andrew.apollo.metachanged";
	
	public ApolloStateListener(RuntimeService runtimeService) {
		super(runtimeService);
	}

	private final BroadcastReceiver playStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String artist = intent.getStringExtra("artist");
			String album = intent.getStringExtra("album");
			String track = intent.getStringExtra("track");
			boolean isPlaying = intent.getBooleanExtra("playing", true);
			
			if (!isPlaying){
				for (IPlayerStateListener listener : listeners.values()){
					listener.onStateChanged(IPlayerStateListener.STOPPED);
				}
				
				return;
			}
			
			ServiceUtils.log("Apollo plays "+track+" / "+artist+ " / "+album);
			
			notifyTrack(track, artist, album);			       
		}};

	@Override
	public void register() {
		service.registerReceiver(playStatusReceiver, new IntentFilter(PLAYSTATE_CHANGED));
		service.registerReceiver(playStatusReceiver, new IntentFilter(META_CHANGED));
	}

	@Override
	public void unregister(){
		service.unregisterReceiver(playStatusReceiver);
	}	
}

