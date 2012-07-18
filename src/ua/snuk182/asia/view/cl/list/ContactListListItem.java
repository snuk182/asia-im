package ua.snuk182.asia.view.cl.list;

import java.lang.ref.WeakReference;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.view.ViewUtils;
import ua.snuk182.asia.view.cl.ContactListItem;
import ua.snuk182.asia.view.more.BuddyImage;
import android.content.Context;
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

public class ContactListListItem extends RelativeLayout implements ContactListItem, Comparable<ContactListListItem> {
	
	private byte status = Buddy.ST_OFFLINE;
	
	public TextView name;
	public ImageView mainStatusIcon;
	public ImageView xStatusIcon;
	//public ImageView clientAppIcon;
	public LinearLayout nameLayout;
	public BuddyImage picLayout;
	public TextView xStatusText;
	public ImageView authIcon;
	
	public static int itemHeight = 48;
	
	//private static int defaultImageResource = R.drawable.contact_48px;
	private static float nameTextSize = 14;
	private static float statusTextSize = 10;
	
	private WeakReference<Bitmap> icon;
	private final ScrollView scroller;
	private BitmapDrawable bd;
	
	private final Runnable iconGot = new Runnable(){

		@Override
		public void run() {
			if (icon != null && icon.get() != null){
				int picSize = itemHeight;
				if (EntryPoint.bgColor == EntryPoint.BGCOLOR_WALLPAPER){
					picSize -= 4;
				}
				bd = new BitmapDrawable(ViewUtils.scaleBitmap(icon.get(), (int) ((picSize) * getEntryPoint().metrics.density), false, false));
				bd.setFilterBitmap(false);
				bd.setDither(false);
				bd.setGravity(Gravity.CENTER);
				picLayout.setBuddyImage(bd);
			} else {
				picLayout.setBuddyImage(R.drawable.dummy_48);
			}
		}		
	};
	
	public boolean showIcons = false;
	
	public String iconId = "";

	public ContactListListItem(Context context, String tag, ScrollView scroller) {
		super(context, null);
		setClickable(true);
		
		LayoutInflater inflate = LayoutInflater.from(context);
		inflate.inflate(R.layout.contact_list_list_item, this); 
		
		name = (TextView) findViewById(R.id.usernamelabel);
		mainStatusIcon = (ImageView) findViewById(R.id.statusimage);
		xStatusIcon = (ImageView) findViewById(R.id.xstatusimage);
		picLayout = (BuddyImage)findViewById(R.id.iconlayout);
		authIcon = (ImageView) findViewById(R.id.authimage);
		xStatusText = (TextView) findViewById(R.id.xstatuslabel);  
		picLayout.setBuddyImage(R.drawable.dummy_48);
		
		this.scroller = scroller;
		
		int size = (int) (itemHeight * getEntryPoint().metrics.density);
		RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(size, size);
		layout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		layout.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		layout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		picLayout.setLayoutParams(layout);
		
		setPadding(2,2,2,2);	
		
		setTag(tag);
		//setBackgroundResource(R.drawable.cl_item);
		setOnFocusChangeListener(this);
	}
	
