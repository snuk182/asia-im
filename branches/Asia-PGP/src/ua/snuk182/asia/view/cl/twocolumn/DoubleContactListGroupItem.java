package ua.snuk182.asia.view.cl.twocolumn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.view.ViewUtils;
import ua.snuk182.asia.view.cl.list.ContactListListItem;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class DoubleContactListGroupItem extends LinearLayout implements OnClickListener{
	
	private static final String SPACE = "  ";
	public static int itemSize = 0;
	
	private TextView subGroupNameText;
	private TextView subGroupCountText;
	private int groupId = -1;
	byte serviceId = -1;
	private List<ContactListListItem> buddyList = new ArrayList<ContactListListItem>();
	private List<LinearLayout> buddyRows = new ArrayList<LinearLayout>();
	private ViewGroup ownerLayout;
	private boolean refreshContents = true;
	private final int columnCount = 2;
	private boolean collapsed = false;
	
	public DoubleContactListGroupItem(final EntryPoint entryPoint, AttributeSet attrs, ViewGroup owner, String tag, String name){
		super(entryPoint, attrs);
		
		ownerLayout = owner;
		
		LayoutInflater inflate = LayoutInflater.from(entryPoint);
		inflate.inflate(R.layout.contact_list_grid_panel_item, this); 
		setLayoutParams(new ListView.LayoutParams(
		        ListView.LayoutParams.FILL_PARENT,
		        ListView.LayoutParams.WRAP_CONTENT));
		setOrientation(LinearLayout.HORIZONTAL);
		
		setFocusable(false);
		setFocusableInTouchMode(false);
		
		subGroupNameText = (TextView) findViewById(R.id.subgroupnametext);
		subGroupCountText = (TextView) findViewById(R.id.subgroupcounttext);
		
		setTag(tag);
		subGroupNameText.setText(SPACE+name);
		
		setBackgroundResource(R.drawable.history_record_indicator);
		setOnClickListener(this);
	}
	
	public synchronized void refresh(boolean refreshLayout){
		if (refreshLayout){
			/*for (ContactListListItem item: buddyList){
				item.setLayoutParams(new LinearLayout.LayoutParams(DoubleContactListGroupItem.itemSize, LayoutParams.WRAP_CONTENT, 1));
			}*/
			for (LinearLayout item : buddyRows) {
				for (int i=0; i<item.getChildCount(); i++){
					if (!item.getChildAt(i).getTag().equals("")){
						item.getChildAt(i).setLayoutParams(new LinearLayout.LayoutParams(DoubleContactListGroupItem.itemSize, item.getHeight(), 1f));
					}
				}
			}
		}
		if (refreshContents){
			forceRefresh();
			refreshContents = false;
		} else {
			ownerLayout.removeView(this);
			ownerLayout.addView(this);
			for (LinearLayout item : buddyRows) {
				ownerLayout.removeView(item);
				ownerLayout.addView(item);
			}
		}
		
		setCollapsedInternal(false);
	}
	
	public void forceRefresh(){
		Collections.sort(buddyList);
		
		ownerLayout.removeView(this);
		for (LinearLayout row : buddyRows) {
			ownerLayout.removeView(row);
			row.removeAllViews();
		}

		ownerLayout.addView(this);
		if (buddyList == null || columnCount < 1) {
			return;
		}
		
		subGroupCountText.setText(buddyList.size() + SPACE);
		
		int rowNum = buddyList.size() / columnCount + ((buddyList.size() % columnCount) > 0 ? 1 : 0);
		
		int currentRow = 0;
		int currentColumn = 0;
		
		for (Iterator<ContactListListItem> iterator = buddyList.iterator(); iterator.hasNext();){
			ContactListListItem buddyItem = iterator.next();
			
			final LinearLayout row;
			if (currentRow < buddyRows.size()){
				row = buddyRows.get(currentRow);
			} else {
				row = new LinearLayout(getContext());
				buddyRows.add(row);
				row.setOrientation(LinearLayout.HORIZONTAL);
				
				row.setGravity(Gravity.CENTER_HORIZONTAL);
				
				row.setFocusable(false);
				
				row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			}
			
			/*if (row.getParent() != null){
				((ViewGroup)row.getParent()).removeView(row);
			}
			ownerLayout.addView(row);*/
			if (row.getParent() == null){
				ownerLayout.addView(row);
			}
						
			if (currentColumn == 0){
				row.addView(newDivider());
			}
			
			buddyItem.removeFromParent();
			row.addView(buddyItem);

			row.addView(newDivider());
			
			currentColumn++;
			
			if (!iterator.hasNext()){
				for (;currentColumn < columnCount; currentColumn++){
					row.addView(newDummy(), new LinearLayout.LayoutParams(itemSize, LayoutParams.WRAP_CONTENT, 1));
					row.addView(newDivider());
				}
			}
			
			if (currentColumn >= columnCount){
				currentRow++;
				currentColumn = 0;
			}
		}

		if (buddyRows.size() > rowNum){
			for (int i=buddyRows.size()-1; i>=rowNum; i--){
				ownerLayout.removeView(buddyRows.remove(i));
			}
		}
		
		refreshContents = false;
	}
	
	private View newDummy() {
		final View dummy2 = new View(getContext());
		dummy2.setFocusable(false);
		dummy2.setTag("dummy");
		return dummy2;
	}

	private View newDivider() {
		final View dummy = new View(getContext());
		dummy.setFocusable(false);
		dummy.setTag("");
		dummy.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1));
		return dummy;
	}
	
	public TextView getSubGroupNameText() {
		return subGroupNameText;
	}

	public TextView getSubGroupCountText() {
		return subGroupCountText;
	}

	@Override
	public void onClick(View v) {
		this.collapsed = !collapsed;
		setCollapsedInternal(true);						
	}
	
	public ContactListListItem removeItem(String uid){
		for (ContactListListItem cli:buddyList){
			if (cli.getTag().equals(uid)){
				buddyList.remove(cli);
				return cli;
			}
		}
		return null;
	}

	public boolean isRefreshContents() {
		return refreshContents;
	}

	public void setRefreshContents(boolean refreshContents) {
		this.refreshContents = refreshContents;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getGroupId() {
		return groupId;
	}
	
	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
		setCollapsedInternal(false);
	}

	private void setCollapsedInternal(boolean save) {
		for (int i = 0; i < buddyRows.size(); i++) {
			View view = buddyRows.get(i);
			if (collapsed) {
				view.setVisibility(View.GONE);
			} else {
				view.setVisibility(View.VISIBLE);
			}
		}
		if (save && serviceId != -1 && groupId != -1){
			try {
				getEntryPoint().runtimeService.setGroupCollapsed(serviceId, groupId, collapsed);
			} catch (RemoteException e) {
				ServiceUtils.log(e);
			}
		}
	}

	public boolean isCollapsed() {
		return collapsed;
	}
	
	public EntryPoint getEntryPoint() {
		return (EntryPoint) getContext();
	}
	
	public List<ContactListListItem> getBuddyList() {
		return buddyList;
	}

	public void resize(int size){
		float textSize;
		switch(size){
		case 64:
			textSize = 26 * getEntryPoint().metrics.density;
			break;
		case 48:
			textSize = 20 * getEntryPoint().metrics.density;
			break;
		case 32:
			textSize = 14 * getEntryPoint().metrics.density;
			break;
		default:
			textSize = 9 * getEntryPoint().metrics.density;
			break;
		}
		subGroupNameText.setTextSize(textSize);
		subGroupCountText.setTextSize(textSize);
	}
	
	public void color() {
		ViewUtils.styleTextView(subGroupCountText);
		ViewUtils.styleTextView(subGroupNameText);
	}
}
