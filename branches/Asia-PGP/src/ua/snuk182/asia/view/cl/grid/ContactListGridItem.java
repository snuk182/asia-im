package ua.snuk182.asia.view.cl.grid;

import java.lang.ref.WeakReference;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.view.ViewUtils;
import ua.snuk182.asia.view.cl.ContactListItem;
import ua.snuk182.asia.view.more.BuddyImage;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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
	
	private WeakReference<Bitmap> icon;
	private BitmapDrawable bd;
	private final ScrollView scroller;
	
	public static int itemSize = 80;
	//private static int defaultImageResource = R.drawable.contact_48px;
	private static float textSize = 12;
	
	private final Runnable iconGot = new Runnable(){

		@Override
		public void run() {
			if (icon != null && icon.get() != null){
				int picSize = itemSize;
				if (EntryPoint.bgColor == EntryPoint.BGCOLOR_WALLPAPER){
					picSize -= 15;
				}
				bd = new BitmapDrawable(ViewUtils.scaleBitmap(icon.get(), (int) ((picSize) * getEntryPoint().metrics.density), false, false));
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
	
	public ContactListGridItem(EntryPoint context, String tag, ScrollView scroller) {
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
		
		this.scroller = scroller;
		
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
		if (!buddy.getFullUid().equals(getTag())){
			return;
		}
		if (layout == null){
			setLayoutParams(new LinearLayout.LayoutParams(itemSize, itemSize));
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
		
		switch(buddy.secureOptions){
		case Buddy.SECURE_SUPPORTS:
			authIcon.setVisibility(View.VISIBLE);
			authIcon.setImageResource(R.drawable.lock_open_tiny);
			break;
		case Buddy.SECURE_ENABLED:
			authIcon.setVisibility(View.VISIBLE);
			authIcon.setImageResource(R.drawable.lock_closed_tiny);
			break;
		case Buddy.SECURE_NOSUPPORT:
			
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
			
			break;
		}
		
		if (showIcons){
			if (!this.showIcon){
				this.showIcon = showIcons;
				visibility2IconAction(scroller.getScrollY(), scroller.getScrollY() + (scroller.getBottom()-scroller.getTop()));
			} 			
		} else {
			this.showIcon = showIcons;
			setNoBuddyImageMode(buddy);			
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
	public void requestIcon(Buddy buddy){
		icon = new WeakReference<Bitmap>(null);
		if (showIcon){
			visibility2IconAction(scroller.getScrollY(), scroller.getScrollY() + (scroller.getBottom()-scroller.getTop()));
		} else {
			setNoBuddyImageMode(buddy);
		}
	}
	
	private void requestIconInternal(final String filename) {
		mainStatusIcon.setVisibility(View.VISIBLE);
		
		getEntryPoint().threadMsgHandler.post(new Runnable() {
			
			@Override
			public void run(){
				if (icon != null && icon.get() != null){
					buddyImage.setBuddyImage(null);
					icon.get().recycle();
					icon.clear();
				}
				
				icon = new WeakReference<Bitmap>(Buddy.getIcon(getEntryPoint(), filename, (icon != null && icon.get() == null)));
				
				//getEntryPoint().threadMsgHandler.post(iconGot);
				iconGot.run();
			}
		});
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
	public void onDrawerScrolled(int parentTop, int parentBottom) {
		if (showIcon){
			visibility2IconAction(parentTop, parentBottom);
		}
	}

	private void visibility2IconAction(int parentTop, int parentBottom) {
		if (checkMeVisible(parentTop, parentBottom)){
			requestIconInternal(getTag().toString());
		} else {
			buddyImage.setBuddyImage(null);
		}
	}

	private boolean checkMeVisible(int parentTop, int parentBottom) {
		View parent = (View) getParent();		
		if (parent == null){
			//we're not drawn yet
			return false;
		}
		
		Rect rect = new Rect();		
		if (parent instanceof ScrollView){
			getHitRect(rect);
		} else {
			parent.getHitRect(rect);
		}
		
		return (rect.top > parentTop-50) && (rect.bottom < parentBottom+50);
	}

	@Override
	public void setTag(String tag) {
		super.setTag(tag);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom){
		super.onLayout(changed, left, top, right, bottom);
		
		if (showIcon && changed && scroller != null){
			visibility2IconAction(scroller.getScrollY(), scroller.getScrollY() + (scroller.getBottom()-scroller.getTop()));
		}
	}
}
