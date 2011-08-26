package ua.snuk182.asia.view.cl.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.services.ServiceUtils;
import android.content.Context;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ContactListListGroupItem extends LinearLayout implements OnClickListener, OnFocusChangeListener{
	
	private static final String SPACE = "  ";
	private TextView subGroupNameText;
	private TextView subGroupCountText;
	public int groupId = -1;
	public byte serviceId = -1;
	private List<ContactListListItem> buddyList = new ArrayList<ContactListListItem>();
	private List<ContactListListItem> buddyRows = new ArrayList<ContactListListItem>();
	private ViewGroup ownerLayout;
	private boolean refreshContents = false;
	private boolean sort = true;
	private boolean collapsed = false;
	
	public ContactListListGroupItem(final EntryPoint entryPoint, AttributeSet attrs, ViewGroup owner, String tag, String name) {
		super(entryPoint, attrs);
		
		ownerLayout = owner;
		
		LayoutInflater inflate = (LayoutInflater)entryPoint.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		
		//setBackgroundResource(R.drawable.cl_item);
		setOnClickListener(this);	
		setOnFocusChangeListener(this);
	}
	
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus){
			//setSelected(true);
			setBackgroundColor(0xddff7f00);
		} else {
			setBackgroundColor(0x00ffffff);
			//setSelected(false);
		}
	}
	
	public void refresh(String bgType){
		if (refreshContents){
			forceRefresh(bgType, sort);
			refreshContents = false;
		} else {
			ownerLayout.removeView(this);
			ownerLayout.addView(this);
			for (ContactListListItem item : buddyRows) {
				ownerLayout.removeView(item);
				ownerLayout.addView(item);
			}
		}
		
		setCollapsed(collapsed);
	}
	
	public void forceRefresh(String bgType, boolean sort){
		this.sort = sort;
		
		if (sort){
			Collections.sort(buddyList);
		}
		
		ownerLayout.removeView(this);
		
		ownerLayout.addView(this);
		if (buddyList == null){
			return;
		}
		
		buddyRows.clear();
		
		getSubGroupCountText().setText(buddyList.size()+SPACE);
		setFocusable(true);
		//setFocusableInTouchMode(true);
		
		for (ContactListListItem cli:buddyList){
			
			cli.removeFromParent();
			
			buddyRows.add(cli);
			ownerLayout.addView(cli);
		}
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
	
	public ContactListListItem removeItem(String protocolUid){
		for (ContactListListItem cli:buddyList){
			if (cli.getTag().equals(protocolUid)){
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

	public EntryPoint getEntryPoint(){
		return (EntryPoint) getContext();
	}

	public List<ContactListListItem> getBuddyList() {
		return buddyList;
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
	
	public void resize(int size){
		float textSize;
		switch(size){
		case 64:
			textSize = 31 * getEntryPoint().metrics.density;
			break;
		case 48:
			textSize = 24 * getEntryPoint().metrics.density;
			break;
		case 32:
			textSize = 18 * getEntryPoint().metrics.density;
			break;
		default:
			textSize = 13 * getEntryPoint().metrics.density;
			break;
		}
		subGroupNameText.setTextSize(textSize);
		subGroupCountText.setTextSize(textSize);
	}

	public boolean isCollapsed() {
		return collapsed;
	}
}
