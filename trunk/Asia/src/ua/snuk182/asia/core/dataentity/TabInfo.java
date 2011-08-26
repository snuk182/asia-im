package ua.snuk182.asia.core.dataentity;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.view.ITabContent;
import ua.snuk182.asia.view.more.TabWidgetLayout;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;

public class TabInfo implements TabContentFactory, Parcelable{
	
	public TabSpec tabSpec;
	public String tag;
	public ITabContent content;
	public TabWidgetLayout tabWidgetLayout;
	
	public TabInfo(String tag, final ITabContent content, final TabHost tabHost){
		this.tag = tag;
		this.content = content;
		
		construct(tabHost);
		content.getTabWidgetLayout().setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				tabHost.scrollTo(content.getTabWidgetLayout().getRight(), content.getTabWidgetLayout().getTop());				
			}
			
		});
	}
	
	public TabInfo(String tag, String title, Intent intent, EntryPoint entryPoint, int iconId){
		this.tag = tag;
		tabSpec = entryPoint.mainScreen.getChatsTabHost().newTabSpec(tag).setContent(intent);		
		
		TabWidgetLayout indicator = new TabWidgetLayout(entryPoint);
		indicator.getTabName().setText(title);
		indicator.getTabIcon().setImageResource(iconId);
		tabWidgetLayout = indicator;
		tabSpec.setIndicator(tabWidgetLayout);
	}

	private TabInfo(Parcel in) {
		tag = in.readString();		
	}
	
	public void construct(TabHost host){
		if (tag==null || content == null){
			return;
		}
		
		tabWidgetLayout = content.getTabWidgetLayout();
		tabSpec = host.newTabSpec(tag).setContent(this).setIndicator(tabWidgetLayout);		
	}

	@Override
	public View createTabContent(String tag) {
		
		return (View) content;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int arg1) {
		out.writeString(tag);
	}
	
	public static final Parcelable.Creator<TabInfo> CREATOR = new Parcelable.Creator<TabInfo>(){

		@Override
		public TabInfo createFromParcel(Parcel source) {
			return new TabInfo(source);
		}

		@Override
		public TabInfo[] newArray(int size) {
			return new TabInfo[size];
		}
		
	};
}
