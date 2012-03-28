package ua.snuk182.asia.view.more;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FileTransferViewAdapter extends BaseAdapter {
	
	final List<FileTransferItem> items;
	private int textColor = 0xffffffff;
	
	public FileTransferViewAdapter(List<FileTransferItem> items){
		this.items = items;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if (convertView == null){
			convertView = (View) getItem(position);
		}
		
		FileTransferItem item = (FileTransferItem) convertView;
		item.title.setTextColor(textColor);
		
		return convertView;
	}
	
	public void setTextColor(int textColor) {
		this.textColor  = textColor;
	}
}
