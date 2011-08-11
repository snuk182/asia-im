package ua.snuk182.asia.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import ua.snuk182.asia.core.dataentity.Buddy;
import ua.snuk182.asia.core.dataentity.TextMessage;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

public final class HistorySaver {
	public static final String MARK_IN = ">->";
	public static final String MARK_OUT = "<-<";
	
	private static final String SUFFIX = ".history";
	private static final String RECORD_DIVIDER = "--------------------------------------";
	
	private final static DateFormat formatter = new SimpleDateFormat("dd/MM/yy HH:mm:ss");	
	
	private Buddy buddy;
	
	public HistorySaver(Buddy buddy){
		this.buddy = buddy;
	}
	
	public static TextMessage formatMessageForHistory(TextMessage message, Buddy buddy, String myName){
		StringBuilder bu = new StringBuilder();
		
		if (buddy.protocolUid.equals(message.from)){
			bu.append(buddy.getName());
			
		} else {
			bu.append(myName);
		}
		
		bu.append(" (");
		
		bu.append(formatter.format(message.time));
		bu.append("): ");
		bu.append(message.text);
		
		message.text = bu.toString();
		
		return message;
	}
	
	public boolean deleteHistory(Context context){
		return context.deleteFile(buddy.getOwnerAccountId()+" "+buddy.protocolUid+SUFFIX);
	}
	
	public String exportHistory(Context context) {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			return "No external storage found";
		}
		FileInputStream fis;
		try {
			fis = context.openFileInput(buddy.getOwnerAccountId()+" "+buddy.protocolUid+SUFFIX);
		} catch (FileNotFoundException e) {
			return "No history found";
		}
		
		File root = Environment.getExternalStorageDirectory();
		File downloads = new File(root, "Asia");
		downloads.mkdirs();

		File file = new File(downloads, buddy.getOwnerAccountId()+" "+buddy.name+"("+buddy.protocolUid+")"+" history.txt");
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			FileOutputStream fos = new FileOutputStream(file, false);
			br = new BufferedReader(new InputStreamReader(new DataInputStream(fis)));
			bw = new BufferedWriter(new OutputStreamWriter(fos));
			String str;
			while((str = br.readLine()) != null){
				str = str.replaceAll("("+MARK_IN+"|"+MARK_OUT+"|"+RECORD_DIVIDER+")", "")+"\n";
				bw.write(str);
				bw.flush();
			}
		} catch (FileNotFoundException e) {
			return e.getLocalizedMessage();
		} catch (IOException e) {
			return e.getLocalizedMessage();
		} finally {
			try {
				br.close();
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "Saved to "+file.getAbsolutePath();
	}
	
	public List<TextMessage> getLastHistory(Context context, boolean getAll){
		FileInputStream fis = null;
		List<TextMessage> output = new ArrayList<TextMessage>();
		try {
			fis = context.openFileInput(buddy.getOwnerAccountId()+" "+buddy.protocolUid+SUFFIX);
		} catch (FileNotFoundException e) {
		}
		
		long fileSize = context.getFileStreamPath(buddy.getOwnerAccountId()+" "+buddy.protocolUid+SUFFIX).length();
		
		if (fis == null || fileSize<8){
			return output;
		}
		
		if (fileSize>1500 && !getAll){
			try {
				long co = fis.skip(fileSize-1500);
				Log.d("Asia", co+"");
			} catch (IOException e) {	
				e.printStackTrace();
			}
		}
		//char[] buf = new char[1000];
		String strLine = "";
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(fis)));
		try {
			 while ((strLine = br.readLine()) != null){
				 sb.append(strLine);
			 }
			/*while (true){
				int read = br.read(buf);
				if (read>-1){
					sb.append(buf, 0, read);
				} else {
					break;
				}
				
			}*/ 
		} catch (IOException e) {	
			e.printStackTrace();
		}
		
		String[] messages = sb.toString().split(RECORD_DIVIDER);
		for (String msg:messages){
			if (msg!=null){
				if (msg.length()>5){
					TextMessage message=null;
					int index = 0;
					if (msg.indexOf(MARK_IN)>-1){
						message = new TextMessage(buddy.protocolUid);
						index = msg.indexOf(MARK_IN);
					} else if (msg.indexOf(MARK_OUT)>-1){
						message = new TextMessage(buddy.ownerUid);
						index = msg.indexOf(MARK_OUT);
					} else {
						if (output.size()>0){
							output.get(output.size()-1).text = output.get(output.size()-1).text+msg;
						}
					}
					
					while (msg.lastIndexOf("\n")==msg.length()-1){
						msg = msg.substring(0, msg.length()-1);
					}
					
					if (message!=null){
						message.text = msg.substring(index+MARK_OUT.length(), msg.length());
						message.messageId = msg.hashCode();
						output.add(message);
					}
				}
			}
		}
		
		return output;
	}
	
	public void saveHistoryRecord(TextMessage message, Context context){
		new AsyncSaver(message, context).start();
	}
	
	class AsyncSaver extends Thread {
		
		TextMessage message;
		Context context;
		
		public AsyncSaver(TextMessage message, Context context){
			this.message = message;
			this.context = context;
			setName("History saver "+buddy.getName());
		}
	
		@Override
		public void run(){
			FileOutputStream fos = null;
			try {
				fos = context.openFileOutput(buddy.getOwnerAccountId()+" "+buddy.protocolUid+SUFFIX, Context.MODE_APPEND);
			} catch (FileNotFoundException e) {
			}
			if (fos == null || message == null){
				return;
			}
			byte[] buffer = null;
			if (message.from.equals(buddy.ownerUid)){ //out
				buffer = new String(RECORD_DIVIDER+MARK_OUT+"\n").getBytes();				
			} else {
				buffer = new String(RECORD_DIVIDER+MARK_IN+"\n").getBytes();	
			}
			
			try {
				fos.write(buffer);
				buffer = new String(message.text+"\n").getBytes();
				fos.write(buffer);
				fos.close();
			} catch (IOException e) {
			}
			return;
		}
	}

	
}
