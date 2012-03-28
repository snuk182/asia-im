package ua.snuk182.asia.view;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.core.dataentity.TabInfo;
import android.content.res.Resources.Theme;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TabsAdapter extends BaseAdapter {
	
	private EntryPoint context;
    public TabsAdapter(EntryPoint entryPoint){
    	this.context = entryPoint;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TabInfo item = (TabInfo) getItem(position);
        
        if (convertView == null) {
            TextView temp = new TextView(context);
            AbsListView.LayoutParams param = new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT, (int) (45+getEntryPoint().metrics.density));
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
        textView.setText(item.tabWidgetLayout.getText());
        textView.setCompoundDrawablesWithIntrinsicBounds( item.tabWidgetLayout.getDrawable(), null, null, null);
              
        return textView;
    }

	@Override
	public int getCount() {
		return getEntryPoint().mainScreen.getTabs().size();
	}

	@Override
	public Object getItem(int position) {
		return getEntryPoint().mainScreen.getTabs().get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	EntryPoint getEntryPoint(){
		return context;
	}
}
