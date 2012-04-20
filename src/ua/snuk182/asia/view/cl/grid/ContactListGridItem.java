package ua.snuk182.asia.view.cl.grid;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.view.ViewUtils;
import ua.snuk182.asia.view.cl.ContactListItem;
import ua.snuk182.asia.view.more.BuddyImage;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ContactListGridItem extends RelativeLayout implements ContactListItem, Comparable<ContactListGridItem> {
	
	private byte status = Buddy.ST_OFFLINE;
	
	public TextView name;
	public ImageView mainStatusIcon;
	public ImageView xStatusIcon;
	public ImageView unreadMsgIcon;
	public ImageView authIcon;
	public LinearLayout iconLayout;
	public LinearLayout nameLayout;
	public BuddyImage buddyImage;
	
	public boolean showIcon = false;
	
	private Bitmap icon;
	
	public static int itemSize = 80;
	//private static int defaultImageResource = R.drawable.contact_48px;
	private static float textSize = 12;
	
	private final Runnable iconGot = new Runnable(){

		@Override
		public void run() {
			if (icon != null){
				int picSize = itemSize;
				if (EntryPoint.bgColor == EntryPoint.BGCOLOR_WALLPAPER){
					picSize -= 15;
				}
				BitmapDrawable bd = new BitmapDrawable(ViewUtils.scaleBitmap(icon, (int) ((picSize) * getEntryPoint().metrics.density), false));
				bd.setFilterBitmap(false);
				bd.setDither(false);
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
		LayoutInflater inflate = LayoutInflater.from(context);
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
		
		setTag(tag);
		//setBackgroundResource(R.drawable.cl_item);
		setOnFocusChangeListener(this);
	}
	
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		buddyImage.onFocusChange(v, hasFocus);
	}	
	
	@Override
	public void populate(Buddy buddy){
		populate(buddy, showIcon);
	}
	
	public static void resize(int itemSize, EntryPoint entryPoint){
		ContactListGridItem.itemSize = (int) (itemSize * entryPoint.metrics.density);

		//picLayout.setLayoutParams(new RelativeLayout.LayoutParams(size, size));		
		switch(itemSize){
		case 75:
			//defaultImageResource = R.drawable.contact_64px;
			textSize = 12;
			break;
		case 96:
			//defaultImageResource = R.drawable.contact_64px;
			textSize = 16;
			break;
		case 62:
			//defaultImageResource = R.drawable.contact_64px;
			textSize = 8;
			break;
		default:
			//defaultImageResource = R.drawable.contact_48px;
			textSize = 4;
			break;
		}
	}
	
	public void populate(Buddy buddy, boolean showIcons){
		populate(buddy, showIcons, null);
	}
	
	public void populate(Buddy buddy, boolean showIcons, ViewGroup.LayoutParams layout){
		if (!buddy.protocolUid.equals(getTag())){
			return;
		}
		if (layout == null){
			setLayoutParams(new LayoutParams(itemSize, itemSize));
		}
		
		//setGravity(Gravity.CENTER);					
		setPadding(2,1,2,1);
		       
		name.setText(buddy.getName());
		name.setTextSize(textSize);
		        
		setFocusable(true);
		//setFocusableInTouchMode(true);
		
		status = buddy.status;
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

	public void removeFromParent(){
		ViewGroup parent = (ViewGroup) getParent();
		if (parent != null){
			parent.removeView(this);
		}
	}

	@Override
	public int compareTo(ContactListGridItem another) {
		if (status != another.status){
			if (status == Buddy.ST_OFFLINE){
				return 1;
			}
			if (another.status == Buddy.ST_OFFLINE){
				return -1;
			}
		}
		return name.getText().toString().compareToIgnoreCase(another.name.getText().toString());
	}
	
	@Override
	public void requestIcon(final Buddy buddy){
		if (showIcon){
			mainStatusIcon.setVisibility(View.VISIBLE);
			new Thread("CL grid item icon request"){
				@Override
				public void run(){
					finalizeBitmap();
					
					icon = Buddy.getIcon(getEntryPoint(), buddy.getFilename());
					if (icon != null){
						ViewUtils.VMRUNTIME.allocBitmap(icon);
					}
					
					getEntryPoint().threadMsgHandler.post(iconGot);
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
	
	@Override
	public void color(){
		ViewUtils.styleTextView(name);
		buddyImage.onFocusChange(null, false);
		int pad2;
		
		if (EntryPoint.bgColor != 0xff7f7f80){
			pad2 = (int) (7*getEntryPoint().metrics.density);				
		} else {
			pad2 = 0;					
		} 	
		
		findViewById(R.id.namelayout).setPadding(0, pad2, pad2, 0);
	}

	@Override
	public void setTag(String tag) {
		super.setTag(tag);
	}
	
	@Override
	protected void finalize() throws Throwable{
		try {
			finalizeBitmap();
		} finally {
			super.finalize();
		}		
	}

	private void finalizeBitmap() {
		if (icon != null){
			ViewUtils.VMRUNTIME.freeBitmap(icon);
			ServiceUtils.log("Bitmap for "+getTag()+" finalized");
		}
	}
}
