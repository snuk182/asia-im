package ua.snuk182.asia.view.more;

import java.util.Arrays;

import ua.snuk182.asia.R;
import ua.snuk182.asia.services.ServiceUtils;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Spannable;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class StatusTextView extends TextView {
	
	public StatusTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setBackgroundResource(R.drawable.history_record_indicator);
		setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				Dialog dialog = new Dialog(getContext());
				dialog.setTitle(R.string.label_xstatus);
				TextView text = new TextView(getContext());
				text.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				text.setGravity(Gravity.CENTER);
				text.setPadding(5, 5, 5, 5);
				text.setText(getText());
				dialog.setContentView(text);
				dialog.show();
				return true;
			}

		});
	}
	
	public void setTextAndFormat(String text){
		setText(text, TextView.BufferType.EDITABLE);
		
		Spannable spannable = getEditableText();
		if (spannable !=null){
			
			TypedArray smileyNames = getContext().getResources().obtainTypedArray(R.array.smiley_names);
			TypedArray smileyValues = ServiceUtils.getSmileyResIdsByHeight(getContext(), getTextSize());
			
			for (int i=0; i<smileyNames.length(); i++){
				String name = smileyNames.getString(i);
				int pos = text.indexOf(name);
				
				if (pos<0){
					continue;
				}
				
				int value = smileyValues.getResourceId(i, R.drawable.asia_tray_16);
				
				while(pos<text.length()){					
					if (pos>-1){
						try {
							spannable.setSpan(new ImageSpan(getContext(), value), pos, pos+name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						} catch (Exception e) {
							ServiceUtils.log(e);
						}
						pos = text.indexOf(name, pos+name.length());
					} else {
						break;
					}					
				}
				byte[] replace = new byte[name.length()];
				Arrays.fill(replace, (byte) '_');
				text = text.replace(name, new String(replace));
			}	
			
			smileyNames.recycle();
			smileyValues.recycle();
		}
	}
}
