package ua.snuk182.asia.view.more;

import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.PersonalInfo;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SearchUsersResultItem extends RelativeLayout {
	
	public ImageView statusImage;
	public TextView userNameLabel;
	public Button addButton;
	public TextView infoLabel;
	public ImageView genderImage;

	public SearchUsersResultItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflate = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflate.inflate(R.layout.buddy_search_item, this); 
		
		statusImage = (ImageView) findViewById(R.id.statusimage);
		userNameLabel = (TextView) findViewById(R.id.usernamelabel);
		addButton = (Button) findViewById(R.id.addbutton);
		infoLabel = (TextView) findViewById(R.id.infolabel);
		genderImage = (ImageView) findViewById(R.id.genderimage);
	}
	
	public void populate(PersonalInfo info){
		userNameLabel.setText(info.properties.getString(PersonalInfo.INFO_NICK));
		
		byte authRequired = info.properties.getByte(PersonalInfo.INFO_REQUIRES_AUTH);
		byte gender = info.properties.getByte(PersonalInfo.INFO_GENDER);
		short age = info.properties.getShort(PersonalInfo.INFO_AGE);
		short status = info.properties.getShort(PersonalInfo.INFO_STATUS);
		String firstName = info.properties.getString(PersonalInfo.INFO_FIRST_NAME);
		
		switch (status){
		case 0:
			statusImage.setImageResource(R.drawable.icq_offline_tiny);
			break;
		case 1:
			statusImage.setImageResource(R.drawable.icq_online_tiny);
			break;
		case 2:
			statusImage.setImageResource(R.drawable.icq_invisible_tiny);
			break;
		}
		
		switch(gender){
		case 0:
			genderImage.setImageResource(R.drawable.female);
			break;
		case 1:
			statusImage.setImageResource(R.drawable.male);
			break;
		}
		
		infoLabel.setText((firstName!=null?firstName+", ":"")+(age>-1?age+", ":"")+(authRequired>-1?(authRequired==0?"No auth required":"Auth required"):""));
	}
}
