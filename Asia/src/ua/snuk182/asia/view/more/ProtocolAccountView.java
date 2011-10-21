package ua.snuk182.asia.view.more;

import ua.snuk182.asia.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProtocolAccountView extends LinearLayout {
	
	public ImageView accountTypeImg;
	public TextView accountName;
	public Button editBtn;
	public Button removeBtn;

	public ProtocolAccountView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflate = LayoutInflater.from(getContext());
		inflate.inflate(R.layout.account_item, this); 
		
		accountTypeImg = (ImageView) findViewById(R.id.accountItemImage);
		accountName = (TextView) findViewById(R.id.accountItemName);
		editBtn = (Button) findViewById(R.id.accountItemEditBtn);
		removeBtn = (Button) findViewById(R.id.accountItemRemoveBtn);
	}
}
