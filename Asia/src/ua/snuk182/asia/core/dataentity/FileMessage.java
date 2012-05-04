package ua.snuk182.asia.core.dataentity;

import java.util.LinkedList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * File transfer message entity.
 * 
 * @author Sergiy Plygun
 *
 */
public class FileMessage extends Message {
	
	/**
	 * List of files to be sent or received
	 */
	public final List<FileInfo> files = new LinkedList<FileInfo>();

	public FileMessage(Parcel arg0) {
		super(arg0);
		readFromParcel(arg0);
	}
	
	public FileMessage(String from){
		super(from);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeList(files);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void readFromParcel(Parcel in){
		super.readFromParcel(in);
		files.addAll(in.readArrayList(FileInfo.class.getClassLoader()));
	}

	public static final Parcelable.Creator<FileMessage> CREATOR = new Parcelable.Creator<FileMessage>(){

		@Override
		public FileMessage createFromParcel(Parcel arg0) {
			return new FileMessage(arg0);
		}

		@Override
		public FileMessage[] newArray(int size) {
			return new FileMessage[size];
		}

	};
}
