package ua.snuk182.asia.view.more;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.view.ViewUtils;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class TabWidgetLayout extends LinearLayout {
	
	private ImageView tabIcon;
	private TextView tabName;
	
	private CharSequence textCache;
	private Drawable iconCache;
	
	public TabSpec spec;

	public TabWidgetLayout(EntryPoint entryPoint) {
		super(entryPoint);
		
		if (EntryPoint.tabStyle.equals("system")){
			tabIcon = null;
			tabName = null;
			return;
		}
		
		LayoutInflater inflate = LayoutInflater.from(entryPoint);
		
		inflate.inflate(R.layout.tab_widget_layout, this);
		setOrientation(LinearLayout.HORIZONTAL);
		setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, (int) (48*entryPoint.metrics.density)));
		
		tabIcon = (ImageView) findViewById(android.R.id.icon);
		tabName = (TextView) findViewById(android.R.id.title);
	
		int pad1 = (int) (3*entryPoint.metrics.density);
		int pad2 = (int) (2*entryPoint.metrics.density);
		
		tabIcon.setPadding(pad1, 0, pad2, 0);
		tabName.setPadding(pad2, 0, pad1, 0);
		
		color();
		
		setPadding(2, 0, 2, 0);
		setGravity(Gravity.CENTER);
	}
	
	public void setFromView(final View parent, final TabHost host) {
		tabIcon = (ImageView) parent.findViewById(android.R.id.icon);
		tabName = (TextView) parent.findViewById(android.R.id.title);
		
		LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, parent.getLayoutParams().height);
		layout.setMargins((int) (-3 * getEntryPoint().metrics.density), 0, (int) (-3 * getEntryPoint().metrics.density), 0);
		parent.setLayoutParams(layout);
		parent.setPadding(0, parent.getPaddingTop(), 0, parent.getPaddingBottom());
		
		int iconPadding = (int) (15*getEntryPoint().metrics.density);
		tabName.setPadding(5, tabName.getPaddingTop(), 5, tabName.getPaddingBottom());
		tabIcon.setPadding(iconPadding, tabIcon.getPaddingTop(), iconPadding, tabIcon.getPaddingBottom());
		
		parent.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				host.scrollTo(parent.getRight(), parent.getTop());				
			}
			
		});
	}

	public void setText(CharSequence text) {
		textCache = text;
		if (tabName != null){
			tabName.setText(textCache);
		} else if (spec != null){
			spec.setIndicator(textCache, iconCache);
		}
	}
	
	public void setText(int resid) {
		textCache = getContext().getString(resid);
		if (tabName != null){
			tabName.setText(textCache);
		} else if (spec != null){
			spec.setIndicator(textCache, iconCache);
		}
	}
	
	public void setImageDrawable(Drawable dr) {
		iconCache = dr;
		
		if (tabIcon != null){
			tabIcon.setImageDrawable(iconCache);
		} else if (spec != null){
			spec.setIndicator(textCache, iconCache);
		}
	}
	
	public void setImageBitmap(Bitmap bitmap) {
		iconCache = new BitmapDrawable(bitmap);
		
		if (tabIcon != null){
			tabIcon.setImageDrawable(iconCache);
		} else if (spec != null){
			spec.setIndicator(textCache, iconCache);
		}
	}
	
	public void setImageResource(int resId) {
		iconCache = getContext().getResources().getDrawable(resId);
		
		if (tabIcon != null){
			tabIcon.setImageDrawable(iconCache);
		} else if (spec != null){
			spec.setIndicator(textCache, iconCache);
		}
	}
	
	public void setScaledBitmap(Bitmap bmp){
		if (bmp == null){
			tabIcon.setImageResource(R.drawable.dummy_32);
			return;
		}
		
		setImageBitmap(ViewUtils.scaleBitmap(bmp, (int) (32 * getEntryPoint().metrics.density), true));
	}

	private EntryPoint getEntryPoint() {
		return (EntryPoint) getContext();
	}

	public void color() {
		
		if (spec != null){
			return;
		}
		
		ViewUtils.styleTextView(tabName);
		int pad1;
		int pad2;
		
		tabName.setBackgroundColor(0x00000000);
		
		if (EntryPoint.bgColor < 0xff7f7f80){
			setBackgroundResource(R.drawable.tab_indicator_dark);
			pad1 = (int) (getEntryPoint().metrics.density);
			pad2 = (int) (2*getEntryPoint().metrics.density);		
			
		} else if (EntryPoint.bgColor == 0xff7f7f80){
			setBackgroundResource(R.drawable.tab_indicator);
			pad1 = (int) (9*getEntryPoint().metrics.density);
			pad2 = (int) (3*getEntryPoint().metrics.density);		
			
		} else {
			setBackgroundResource(R.drawable.tab_indicator_white);
			pad1 = (int) (getEntryPoint().metrics.density);
			pad2 = (int) (2*getEntryPoint().metrics.density);		
			
		}	
		
		tabIcon.setPadding(pad1, 0, pad2, 0);
		tabName.setPadding(pad2, 0, pad1, 0);
	}

	public CharSequence getText() {
		return textCache;
	}

	public Drawable getDrawable() {
		return iconCache;
	}
	
	public int getLeftBound(){
		if (spec != null){
			return ((View) tabName.getParent()).getLeft();
		} else {
			return super.getLeft();
		}
	}
	
	public int getRightBound(){
		if (spec != null){
			return ((View) tabName.getParent()).getRight();
		} else {
			return super.getRight();
		}
	}
}
