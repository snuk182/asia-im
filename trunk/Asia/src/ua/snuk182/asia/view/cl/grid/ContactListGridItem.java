package ua.snuk182.asia.view.cl.grid;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.view.ViewUtils;
import ua.snuk182.asia.view.more.BuddyImage;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ContactListGridItem extends RelativeLayout implements Comparable<ContactListGridItem>, OnFocusChangeListener {
	
	public TextView name;
	public ImageView mainStatusIcon;
	public ImageView xStatusIcon;
	public ImageView unreadMsgIcon;
	public ImageView authIcon;
	public LinearLayout iconLayout;
	public LinearLayout nameLayout;
	/*public ImageView picLayout;
	public ImageView topLayout;*/
	public BuddyImage buddyImage;
	
	public boolean showIcon = false;
	
	private Bitmap icon;
	
	private final Handler handler = new Handler();
	
	private final Runnable iconGot = new Runnable(){

		@Override
		public void run() {
			if (icon != null){
				BitmapDrawable bd = new BitmapDrawable(ViewUtils.scaleBitmap(icon, (int) (60 * getEntryPoint().metrics.density), false));
				bd.setGravity(Gravity.CENTER);
				buddyImage.setBuddyImage(bd);
			} else {
				buddyImage.setBuddyImage(R.drawable.dummy_48);
			}
		}		
	};
	
	public String iconId = "";
	
	public ContactListGridItem(EntryPoint context, String tag) {
		super(context, null);
		LayoutInflater inflate = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflate.inflate(R.layout.contact_list_grid_item, this); 
		
		setClickable(true);
		name = (TextView) findViewById(R.id.usernamelabel);
		mainStatusIcon = (ImageView) findViewById(R.id.statusimage);
		xStatusIcon = (ImageView) findViewById(R.id.xstatusimage);
		authIcon = (ImageView) findViewById(R.id.authimage);
		iconLayout = (LinearLayout)findViewById(R.id.iconlayout);
		//nameLayout = (LinearLayout) findViewById(R.id.nameLayout);  
		unreadMsgIcon = (ImageView) findViewById(R.id.unreadimage);
		//picLayout.setImageResource(R.drawable.contact_64px);
		buddyImage = (BuddyImage) findViewById(R.id.buddyimage);
		buddyImage.setBuddyImage(R.drawable.dummy_48);
		
		setTag(tag);
		//setBackgroundResource(R.drawable.cl_item);
		setOnFocusChangeListener(this);
	}
	
	public void populate(Buddy buddy){
		populate(buddy, -1, showIcon);
	}
	
	public void populate(Buddy buddy, int itemSize, boolean showIcons){
		if (!buddy.protocolUid.equals(getTag())){
			return;
		}
		if (itemSize > 0){
			setLayoutParams(new LayoutParams(itemSize, itemSize));
		}
		//setGravity(Gravity.CENTER);					
		setPadding(2,2,2,2);
		       
		name.setText(buddy.getName());
		        
		setFocusable(true);
		//setFocusableInTouchMode(true);
				
		mainStatusIcon.setImageResource(ServiceUtils.getStatusResIdByBuddyTiny(getContext(), buddy));
		
		if (buddy.status != Buddy.ST_OFFLINE && buddy.xstatus>-1){
			TypedArray xNames = ServiceUtils.getXStatusArray(getContext(), buddy.serviceName);
			int xImageId = xNames.getResourceId(buddy.xstatus, 0);
			if (xImageId != 0){
				xStatusIcon.setVisibility(View.VISIBLE);
				xStatusIcon.setImageResource(xImageId);
			}
		} else {
			xStatusIcon.setVisibility(View.GONE);
		}
		
		if (buddy.unread>0){
		   unreadMsgIcon.setVisibility(View.VISIBLE);
		} else {
		   unreadMsgIcon.setVisibility(View.GONE);
		}
		
		switch(buddy.visibility){
		case Buddy.VIS_PERMITTED:
			authIcon.setVisibility(View.VISIBLE);
			authIcon.setImageResource(R.drawable.permitted);
			break;
		case Buddy.VIS_DENIED:
			authIcon.setVisibility(View.VISIBLE);
			authIcon.setImageResource(R.drawable.denied);
			break;
		case Buddy.VIS_NOT_AUTHORIZED:
			authIcon.setVisibility(View.VISIBLE);
			authIcon.setImageResource(R.drawable.not_authorized);
			break;
		default:
			authIcon.setVisibility(View.GONE);
			break;	
		}
		
		if (showIcons){
			if (!this.showIcon){
				this.showIcon = showIcons;
				requestIcon(buddy);
			} else {
				this.showIcon = showIcons;
			}
			mainStatusIcon.setVisibility(View.VISIBLE);
		} else {
			setNoBuddyImageMode(buddy);
			this.showIcon = showIcons;
		}
		
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus){
			buddyImage.setTopImage(R.drawable.contact_sel_64px);
			
		} else {
			buddyImage.setTopImage(R.drawable.contact_64px);
		}
	}
	
	public void removeFromParent(){
		ViewGroup parent = (ViewGroup) getParent();
		if (parent != null){
			parent.removeView(this);
		}
	}

	@Override
	public int compareTo(ContactListGridItem another) {
		return name.getText().toString().compareToIgnoreCase(another.name.getText().toString());
	}
	
	public void requestIcon(final Buddy buddy){
		if (showIcon){
			mainStatusIcon.setVisibility(View.VISIBLE);
			new Thread("CL grid item icon request"){
				@Override
				public void run(){
					Bitmap b = buddy.getIcon(getEntryPoint(), getLayoutParams().height);
						if (b != null){
							icon = b;
							handler.post(iconGot);
						}
				}
			}.start();
		} else {
			setNoBuddyImageMode(buddy);
		}
	}
	
	private void setNoBuddyImageMode(Buddy buddy){
		mainStatusIcon.setVisibility(View.GONE);
		//picLayout.setImageResource(ServiceUtils.getStatusResIdByBuddy(getContext(), buddy, getLayoutParams().width));
		buddyImage.setBuddyImage(ServiceUtils.getStatusResIdByBuddyBig(getContext(), buddy));
	}
	
	public EntryPoint getEntryPoint() {
		return (EntryPoint) getContext();
	}
}