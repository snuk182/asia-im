package ua.snuk182.asia.view.more;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BuddyImage extends ImageView{

	public BuddyImage(Context context) {
		super(context);		
	}
	
	public BuddyImage(Context context, AttributeSet set) {
		super(context, set);		
	}

	public void setTopImage(Drawable drawable){
		setImageDrawable(drawable);
	}
	
	public void setTopImage(int id){
		setImageResource(id);
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
}
