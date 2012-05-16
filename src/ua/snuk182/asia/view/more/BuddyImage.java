package ua.snuk182.asia.view.more;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.ImageView;

public class BuddyImage extends ImageView implements OnFocusChangeListener{

	public BuddyImage(Context context) {
		super(context);		
		onFocusChange(null, false);
	}
	
	public BuddyImage(Context context, AttributeSet set) {
		super(context, set);		
		onFocusChange(null, false);
	}

	public void setTopImage(Drawable drawable){
		setImageDrawable(drawable);
	}
	
	public void setTopImage(int id){
		setImageResource(id);
		//setScaleType(ScaleType.FIT_XY);
	}
	
	public void setTopImage(Bitmap bitmap){
		setImageBitmap(bitmap);
	}
	
	public void setBuddyImage(Drawable drawable){
		setBackgroundDrawable(drawable);
	}
	
	public void setBuddyImage(int id){
		setBackgroundResource(id);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (EntryPoint.bgColor == 0xff7f7f80){
			if (hasFocus){
				setTopImage(R.drawable.contact_sel_64px);			
			} else {
				setTopImage(R.drawable.contact_64px);
			}
		} else {
			if (hasFocus){
				setTopImage(R.drawable.dummy_selected);			
			} else {
				setTopImage(R.drawable.dummy);
			}
		}
	}
}
