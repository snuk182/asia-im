package ua.snuk182.asia.services.plus;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListAdapter;

public class ImageGridAdapter implements ListAdapter {
	
	private TypedArray objects;
	private EntryPoint context;
	public int size = 80;
	
	public ImageGridAdapter(EntryPoint context, int resource,
			int textViewResourceId, TypedArray objects) {
		this.objects = objects;
		this.context = context;
	}

	public ImageGridAdapter(EntryPoint context, TypedArray objects, int size) {
		this(context, 0, 0, objects);
		this.size = size;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		int objectId = (Integer) getItem(position);
		if (convertView == null){
			ImageView image = new ImageView(context);
			image.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));
	        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
	        int padding = (int) (2*context.metrics.density);
	        image.setPadding(padding, padding, padding, padding);
	        convertView = image;
		}
		
		ImageView im = (ImageView) convertView;
		im.setImageResource(objectId);
		im.setId(position);
		//objects.recycle();
		return im;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getCount() {
		return objects.length();
	}

	@Override
	public Object getItem(int position) {
		return objects.getResourceId(position, R.drawable.asia_tray_32);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return objects==null;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}
}
