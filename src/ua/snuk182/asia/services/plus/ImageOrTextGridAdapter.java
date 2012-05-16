package ua.snuk182.asia.services.plus;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class ImageOrTextGridAdapter implements ListAdapter {
	
	private TypedArray objects;
	private EntryPoint context;
	public int size = 80;
	
	private Class<?> cls;
	
	public ImageOrTextGridAdapter(EntryPoint context, int resource,
			int textViewResourceId, TypedArray objects) {
		this.objects = objects;
		this.context = context;
	}

	public ImageOrTextGridAdapter(EntryPoint context, TypedArray objects, int size, Class<?> cls) {
		this(context, 0, 0, objects);
		this.size = size;
		this.cls = cls;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		if (convertView == null){
			
			View target;
			try {
				target = (View) cls.getConstructor(Context.class).newInstance(context);
			} catch (Exception e) {
				target = new ImageView(context);
			}
			
			target.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));
			
			if (target instanceof ImageView){
				((ImageView)target).setScaleType(ImageView.ScaleType.FIT_CENTER);
			}
			
	        int padding = (int) (2*context.metrics.density);
	        target.setPadding(padding, padding, padding, padding);
	        convertView = target;
		}
		
		View im = (View) convertView;
		
		if (im instanceof ImageView){
			int objectId = (Integer) getItem(position);
			((ImageView)im).setImageResource(objectId);
		}
		
		if (im instanceof TextView){
			String text = (String) getItem(position);
			((TextView)im).setText(text);
		}
		
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
		if (cls == TextView.class){
			return objects.getString(position);
		}
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
