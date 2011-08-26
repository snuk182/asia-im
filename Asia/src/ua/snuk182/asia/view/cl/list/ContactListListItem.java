package ua.snuk182.asia.view.cl.list;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.view.ViewUtils;
import android.content.Context;
import android.content.res.ColorStateList;
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

public class ContactListListItem extends RelativeLayout implements Comparable<ContactListListItem>, OnFocusChangeListener {
	
	public TextView name;
	public ImageView mainStatusIcon;
	public ImageView xStatusIcon;
	//public ImageView clientAppIcon;
	public LinearLayout nameLayout;
	public ImageView picLayout;
	public TextView xStatusText;
	public ImageView authIcon;
	
	public static int itemHeight = 48;
	
	private static int defaultImageResource = R.drawable.contact_48px;
	private static float nameTextSize = 14;
	private static float statusTextSize = 10;
	
	private Bitmap icon;
	
	private final Handler handler = new Handler();
	
	private final Runnable iconGot = new Runnable(){

		@Override
		public void run() {
			if (icon != null){
				BitmapDrawable bicon = new BitmapDrawable(ViewUtils.scaleBitmap(icon, (int) (itemHeight*getEntryPoint().metrics.density), false));
				bicon.setGravity(Gravity.CENTER);
				picLayout.setImageDrawable(bicon);
			} 
		}		
	};
	
	public boolean showIcons = false;
	
	public String iconId = "";

	public ContactListListItem(Context context, String tag) {
		super(context, null);
		setClickable(true);
		
		LayoutInflater inflate = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflate.inflate(R.layout.contact_list_list_item, this); 
		
		name = (TextView) findViewById(R.id.usernamelabel);
		mainStatusIcon = (ImageView) findViewById(R.id.statusimage);
		xStatusIcon = (ImageView) findViewById(R.id.xstatusimage);
		picLayout = (ImageView)findViewById(R.id.iconlayout);
		authIcon = (ImageView) findViewById(R.id.authimage);
		xStatusText = (TextView) findViewById(R.id.xstatuslabel);  
		picLayout.setImageResource(defaultImageResource);
		
		setPadding(2,2,2,2);	
		
		setTag(tag);
		//setBackgroundResource(R.drawable.cl_item);
		setOnFocusChangeListener(this);
	}
	
	public void populate(Buddy buddy, String bgType, boolean showIcons){
		if (!buddy.protocolUid.equals(getTag())){
			return;
		}
		
		if (bgType == null || bgType.equals("wallpaper")){
			name.setTextColor(0xffffffff);
		}else {
			try {
				int color = (int) Long.parseLong(bgType);
				name.setTextColor(ColorStateList.valueOf((color-0xff000000)>0x777777?0xff000000:0xffffffff));
			} catch (NumberFormatException e) {
				ServiceUtils.log(e);
			}
		}
		
		name.setText(buddy.getName());
		name.setTextSize(nameTextSize);
		xStatusText.setTextSize(statusTextSize);
	       
		setTag(buddy.protocolUid);
		        
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
		
		if (showIcons && !this.showIcons){
			this.showIcons = showIcons;			
			requestIcon(buddy);
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
		return name.getText().toString().compareToIgnoreCase(another.name.getText().toString());
	}
	
	public void requestIcon(final Buddy buddy){
		if (showIcons){
			new Thread("CL list item icon request"){
				@Override
				public void run(){
					Bitmap b = buddy.getIcon(getEntryPoint());
						if (b != null){
							icon = b;
							handler.post(iconGot);
						} else {
							picLayout.setImageResource(defaultImageResource);
						}
					
				}
			}.start();
		} else {
			picLayout.setImageResource(defaultImageResource);
		}
	}
	
	public static void resize(int itemHeight){
		ContactListListItem.itemHeight = itemHeight;
		//picLayout.setLayoutParams(new RelativeLayout.LayoutParams(size, size));		
		switch(itemHeight){
		case 24:
			defaultImageResource = R.drawable.contact_24px;
			nameTextSize = 8;
			statusTextSize = 5;
			break;
		case 32:
			defaultImageResource = R.drawable.contact_32px;
			nameTextSize = 12;
			statusTextSize = 7;
			break;
		case 48:
			defaultImageResource = R.drawable.contact_48px;
			nameTextSize = 18;
			statusTextSize = 10;
			break;
		default:
			defaultImageResource = R.drawable.contact_64px;
			nameTextSize = 25;
			statusTextSize = 13;
			break;
		}
	}
	
	public EntryPoint getEntryPoint() {
		return (EntryPoint) getContext();
	}

	public void populate(Buddy buddy, String bgType) {
		populate(buddy, bgType, showIcons);
	}
}
