package ua.snuk182.asia.view.cl.grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.view.ViewUtils;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ContactListGridGroupItem extends LinearLayout implements OnClickListener {

	private static final String SPACE = "  ";
	private TextView subGroupNameText;
	private TextView subGroupCountText;
	private int columnCount;
	public int groupId = -1;
	public byte serviceId = -1;
	private List<ContactListGridItem> buddyList = new ArrayList<ContactListGridItem>();
	//private List<Buddy> buddyList = new ArrayList<Buddy>();
	private List<LinearLayout> buddyRows = new ArrayList<LinearLayout>();
	private ViewGroup ownerLayout;
	private boolean refreshContents = true;
	private boolean collapsed = false;
	
	ContactListGridGroupItem(final EntryPoint entryPoint, AttributeSet attrs, ViewGroup owner, String tag, String name) {
		super(entryPoint, attrs);

		ownerLayout = owner;
		
		LayoutInflater inflate = LayoutInflater.from(entryPoint);
		inflate.inflate(R.layout.contact_list_grid_panel_item, this);
		setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		setOrientation(LinearLayout.HORIZONTAL);

		setFocusable(true);
		//setFocusableInTouchMode(true);
		
		subGroupNameText = (TextView) findViewById(R.id.subgroupnametext);
		subGroupCountText = (TextView) findViewById(R.id.subgroupcounttext);

		setTag(tag);
		subGroupNameText.setText(SPACE + name);
		
		setBackgroundResource(R.drawable.history_record_indicator);
		setOnClickListener(this);
	}
	
	void refresh() {
		if (refreshContents) {
			refresh(0);
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

	void refresh(int column_count) {
		Collections.sort(buddyList);
		ownerLayout.removeView(this);
		for (LinearLayout row : buddyRows) {
			ownerLayout.removeView(row);
			row.removeAllViews();
		}

		ownerLayout.addView(this);
		if (column_count > 0) {
			columnCount = column_count;
		}

		/*if (item_size > 0) {
			itemSize = item_size;
		}*/

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
				
				row.setPadding(8, 2, 8, 2);

				row.setFocusable(false);
				
				row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
				row.setGravity(Gravity.CENTER_HORIZONTAL);
			}
			
			if (row.getParent() == null){
				ownerLayout.addView(row);
			}
			/*if (row.getParent() != null){
				((ViewGroup)row.getParent()).removeView(row);
			}
			ownerLayout.addView(row);*/
						
			if (currentColumn == 0){
				row.addView(newDivider());
			}
			
			buddyItem.removeFromParent();
			row.addView(buddyItem);

			row.addView(newDivider());
			
			currentColumn++;
			
			if (!iterator.hasNext()){
				for (;currentColumn < columnCount; currentColumn++){
					row.addView(newDummy(), new LinearLayout.LayoutParams(ContactListGridItem.itemSize, ContactListGridItem.itemSize, 1));
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
		
		/*for (LinearLayout row : buddyRows) {
			row.requestLayout();
		}*/
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
	
	public void resize(int size){
		float textSize;
		switch(size){
		case 75:
			textSize = 20 * getEntryPoint().metrics.density;
			break;
		case 96:
			textSize = 26 * getEntryPoint().metrics.density;
			break;
		case 62:
			textSize = 14 * getEntryPoint().metrics.density;
			break;
		default:
			textSize = 9 * getEntryPoint().metrics.density;
			break;
		}
		subGroupNameText.setTextSize(textSize);
		subGroupCountText.setTextSize(textSize);
	}

	public boolean isCollapsed() {
		return collapsed;
	}
	
	public EntryPoint getEntryPoint() {
		return (EntryPoint) getContext();
	}

	public void color() {
		ViewUtils.styleTextView(subGroupCountText);
		ViewUtils.styleTextView(subGroupNameText);
	}
}
