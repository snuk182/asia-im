package ua.snuk182.asia.view.more.musiccontrol;

import java.util.HashMap;
import java.util.Map;

import ua.snuk182.asia.services.RuntimeService;
import ua.snuk182.asia.services.ServiceUtils;

public abstract class AbstractPlayerStateListener {

	protected final RuntimeService service;

	protected final Map<Integer, IPlayerStateListener> listeners = new HashMap<Integer, IPlayerStateListener>();

	public AbstractPlayerStateListener(RuntimeService runtimeService) {
		this.service = runtimeService;
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
			try{
				unregister();
			} catch (IllegalArgumentException e){
				ServiceUtils.log(e);
			}
		}
	}
	
	protected void notifyTrack(String title, String artist, String album) {
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
	
	public abstract void register();
	public abstract void unregister();	
}
