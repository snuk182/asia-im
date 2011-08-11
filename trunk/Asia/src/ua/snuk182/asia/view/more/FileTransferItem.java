package ua.snuk182.asia.view.more;

import java.io.File;

import ua.snuk182.asia.R;
import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.services.ServiceUtils;
import ua.snuk182.asia.view.cl.grid.ContactListGridItem;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FileTransferItem extends RelativeLayout {
	
	long messageId;
	final TextView title;
	final ProgressBar progressBar;
	final FrameLayout buddyIcon;
	
	final String filePath;
	
	final FileTransferView ftView;
	
	public FileTransferItem(final FileTransferView ftView, final long messageId, String filename, final Buddy buddy, boolean incoming, int progress, int total, String error) {
		super(ftView.getEntryPoint());
		this.ftView = ftView;
		this.messageId = messageId;
		this.filePath = filename;
		
		LayoutInflater inflate = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflate.inflate(R.layout.filetransfer_view_item, this);
		
		setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, (int) (83*ftView.getEntryPoint().metrics.density)));
		
		int pad = (int) (3*ftView.getEntryPoint().metrics.density);
		setPadding(pad, pad, pad, pad);
		setLongClickable(true);
		
		final FrameLayout buddyIconLeft = (FrameLayout) findViewById(R.id.buddy_icon_left);
		final FrameLayout buddyIconRight = (FrameLayout) findViewById(R.id.buddy_icon_right);
		title = (TextView) findViewById(R.id.title);
		
		final String file = filePath.substring(filePath.lastIndexOf(File.separator)+File.separator.length(), filePath.length());
		title.setText(file);
		
		progressBar = (ProgressBar) findViewById(R.id.progressbar);
		
		if (incoming){
			buddyIcon = buddyIconLeft;
			buddyIconRight.setVisibility(View.GONE);
			((ImageView) findViewById(R.id.direction_img_right)).setVisibility(View.GONE);
		} else {
			buddyIcon = buddyIconRight; 
			buddyIconLeft.setVisibility(View.GONE);
			((ImageView) findViewById(R.id.direction_img_left)).setVisibility(View.GONE);
		}
		
		boolean showIcons = true;
		try {
			String icons = ftView.getEntryPoint().runtimeService.getAccountView(buddy.serviceId).options.getString(getContext().getResources().getString(R.string.key_show_icons));
			showIcons = icons!=null?Boolean.parseBoolean(icons):true;
		} catch (NullPointerException npe) {	
			ServiceUtils.log(npe);
		} catch (RemoteException e) {
			ftView.getEntryPoint().onRemoteCallFailed(e);
		} catch (NotFoundException e1) {
			ServiceUtils.log(e1);
		}
		
		ContactListGridItem buddyItem = new ContactListGridItem(ftView.getEntryPoint(), buddy.protocolUid);
		buddyItem.populate(buddy, (int) (75*ftView.getEntryPoint().metrics.density), showIcons);
		
		buddyItem.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {							
					ftView.getEntryPoint().getConversationTab(ftView.getEntryPoint().runtimeService.getBuddy(buddy.serviceId, buddy.protocolUid));
				} catch (NullPointerException npe) {	
					ServiceUtils.log(npe);
				} catch (RemoteException e) {
					ftView.getEntryPoint().onRemoteCallFailed(e);
				}
			}
		});
		
		buddyIcon.addView(buddyItem);
		
		setOnLongClickListener(new OnLongClickListener(){

			@Override
			public boolean onLongClick(View v) {
				AlertDialog.Builder newBuilder = new AlertDialog.Builder(ftView.getEntryPoint());
				newBuilder.setMessage(ftView.getEntryPoint().getResources().getString(R.string.label_are_you_sure_you_want_to_cancel) + file + "?").setCancelable(false).setPositiveButton(R.string.label_yes, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							ftView.getEntryPoint().runtimeService.cancelFileTransfer(buddy.serviceId, messageId);
							ftView.removeTask(FileTransferItem.this);
						} catch (NullPointerException npe) {	
							ServiceUtils.log(npe);
						} catch (RemoteException e) {
							ftView.getEntryPoint().onRemoteCallFailed(e);
						}
					}

				}).setNegativeButton(R.string.label_no, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}

				});
				newBuilder.create().show();
				return true;
			}
			
		});
		
		progressBar.setMax(total);
		populate(progress, error);
	}
	
	public void populate(int progress, String error){
		final String file = filePath.substring(filePath.lastIndexOf(File.separator)+File.separator.length(), filePath.length());
		
		ServiceUtils.log("ft "+file+" "+progress);
		
		if (error != null){
			progressBar.setVisibility(View.GONE);
			title.setText(file+" - "+error);
			setBackgroundColor(0x77ff0000);
			
			this.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					ftView.removeTask(FileTransferItem.this);
				}
				
			});
		} else {
			if (progressBar.getVisibility() != View.VISIBLE){
				progressBar.setVisibility(View.VISIBLE);
			}
			if (progress > -1){
				progressBar.setIndeterminate(false);
				progressBar.setProgress(progress);
				if (progressBar.getProgress() == progressBar.getMax()){
					title.setText(file+" - "+ftView.getEntryPoint().getResources().getString(R.string.label_completed));
					setBackgroundColor(0x7700ff00);
					setLongClickable(false);
					this.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
							String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setDataAndType(Uri.parse("file://" + filePath), mime);
							try {
								ftView.getEntryPoint().startActivity(intent);
								ftView.removeTask(FileTransferItem.this);
							} catch (Exception e) {
								ServiceUtils.log(e);
							}
						}
						
					});
					
				} else {
					if (file.length()>0){
						title.setText(file);												
					}
					setBackgroundColor(0);	
				}
			} else {
				progressBar.setIndeterminate(true);
				title.setText(file+" - "+ftView.getEntryPoint().getResources().getString(R.string.label_connecting));
				setBackgroundColor(0x770000ff);				
			}		
		}
	}
}
