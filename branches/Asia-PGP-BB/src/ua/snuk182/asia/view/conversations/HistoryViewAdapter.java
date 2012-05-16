package ua.snuk182.asia.view.conversations;

import java.util.List;

import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.Message;
import ua.snuk182.asia.core.dataentity.ServiceMessage;
import ua.snuk182.asia.core.dataentity.TextMessage;
import ua.snuk182.asia.view.ViewUtils;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class HistoryViewAdapter extends ArrayAdapter<Message> {
	
	private int textColor = 0;
	private int bgColor = 0xffffff;
	private float textSize;
	private boolean dontDrawSmileys = false;

	public HistoryViewAdapter(Context context, int resource, int textViewResourceId, List<Message> objects, float textSize, boolean dontDrawSmileys) {
		super(context, resource, textViewResourceId, objects);
		this.textSize = textSize;
		this.dontDrawSmileys = dontDrawSmileys;
	}

	public HistoryViewAdapter(Context context, List<Message> objects, float textSize, boolean dontDrawSmileys) {
		this(context, 0, 0, objects, textSize, dontDrawSmileys);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		Message message = getItem(position);
		
		if (message instanceof TextMessage){
			HistoryRecordView v;
			if (convertView != null && convertView instanceof HistoryRecordView && message.messageId == ((HistoryRecordView) convertView).recordId){
				v = (HistoryRecordView) convertView;
			} else {
				v = new HistoryRecordView(getContext(), null, textSize);
				v.recordId = message.messageId;
				final String msg = message.text.trim();
				v.setOnLongClickListener(new OnLongClickListener(){

					@Override
					public boolean onLongClick(View arg0) {
						return historyRecordMenu(msg);
					}
					
				});
				
				v.setTextAndFormat(msg, null, dontDrawSmileys, ((TextMessage)message).options == TextMessage.OPT_SECURE);
			}
			
			v.setBackgroundColor(bgColor);
			v.setTextColor(0xff000000+textColor);
			return v;
		}
		if (message instanceof ServiceMessage){ //TODO 
			TextView tv;
			if (convertView != null && convertView instanceof TextView){
				tv = (TextView) convertView;				
			} else {
				tv = new TextView(getContext());
				tv.setTextColor(0xff000000+textColor);
				tv.setGravity(Gravity.CENTER);
				tv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1));
			}
			return tv;
		}
		
		return null;
	}
	
	protected boolean historyRecordMenu(final String msg) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		final TypedArray values = getContext().getResources().obtainTypedArray(R.array.conversation_menu_values);
		builder.setItems(R.array.history_menu_values, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String value = values.getString(which);
				if (value.equals(getContext().getResources().getString(R.string.menu_value_copy))) {
					ViewUtils.replaceClipboard(getContext(), msg, false);
				}
				if (value.equals(getContext().getResources().getString(R.string.menu_value_add_to_copied))) {
					ViewUtils.replaceClipboard(getContext(), msg, true);
				}
				
			}
		});
		builder.create().show();
		
		return false;
	}

	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	public float getTextSize() {
		return textSize;
	}

	public void setTextSize(float textSize) {
		this.textSize = textSize;
	}

	public int getBgColor() {
		return bgColor;
	}

	public void setBgColor(int bgColor) {
		this.bgColor = bgColor;
	}

	public boolean isDontDrawSmileys() {
		return dontDrawSmileys;
	}

	public void setDontDrawSmileys(boolean dontDrawSmileys) {
		this.dontDrawSmileys = dontDrawSmileys;
	}
}
