package ua.snuk182.asia.view;

import ua.snuk182.asia.core.dataentity.Buddy;

public interface IHasFileTransfer extends IHasAccount {

	void notifyFileProgress(long messageId, Buddy buddy, String filename, long totalSize, long sizeTransferred, Boolean isReceive, String error);

}
