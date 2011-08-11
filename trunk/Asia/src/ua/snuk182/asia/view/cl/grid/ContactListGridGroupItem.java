package ua.snuk182.asia.view.cl.grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.services.ServiceUtils;
import android.content.Context;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ContactListGridGroupItem extends LinearLayout implements OnClickListener, OnFocusChangeListener {

	private static final String SPACE = "  ";
	private TextView subGroupNameText;
	private TextView subGroupCountText;
	private int columnCount;
	private int itemSize;
	public int groupId = -1;
	public byte serviceId = -1;
	private List<ContactListGridItem> buddyList = new ArrayList<ContactListGridItem>();
	//private List<Buddy> buddyList = new ArrayList<Buddy>();
	private List<LinearLayout> buddyRows = new ArrayList<LinearLayout>();
	private ViewGroup ownerLayout;
	private boolean refreshContents = false;
	private boolean sort = true;
	private boolean collapsed = false;
	
	ContactListGridGroupItem(final EntryPoint entryPoint, AttributeSet attrs, ViewGroup owner, String tag, String name) {
		super(entryPoint, attrs);

		ownerLayout = owner;
		
		LayoutInflater inflate = (LayoutInflater) entryPoint.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflate.inflate(R.layout.contact_list_grid_panel_item, this);
		setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		setOrientation(LinearLayout.HORIZONTAL);

		setFocusable(true);
		//setFocusableInTouchMode(true);
		
		subGroupNameText = (TextView) findViewById(R.id.subgroupnametext);
		subGroupCountText = (TextView) findViewById(R.id.subgroupcounttext);

		setTag(tag);
		subGroupNameText.setText(SPACE + name);
		
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

	void refresh() {
		if (refreshContents) {
			refresh(0, 0, sort);
			refreshContents = false;
		} else {
			ownerLayout.removeView(this);
			ownerLayout.addView(this);
			for (LinearLayout row : buddyRows) {
				ownerLayout.removeView(row);
				ownerLayout.addView(row);
			}
		}
		
		setCollapsedInternal(false);
	}

	void refresh(int column_count, int item_size, boolean sort) {
		this.sort  = sort;
		
		if (sort){
			Collections.sort(buddyList);
		}
		ownerLayout.removeView(this);
		for (LinearLayout row : buddyRows) {
			ownerLayout.removeView(row);
			row.removeAllViews();
		}

		ownerLayout.addView(this);
		if (column_count > 0) {
			columnCount = column_count;
		}

		if (item_size > 0) {
			itemSize = item_size;
		}

		if (buddyList == null || columnCount < 1) {
			return;
		}

		subGroupCountText.setText(buddyList.size() + SPACE);
		
		int rowNum = buddyList.size() / columnCount + ((buddyList.size() % columnCount) > 0 ? 1 : 0);
		
		int currentRow = 0;
		int currentColumn = 0;
		
		for (Iterator<ContactListGridItem> iterator = buddyList.iterator(); iterator.hasNext();){
			ContactListGridItem buddyItem = iterator.next();
			
			final LinearLayout row;
			if (currentRow < buddyRows.size()){
				row = buddyRows.get(currentRow);
			} else {
				row = new LinearLayout(getContext());
				buddyRows.add(row);
				row.setOrientation(LinearLayout.HORIZONTAL);
				
				row.setGravity(Gravity.CENTER_HORIZONTAL);
				row.setPadding(8, 8, 8, 8);

				row.setFocusable(false);
				
				row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			}
			
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
					row.addView(newDummy(), new LinearLayout.LayoutParams(itemSize, itemSize, 1));
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
		
	}

	@Override
	public void onClick(View v) {
		this.collapsed = !collapsed;
		setCollapsedInternal(true);
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

	ContactListGridItem removeItem(String protocolUid) {
		for (ContactListGridItem bu : buddyList) {
			if (bu.getTag().equals(protocolUid)) {
				buddyList.remove(bu);
				return bu;
			}
		}
		return null;
	}

	List<ContactListGridItem> getBuddyList() {
		return buddyList;
	}

	public void setRefreshContents(boolean b) {
		refreshContents = b;		
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
}
