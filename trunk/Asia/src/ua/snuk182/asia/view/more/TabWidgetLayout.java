package ua.snuk182.asia.view.more;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.view.ViewUtils;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TabWidgetLayout extends LinearLayout {
	
	private final ImageView tabIcon;
	private final TextView tabName;

	public TabWidgetLayout(EntryPoint entryPoint) {
		super(entryPoint);
		
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

	public ImageView getTabIcon() {
		return tabIcon;
	}

	public TextView getTabName() {
		return tabName;
	}
	
	public void setScaledBitmap(Bitmap bmp){
		if (bmp == null){
			tabIcon.setImageResource(R.drawable.dummy_32);
			return;
		}
		
		tabIcon.setImageBitmap(ViewUtils.scaleBitmap(bmp, (int) (32 * getEntryPoint().metrics.density), true));
	}

	private EntryPoint getEntryPoint() {
		return (EntryPoint) getContext();
	}

	public void color() {
		
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
}