	public void populate(Buddy buddy, boolean showIcons){
		if (!buddy.getFullUid().equals(getTag())){
			return;
		}
		
		name.setText(buddy.getName());
		name.setTextSize(nameTextSize);
		xStatusText.setTextSize(statusTextSize);
	       
		setFocusable(true);
		//setFocusableInTouchMode(true);
		
		switch(itemHeight){
		case 24:
			mainStatusIcon.setImageResource(ServiceUtils.getStatusResIdByBuddySmall(getContext(), buddy));
			break;
		case 32:
			mainStatusIcon.setImageResource(ServiceUtils.getStatusResIdByBuddyMedium(getContext(), buddy));
			break;
		case 48:
			mainStatusIcon.setImageResource(ServiceUtils.getStatusResIdByBuddyBigger(getContext(), buddy));
			break;
		default:
			mainStatusIcon.setImageResource(ServiceUtils.getStatusResIdByBuddyBig(getContext(), buddy));
			break;
		}
		
		status = buddy.status;
		
		switch(buddy.status){
		case Buddy.ST_ONLINE:
			xStatusText.setText(R.string.label_st_online);
		    break;
		case Buddy.ST_FREE4CHAT:
			xStatusText.setText(R.string.label_st_free4chat);
		    break;
		case Buddy.ST_AWAY:
			xStatusText.setText(R.string.label_st_away);
		    break;
		case Buddy.ST_BUSY:
			xStatusText.setText(R.string.label_st_busy);
		    break;
		case Buddy.ST_DND:
			xStatusText.setText(R.string.label_st_dnd);
		    break;
		case Buddy.ST_INVISIBLE:
			xStatusText.setText(R.string.label_st_invisible);
		    break;
		case Buddy.ST_NA:
			xStatusText.setText(R.string.label_st_na);
		    break;
		case Buddy.ST_OFFLINE:
			xStatusText.setText(R.string.label_st_offline);
		    break;
		case Buddy.ST_ANGRY:
			xStatusText.setText(R.string.label_st_angry);
		    break;
		case Buddy.ST_DEPRESS:
			xStatusText.setText(R.string.label_st_depress);
		    break;
		case Buddy.ST_DINNER:
			xStatusText.setText(R.string.label_st_dinner);
		    break;
		case Buddy.ST_HOME:
			xStatusText.setText(R.string.label_st_home);
		    break;
		case Buddy.ST_WORK:
			xStatusText.setText(R.string.label_st_work);
		    break;
		}
		        
		if (buddy.xstatus>-1){
			TypedArray xNames = ServiceUtils.getXStatusArray(getContext(), buddy.serviceName);
			int xImageId = xNames.getResourceId(buddy.xstatus, 0);
			if (xImageId != 0){
				xStatusIcon.setVisibility(View.VISIBLE);
				xStatusIcon.setImageResource(xImageId);
			}
		} else {
			xStatusIcon.setVisibility(View.GONE);
		}
		
		if (buddy.xstatusName!=null){
			xStatusText.setText(buddy.xstatusName+(buddy.xstatusDescription!=null?(" "+buddy.xstatusDescription):""));
		}
		        
		if (buddy.unread>0){
			switch(itemHeight){
			case 24:
				mainStatusIcon.setImageResource(R.drawable.message_tiny);
				break;
			case 32:
				mainStatusIcon.setImageResource(R.drawable.message_medium);
				break;
			default:
				mainStatusIcon.setImageResource(R.drawable.message_big);
				break;
			}
		} 
		
		/*if (buddy.visibility == Buddy.VIS_NOT_AUTHORIZED){
			authIcon.setVisibility(View.VISIBLE);
		} else {
			authIcon.setVisibility(View.GONE);
		}*/
		
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
		
		if (showIcons && !this.showIcons){
			this.showIcons = showIcons;			
			visibility2IconAction(scroller.getScrollY(), scroller.getScrollY() + (scroller.getBottom()-scroller.getTop()));
		} else {
			this.showIcons = showIcons;
		}
		
		if (this.showIcons){
			picLayout.setVisibility(View.VISIBLE);
		} else {
			picLayout.setVisibility(View.GONE);
		}
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
	
	public void removeFromParent(){
		ViewGroup parent = (ViewGroup) getParent();
		if (parent != null){
			parent.removeView(this);
		}
	}

	@Override
	public int compareTo(ContactListListItem another) {
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
		icon = null;
		if (showIcons){
			visibility2IconAction(scroller.getScrollY(), scroller.getScrollY() + (scroller.getBottom()-scroller.getTop()));
		} else {
			picLayout.setBuddyImage(R.drawable.dummy_48);
		}
	}
	
	public static void resize(int itemHeight){
		ContactListListItem.itemHeight = itemHeight;
		//picLayout.setLayoutParams(new RelativeLayout.LayoutParams(size, size));		
		switch(itemHeight){
		case 24:
			nameTextSize = 8;
			statusTextSize = 5;
			break;
		case 32:
			nameTextSize = 12;
			statusTextSize = 7;
			break;
		case 48:
			nameTextSize = 18;
			statusTextSize = 10;
			break;
		default:
			nameTextSize = 25;
			statusTextSize = 13;
			break;
		}
	}
	
	public EntryPoint getEntryPoint() {
		return (EntryPoint) getContext();
	}

	@Override
	public void populate(Buddy buddy) {
		populate(buddy, showIcons);
	}
	
	@Override
	public void color(){
		ViewUtils.styleTextView(name);
		picLayout.onFocusChange(null, false);
	}

	@Override
	public void onDrawerScrolled(int parentTop, int parentBottom) {
		visibility2IconAction(parentTop, parentBottom);
	}

	private void visibility2IconAction(int parentTop, int parentBottom) {
		if (checkMeVisible(parentTop, parentBottom)){
			requestIconInternal(getTag().toString());
		} else {
			picLayout.setBuddyImage(null);
		}
	}
	
	private void requestIconInternal(final String filename) {
		mainStatusIcon.setVisibility(View.VISIBLE);
		
		getEntryPoint().threadMsgHandler.post(new Runnable() {
			
			@Override
			public void run(){
				icon = new WeakReference<Bitmap>(Buddy.getIcon(getEntryPoint(), filename, icon == null));
				
				//getEntryPoint().threadMsgHandler.post(iconGot);
				iconGot.run();
			}
		});
	}

	private boolean checkMeVisible(int parentTop, int parentBottom) {
		View parent = (View) getParent();		
		if (parent == null){
			//we're not drawn yet
			return false;
		}
		
		Rect rect = new Rect();		
		if (parent instanceof LinearLayout && (((LinearLayout)parent).getOrientation() == LinearLayout.VERTICAL)){
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
		
		if (changed && scroller != null){
			visibility2IconAction(scroller.getScrollY(), scroller.getScrollY() + (scroller.getBottom()-scroller.getTop()));
		}
	}
}
