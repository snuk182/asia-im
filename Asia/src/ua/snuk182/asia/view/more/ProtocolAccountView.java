package ua.snuk182.asia.view.more;

import ua.snuk182.asia.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProtocolAccountView extends LinearLayout {
	
	public ImageView accountTypeImg;
	public TextView accountName;
	public ImageButton editBtn;
	public ImageButton removeBtn;
	public CheckBox accEnabledCb;

	public ProtocolAccountView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflate = LayoutInflater.from(getContext());
		inflate.inflate(R.layout.account_item, this); 
		
		setGravity(Gravity.CENTER_VERTICAL);
		setOrientation(LinearLayout.HORIZONTAL);
		
		accountTypeImg = (ImageView) findViewById(R.id.accountItemImage);
		accountName = (TextView) findViewById(R.id.accountItemName);
		editBtn = (ImageButton) findViewById(R.id.accountItemEditBtn);
		removeBtn = (ImageButton) findViewById(R.id.accountItemRemoveBtn);
		accEnabledCb = (CheckBox) findViewById(R.id.accountItemEnableCb);
	}
}
