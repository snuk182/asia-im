package ua.snuk182.asia.view.more.fileexplorer;

import java.io.File;
import java.util.List;

import ua.snuk182.asia.R;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FileExplorerAdapter extends BaseAdapter {
	private List<File> files;
	private Context context;
	
	public FileExplorerAdapter(Context context, List<File> files){
		this.files = files;
		this.context = context;
	}

	@Override
	public int getCount() {
		return files!=null ? files.size() : -1;
	}

	@Override
	public Object getItem(int position) {
		return files!=null ? files.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		File item = (File) getItem(position);
        
        if (convertView == null) {
            TextView temp = new TextView(context);
            AbsListView.LayoutParams param = new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
            temp.setLayoutParams(param);
            temp.setPadding(8, 8, 8, 8);
            temp.setGravity(android.view.Gravity.CENTER_VERTICAL);
               
            /*Theme th = context.getTheme();
            TypedValue tv = new TypedValue();
         
            if (th.resolveAttribute(android.R.attr.textAppearanceLargeInverse, tv, true)) {
                temp.setTextAppearance(context, tv.resourceId);
            	//temp.setTextColor(0xffff);
            }*/
            temp.setMinHeight(32);
            temp.setCompoundDrawablePadding(8);
            convertView = temp;
        }
        
        TextView textView = (TextView) convertView;
        textView.setTag(item);
        textView.setText(item.getName());
        textView.setCompoundDrawablesWithIntrinsicBounds(getDrawableByFileType(item), null, null, null);
              
        return textView;
	}
	
	private Drawable getDrawableByFileType(File file){
		if (file.isDirectory()){
			return context.getResources().getDrawable(R.drawable.file_folder);
		} else if (file.getName().endsWith(".mp3") 
				|| file.getName().endsWith(".flac")
				|| file.getName().endsWith(".ogg")
				|| file.getName().endsWith(".ape")
				|| file.getName().endsWith(".m4a")){
			return context.getResources().getDrawable(R.drawable.file_audio);
		} else if (file.getName().endsWith(".avi")
				|| file.getName().endsWith(".mp4")
				|| file.getName().endsWith(".3gp")
				|| file.getName().endsWith(".flv")){
			return context.getResources().getDrawable(R.drawable.file_video);
		} else if (file.getName().endsWith(".txt")
				|| file.getName().endsWith(".fb2")
				|| file.getName().endsWith(".doc")
				|| file.getName().endsWith(".docx")){
			return context.getResources().getDrawable(R.drawable.file_text);
		} else if (file.getName().endsWith(".jpg")
				|| file.getName().endsWith(".jpeg")
				|| file.getName().endsWith(".gif")
				|| file.getName().endsWith(".png")
				|| file.getName().endsWith(".bmp")){
			return context.getResources().getDrawable(R.drawable.file_pict);
		} else if (file.getName().equals("")){
			return context.getResources().getDrawable(R.drawable.file_back);
		} else {
			return context.getResources().getDrawable(R.drawable.file_plain);
		}
	}
}
