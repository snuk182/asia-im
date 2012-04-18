package ua.snuk182.asia.services.plus;

import java.util.List;

import ua.snuk182.asia.core.dataentity.BuddyGroup;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BuddyGroupAdapter extends ArrayAdapter<BuddyGroup> {

	public BuddyGroupAdapter(Context context, List<BuddyGroup> objects){
		this(context, 0, 0, objects);
	}

	public BuddyGroupAdapter(Context context, int resource,
			int textViewResourceId, List<BuddyGroup> objects) {
		super(context, resource, textViewResourceId, objects);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		final BuddyGroup bg = getItem(position);
		final TextView text = new TextView(getContext());
		text.setText(bg.name);
		text.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
		
        return text;
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent){
		return getView(position, convertView, parent);
	}
}
