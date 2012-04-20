package ua.snuk182.asia.core.dataentity;

import android.os.Parcel;
import android.os.Parcelable;

public class FileInfo implements Parcelable {

	public long size;
	public String filename;
	
	public FileInfo(Parcel arg0) {
		readFromParcel(arg0);
	}

	public FileInfo() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(filename);
		dest.writeLong(size);
	}
	
	private void readFromParcel(Parcel in){
		filename = in.readString();
		size = in.readLong();
	}

	public static final Parcelable.Creator<FileInfo> CREATOR = new Parcelable.Creator<FileInfo>(){

		@Override
		public FileInfo createFromParcel(Parcel arg0) {
			return new FileInfo(arg0);
		}

		@Override
		public FileInfo[] newArray(int size) {
			return new FileInfo[size];
		}
	};
}
