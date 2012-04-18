package ua.snuk182.asia.view.more.musiccontrol;

public interface IPlayerStateListener {
	
	public void onStateChanged(int status, Object... properties);

	public static final int NONE = 0;
	public static final int STARTED = 1;
	public static final int STOPPED = 2;
	public static final int TRACK = 3;
}
