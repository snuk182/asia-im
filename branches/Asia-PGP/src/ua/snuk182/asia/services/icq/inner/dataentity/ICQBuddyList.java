package ua.snuk182.asia.services.icq.inner.dataentity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ICQBuddyList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5979496006961145797L;

	/*private List<ICQBuddy> buddyList = new ArrayList<ICQBuddy>();
	private List<ICQBuddyGroup> buddyGroupList = new ArrayList<ICQBuddyGroup>();*/
	public List<Integer> buddyGroupList = new ArrayList<Integer>();
	public Map<String, Short> permitList = new HashMap<String, Short>();
	public Map<String, Short> denyList = new HashMap<String, Short>();
	
	public List<ICQOnlineInfo> buddyInfos = Collections.synchronizedList(new ArrayList<ICQOnlineInfo>());
	
	public Date lastUpdateTime;
	public int itemNumber = 0;
	public byte ssiVersion;

	public List<ICQBuddy> notAuthList = Collections.synchronizedList(new ArrayList<ICQBuddy>());
	
	public ICQOnlineInfo getByUin(String uin){
		for (ICQOnlineInfo info:buddyInfos){
			if (info.uin.equals(uin)){
				return info;
			}
		}
		return null;
	}

	public ICQBuddy removeFromNotAuthListByUin(String uin) {
		for (int i=notAuthList.size()-1; i>=0; i--){
			if (notAuthList.get(i).uin.equals(uin)){
				return notAuthList.remove(i);
			}
		}
		return null;
	}
}
