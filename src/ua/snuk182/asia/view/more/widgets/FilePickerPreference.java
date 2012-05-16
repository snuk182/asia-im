package ua.snuk182.asia.view.more.widgets;

import java.io.File;
import java.io.FileFilter;

import ua.snuk182.asia.R;
import ua.snuk182.asia.view.more.fileexplorer.FileExplorer;
import ua.snuk182.asia.view.more.fileexplorer.FileExplorer.FileExplorerAction;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

public class FilePickerPreference extends DialogPreference{
	
	public String mValue;
	public final FileFilter mFilter;
	public final FileExplorerAction action;

	public FilePickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		String filter = attrs.getAttributeValue("http://ua.snuk182.asia/res", "filter");
		
		if (filter != null){
			final String[] filters = filter.split(";");
			
			mFilter = new FileFilter() {
				
				@Override
				public boolean accept(File pathname) {
					if (pathname.isDirectory()){
						return true;
					}
					
					boolean ok = false;
					
					for (String f: filters){
						if (pathname.getAbsolutePath().endsWith(f)){
							ok = true;
							break;
						}
					}
					
					return ok;
				}
			};
		} else {
			mFilter = null;
		}	
		
		action = new FileExplorerAction() {
			
			@Override
			public void action(File file) {
				mValue = file.getAbsolutePath();
				onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
				getDialog().dismiss();
			}
		};
		
		setPositiveButtonText(null);
		setNegativeButtonText(R.string.label_cancel);
	}
	
	@Override
	protected View onCreateDialogView() {
		FileExplorer explorer = new FileExplorer(getContext(), action, mFilter);
		explorer.setDialog(getDialog());
		return explorer;
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult){
			if (shouldPersist())
				persistString(mValue);			
		}
		callChangeListener(mValue);
    }
}
