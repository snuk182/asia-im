package ua.snuk182.asia.view.conversations;

import java.util.Arrays;

import ua.snuk182.asia.R;
import ua.snuk182.asia.services.ServiceUtils;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.widget.AbsListView.LayoutParams;
import android.widget.TextView;

public class HistoryRecordView extends TextView {
	
	public long recordId = 0;
	
	public HistoryRecordView(Context context, AttributeSet attrs, float textSize) {
		super(context, attrs);
		LayoutParams layout = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1);
		setLayoutParams(layout);
		setPadding(2, 0, 2, 0);
		setTextSize(textSize);
		MovementMethod mm = getMovementMethod();
        if (!(mm instanceof LinkMovementMethod))
        {
             setMovementMethod(LinkMovementMethod.getInstance());
        }        
    }
	
	public void setTextAndFormat(String text, String printDateMode){
		int startIndex = text.indexOf("(");
		int endIndex = text.indexOf("):", startIndex)+2;
		
		if (startIndex < 0){
			startIndex = 0;
		}
		
		if (printDateMode != null){
			if (printDateMode.equals(getContext().getString(R.string.value_chat_date_time))){
				text = text.substring(0, startIndex+1)+text.substring(text.indexOf(" ", startIndex)+1);
				endIndex = text.indexOf("):", startIndex)+2;
			} else if (printDateMode.equals(getContext().getString(R.string.value_chat_date_none))){
				text = text.substring(0, startIndex)+text.substring(text.indexOf("):", startIndex)+1);				
				endIndex = text.indexOf(":", startIndex)+1;
				startIndex = 0;
			}
		}
		
		setText(text, TextView.BufferType.EDITABLE);
		
		Spannable spannable = getEditableText();
		if (spannable !=null){	
			
			if (text.indexOf(getContext().getString(R.string.label_me))==0){
				spannable.setSpan(new ForegroundColorSpan(0xff00a5ff), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}else {
				spannable.setSpan(new ForegroundColorSpan(0xffff0000), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			
			if (text.indexOf("http://")>-1){
				int pos = endIndex;
				
				while(pos>-1 && pos<text.length()){		
					pos = text.indexOf("http://", pos);
					
					if (pos>-1){
						int spaceEndPos = text.indexOf(" ", pos);
						int endPos = spaceEndPos>-1? spaceEndPos : text.length();
						
						int nlEndPos = text.indexOf("\n", pos);
						
						if (nlEndPos > pos && nlEndPos < endPos){
							endPos = nlEndPos;
						}
						
						String url = text.substring(pos, endPos);
						URLSpan urlSpan = new URLSpan(url);
						spannable.setSpan(urlSpan, pos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						byte[] replace = new byte[endPos-pos];
						Arrays.fill(replace, (byte) '_');
						text = text.replace(url, new String(replace));
						pos = ++endPos;
					} else {
						break;
					}					
				}				
			}
			
			if (text.indexOf("ftp://")>-1){
				int pos = endIndex;
				
				while(pos>-1 && pos<text.length()){		
					pos = text.indexOf("ftp://", pos);				
					if (pos>-1){
						int spaceEndPos = text.indexOf(" ", pos);
						int endPos = spaceEndPos>-1? spaceEndPos : text.length();
						
						int nlEndPos = text.indexOf("\n", pos);
						
						if (nlEndPos > -1 && nlEndPos < endPos){
							endPos = nlEndPos;
						}
						String url = text.substring(pos, endPos);
						URLSpan urlSpan = new URLSpan(url);
						spannable.setSpan(urlSpan, pos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						byte[] replace = new byte[endPos-pos];
						Arrays.fill(replace, (byte) '_');
						text = text.replace(url, new String(replace));
						pos = ++endPos;
						
					} else {
						break;
					}					
				}				
			}
			
			if (text.indexOf("https://")>-1){
				int pos = endIndex;
				
				while(pos>-1 && pos<text.length()){		
					pos = text.indexOf("https://", pos);			
					if (pos>-1){
						int spaceEndPos = text.indexOf(" ", pos);
						int endPos = spaceEndPos>-1? spaceEndPos : text.length();
						
						int nlEndPos = text.indexOf("\n", pos);
						
						if (nlEndPos > -1 && nlEndPos < endPos){
							endPos = nlEndPos;
						}
						String url = text.substring(pos, endPos);
						URLSpan urlSpan = new URLSpan(url);
						spannable.setSpan(urlSpan, pos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						byte[] replace = new byte[endPos-pos];
						Arrays.fill(replace, (byte) '_');
						text = text.replace(url, new String(replace));
						pos = ++endPos;
						
					} else {
						break;
					}					
				}				
			}
			
			TypedArray smileyNames = getContext().getResources().obtainTypedArray(R.array.smiley_names);
			TypedArray smileyValues = ServiceUtils.getSmileyResIdsByHeight(getContext(), getTextSize());			
			
			for (int i=0; i<smileyNames.length(); i++){
				String name = smileyNames.getString(i);
				int pos = text.indexOf(name, text.indexOf("):")+2);
				
				if (pos<0){
					continue;
				}
				
				int value = smileyValues.getResourceId(i, R.drawable.logo_16px);
				
				while(pos<text.length()){					
					if (pos>-1){
						try {
							spannable.setSpan(new ImageSpan(getContext(), value, ImageSpan.ALIGN_BASELINE), pos, pos+name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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

	public void messageAck(int level) {
		setCompoundDrawablePadding(0);
		switch(level){
		case 1:
			setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.btn_check_buttonless_off, 0);
			break;
		case 2:
			setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.btn_check_buttonless_on, 0);
			break;
		}		
	}
	
	/*private EntryPoint getEntryPoint(){
		return (EntryPoint) getContext();
	}*/
}
