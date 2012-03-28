package ua.snuk182.asia.services.plus;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.R;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class IconMenuAdapter extends BaseAdapter {
	private EntryPoint context;
    private String[] names = null;
    private int[] iconIds = null;
    
    public IconMenuAdapter(EntryPoint entryPoint, String[] names, TypedArray typedArray){
    	this.context = entryPoint;
    	this.names = names;

    	int count = typedArray.length();
    	this.iconIds = new int[count];
    	for(int i=0; i<count; i++){
    		iconIds[i] = typedArray.getResourceId(i, R.drawable.asia_logo_60px);
    	}
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Object[] item = (Object[]) getItem(position);
        
        if (convertView == null) {
            TextView temp = new TextView(context);
            AbsListView.LayoutParams param = new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT, (int) (64*getEntryPoint().metrics.density));
            temp.setLayoutParams(param);
            temp.setPadding(8, 8, 8, 8);
            temp.setGravity(android.view.Gravity.CENTER_VERTICAL);
               
            Theme th = context.getTheme();
            TypedValue tv = new TypedValue();
         
            if (th.resolveAttribute(android.R.attr.textAppearanceLargeInverse, tv, true)) {
                temp.setTextAppearance(context, tv.resourceId);
            }
            temp.setMinHeight(32);
            temp.setCompoundDrawablePadding(8);
            convertView = temp;
        }
        
        TextView textView = (TextView) convertView;
        textView.setTag(item);
        textView.setText((CharSequence) item[0]);
        textView.setCompoundDrawablesWithIntrinsicBounds( context.getResources().getDrawable((Integer) item[1]), null, null, null);
              
        return textView;
    }

	@Override
	public int getCount() {
		return names.length;
	}

	@Override
	public Object getItem(int position) {
		return new Object[]{names[position], iconIds[position]};
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	private EntryPoint getEntryPoint(){
		return context;
	}
}
