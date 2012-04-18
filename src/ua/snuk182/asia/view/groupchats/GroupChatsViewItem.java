package ua.snuk182.asia.view.groupchats;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import ua.snuk182.asia.view.ViewUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class GroupChatsViewItem extends RelativeLayout implements OnFocusChangeListener {
	
	private final TextView nameTV;
	private final TextView uidTV;

	public GroupChatsViewItem(EntryPoint entryPoint, String name, String uid, String searchString, int itemHeight) {
		super(entryPoint);
		setClickable(true);
		
		LayoutInflater inflate = LayoutInflater.from(entryPoint);
		inflate.inflate(R.layout.group_chats_list_item, this);
		setLayoutParams(new ScrollView.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		setBackgroundResource(R.drawable.history_record_indicator);
		
		nameTV = (TextView) findViewById(R.id.namelabel);
		uidTV = (TextView) findViewById(R.id.uidlabel);
		
		nameTV.setFocusable(false);
		uidTV.setFocusable(false);
		
		setOnFocusChangeListener(this);
		
		nameTV.setText(name);
		uidTV.setText(uid);
		
		resize(itemHeight);
		
		checkSearchStringMatch(searchString);
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

	public void checkSearchStringMatch(String searchString) {
		if (searchString!=null && searchString.length()>0 && nameTV.getText().toString().indexOf(searchString)<0 && uidTV.getText().toString().indexOf(searchString)<0){
			setVisibility(GONE);
		} else {
			setVisibility(VISIBLE);
		}
	}
	
	public void color(){
		ViewUtils.styleTextView(nameTV);
	}
	
	public void resize(int itemHeight){
		float nameTextSize;
		float statusTextSize;
		
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
		
		nameTV.setTextSize(nameTextSize);
		uidTV.setTextSize(statusTextSize);
	}
}
