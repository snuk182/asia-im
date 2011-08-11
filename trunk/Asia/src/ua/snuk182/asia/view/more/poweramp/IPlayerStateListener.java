package ua.snuk182.asia.view.more.poweramp;

public interface IPlayerStateListener {
	
	public void onStateChanged(int status, Object... properties);

	public static final int STARTED = 0;
	public static final int STOPPED = 1;
	public static final int TRACK = 2;
}
