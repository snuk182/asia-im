package ua.snuk182.asia.view.more;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.view.ViewUtils;
import android.content.Context;
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
		
		LayoutInflater inflate = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		inflate.inflate(R.layout.tab_widget_layout, this);
		setOrientation(LinearLayout.HORIZONTAL);
		setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, (int) (48*entryPoint.metrics.density)));
		
		tabIcon = (ImageView) findViewById(android.R.id.icon);
		tabName = (TextView) findViewById(android.R.id.title);
	
		int pad1 = (int) (9*entryPoint.metrics.density);
		int pad2 = (int) (3*entryPoint.metrics.density);
		
		tabIcon.setPadding(pad1, 0, pad2, 0);
		tabName.setPadding(pad2, 0, pad1, 0);
		
		color(entryPoint.bgColor);
		
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
			tabIcon.setImageResource(R.drawable.contact_32px);
			return;
		}
		
		tabIcon.setImageBitmap(ViewUtils.scaleBitmap(bmp, (int) (32 * getEntryPoint().metrics.density), true));
	}

	private EntryPoint getEntryPoint() {
		return (EntryPoint) getContext();
	}

	public void color(int bgColor) {
		
		setBackgroundResource(R.drawable.tab_indicator);
		tabName.setShadowLayer(1.8f, 2, 2, 0xcd000000);
		tabName.setTextColor(0xffffffff);
		
		/*if (bgColor < 0xff7f7f80){
			setBackgroundResource(R.drawable.tab_indicator_white);
			tabName.setShadowLayer(0, 0, 0, 0);
			tabName.setTextColor(0xff000000);
		} else if (bgColor == 0xff7f7f80){
			setBackgroundResource(R.drawable.tab_indicator);
			tabName.setShadowLayer(1.8f, 2, 2, 0xcd000000);
			tabName.setTextColor(0xffffffff);
		} else {
			setBackgroundResource(R.drawable.tab_indicator_dark);
			tabName.setShadowLayer(0, 0, 0, 0);
			tabName.setTextColor(0xffffffff);
		}	*/	
	}
}
