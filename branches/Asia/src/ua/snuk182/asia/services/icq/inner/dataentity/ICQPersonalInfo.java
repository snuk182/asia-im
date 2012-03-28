package ua.snuk182.asia.services.icq.inner.dataentity;

import java.util.HashMap;
import java.util.Map;

public class ICQPersonalInfo {
	
	public String uin;
	public String nickname;
	public String firstName;
	public String lastName;
	public String email;
	public byte authRequired = -1;
	public short status = -1;
	public byte gender = -1;
	public short age = -1;
	
	public final Map<String, Object> params = new HashMap<String, Object>();
}
