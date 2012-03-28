package ua.snuk182.asia.services.api;

public class ProtocolException extends Exception {
	
	public final byte errorCode;
	
	public static final byte ERROR_NONE = 0;
	public static final byte ERROR_DEFAULT = 1;
	public static final byte ERROR_GROUPCHAT_ALREADY_EXISTS = 2;
	public static final byte ERROR_NO_GROUPCHAT_AVAILABLE = 3;
	
	public ProtocolException(String message) {
		this(ERROR_DEFAULT, message);
	}
	
	public ProtocolException(byte errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5676297681749673246L;

}
