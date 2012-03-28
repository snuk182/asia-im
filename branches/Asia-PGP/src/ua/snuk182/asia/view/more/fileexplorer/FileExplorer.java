package ua.snuk182.asia.view.more.fileexplorer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ua.snuk182.asia.EntryPoint;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.services.ServiceUtils;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class FileExplorer extends ListView{
	
	public File currentDirectory = new File("/");
	
	private final List<File> files = new ArrayList<File>();
	private final FileExplorerAdapter adapter;
	private final File levelUp = new File("..");

	private Dialog dialog = null;

	public FileExplorer(EntryPoint entryPoint, final Buddy buddy) {
		super(entryPoint);	
		adapter = new FileExplorerAdapter(entryPoint, files);
		setAdapter(adapter);
		
		String storageState = Environment.getExternalStorageState();
		if (storageState.equals(Environment.MEDIA_MOUNTED)){
			currentDirectory = Environment.getExternalStorageDirectory();			
		} else {
			Toast.makeText(entryPoint, "Nothing to send!", Toast.LENGTH_SHORT).show();
		}
		setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position == 0){
					drawFileList(currentDirectory.getParentFile());
					return;
				} 
				File clicked = files.get(position);
				if(clicked.isDirectory()){
					currentDirectory = clicked;
					drawFileList(currentDirectory);
					return;
				} else {
					Bundle bu = new Bundle();
					bu.putSerializable(File.class.getName(), clicked);
					try {
						getEntryPoint().runtimeService.sendFile(bu, buddy);
					} catch (NullPointerException npe) {	
						ServiceUtils.log(npe);
					} catch (RemoteException e) {
						getEntryPoint().onRemoteCallFailed(e);
					}
					dialog.dismiss();
				}
			}
			
		});
		
		post(new Runnable(){

			@Override
			public void run() {
				drawFileList(currentDirectory);				
			}});
	}
	
	private void drawFileList(File dir){
		this.files.clear();
		if (dir.getParent() != null){
			this.files.add(levelUp);
		}
		this.files.addAll(Arrays.asList(dir.listFiles()));
		if (dialog!=null){
			try {
				dialog.setTitle(dir.getCanonicalPath());
			} catch (IOException e) {
				ServiceUtils.log(e);
			}
		}
		adapter.notifyDataSetChanged();
	}
	
	private EntryPoint getEntryPoint(){
		return (EntryPoint) getContext();
	}

	public void setDialog(Dialog dialog) {
		this.dialog  = dialog;
	}
}
