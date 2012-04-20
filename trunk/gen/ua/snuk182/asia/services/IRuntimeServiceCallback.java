/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\studyworkspace\\Asia\\src\\ua\\snuk182\\asia\\services\\IRuntimeServiceCallback.aidl
 */
package ua.snuk182.asia.services;
public interface IRuntimeServiceCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements ua.snuk182.asia.services.IRuntimeServiceCallback
{
private static final java.lang.String DESCRIPTOR = "ua.snuk182.asia.services.IRuntimeServiceCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an ua.snuk182.asia.services.IRuntimeServiceCallback interface,
 * generating a proxy if needed.
 */
public static ua.snuk182.asia.services.IRuntimeServiceCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof ua.snuk182.asia.services.IRuntimeServiceCallback))) {
return ((ua.snuk182.asia.services.IRuntimeServiceCallback)iin);
}
return new ua.snuk182.asia.services.IRuntimeServiceCallback.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_accountConnected:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.AccountView _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.AccountView.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.accountConnected(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_icon:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
java.lang.String _arg1;
_arg1 = data.readString();
this.icon(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_contactListUpdated:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.AccountView _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.AccountView.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.contactListUpdated(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_buddyStateChanged:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.Buddy _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.Buddy.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.buddyStateChanged(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_connecting:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
int _arg1;
_arg1 = data.readInt();
this.connecting(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_accountUpdated:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.AccountView _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.AccountView.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.accountUpdated(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_serviceMessage:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.ServiceMessage _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.ServiceMessage.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.serviceMessage(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_fileMessage:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.FileMessage _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.FileMessage.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.fileMessage(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_searchResult:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
java.util.List<ua.snuk182.asia.core.dataentity.PersonalInfo> _arg1;
_arg1 = data.createTypedArrayList(ua.snuk182.asia.core.dataentity.PersonalInfo.CREATOR);
this.searchResult(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_groupAdded:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.BuddyGroup _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.BuddyGroup.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
ua.snuk182.asia.core.dataentity.AccountView _arg1;
if ((0!=data.readInt())) {
_arg1 = ua.snuk182.asia.core.dataentity.AccountView.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
this.groupAdded(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_buddyAdded:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.Buddy _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.Buddy.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
ua.snuk182.asia.core.dataentity.AccountView _arg1;
if ((0!=data.readInt())) {
_arg1 = ua.snuk182.asia.core.dataentity.AccountView.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
this.buddyAdded(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_buddyRemoved:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.Buddy _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.Buddy.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
ua.snuk182.asia.core.dataentity.AccountView _arg1;
if ((0!=data.readInt())) {
_arg1 = ua.snuk182.asia.core.dataentity.AccountView.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
this.buddyRemoved(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_groupRemoved:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.BuddyGroup _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.BuddyGroup.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
ua.snuk182.asia.core.dataentity.AccountView _arg1;
if ((0!=data.readInt())) {
_arg1 = ua.snuk182.asia.core.dataentity.AccountView.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
this.groupRemoved(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_buddyEdited:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.Buddy _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.Buddy.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
ua.snuk182.asia.core.dataentity.AccountView _arg1;
if ((0!=data.readInt())) {
_arg1 = ua.snuk182.asia.core.dataentity.AccountView.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
this.buddyEdited(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_groupEdited:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.BuddyGroup _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.BuddyGroup.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
ua.snuk182.asia.core.dataentity.AccountView _arg1;
if ((0!=data.readInt())) {
_arg1 = ua.snuk182.asia.core.dataentity.AccountView.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
this.groupEdited(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_disconnected:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.AccountView _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.AccountView.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.disconnected(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_textMessage:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.TextMessage _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.TextMessage.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.textMessage(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_accountAdded:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.AccountView _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.AccountView.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.accountAdded(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_status:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.AccountView _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.AccountView.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.status(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_accountRemoved:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.AccountView _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.AccountView.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.accountRemoved(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_visualStyleUpdated:
{
data.enforceInterface(DESCRIPTOR);
this.visualStyleUpdated();
reply.writeNoException();
return true;
}
case TRANSACTION_fileProgress:
{
data.enforceInterface(DESCRIPTOR);
long _arg0;
_arg0 = data.readLong();
ua.snuk182.asia.core.dataentity.Buddy _arg1;
if ((0!=data.readInt())) {
_arg1 = ua.snuk182.asia.core.dataentity.Buddy.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
java.lang.String _arg2;
_arg2 = data.readString();
long _arg3;
_arg3 = data.readLong();
long _arg4;
_arg4 = data.readLong();
boolean _arg5;
_arg5 = (0!=data.readInt());
java.lang.String _arg6;
_arg6 = data.readString();
this.fileProgress(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6);
reply.writeNoException();
return true;
}
case TRANSACTION_messageAck:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.Buddy _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.Buddy.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
long _arg1;
_arg1 = data.readLong();
int _arg2;
_arg2 = data.readInt();
this.messageAck(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_personalInfo:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.Buddy _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.Buddy.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
ua.snuk182.asia.core.dataentity.PersonalInfo _arg1;
if ((0!=data.readInt())) {
_arg1 = ua.snuk182.asia.core.dataentity.PersonalInfo.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
this.personalInfo(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_typing:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
java.lang.String _arg1;
_arg1 = data.readString();
this.typing(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_availableChatsList:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
java.util.List<ua.snuk182.asia.core.dataentity.MultiChatRoom> _arg1;
_arg1 = data.createTypedArrayList(ua.snuk182.asia.core.dataentity.MultiChatRoom.CREATOR);
this.availableChatsList(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_chatRoomOccupants:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
java.lang.String _arg1;
_arg1 = data.readString();
ua.snuk182.asia.core.dataentity.MultiChatRoomOccupants _arg2;
if ((0!=data.readInt())) {
_arg2 = ua.snuk182.asia.core.dataentity.MultiChatRoomOccupants.CREATOR.createFromParcel(data);
}
else {
_arg2 = null;
}
this.chatRoomOccupants(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements ua.snuk182.asia.services.IRuntimeServiceCallback
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/*
	public static final short RES_NOP = 0;
	public static final short RES_SAVEPARAMS = 1;
	public static final short RES_CONNECTED = 2;
	public static final short RES_DISCONNECTED = 3;
	public static final short RES_STATUSSET = 4;
	public static final short RES_EXTENDEDSTATUSSET = 5;
	public static final short RES_MESSAGE = 6;
	public static final short RES_FILEMESSAGE = 7;
	public static final short RES_USERINFO = 8;
	public static final short RES_OWNINFO = 9;
	public static final short RES_OWNINFOSET = 10;
	public static final short RES_BUDDYADDED = 11;
	public static final short RES_BUDDYDELETED = 12;
	public static final short RES_CLUPDATED = 13;
	*//*void accountAdded(in AccountView account);
	void accountInfoUpdated(in AccountView account);
	void accountRemoved(in AccountView account);
	void accountStateChanged(in AccountView account);
	void textMessage(in TextMessage message);
	void buddyStateChanged(in Buddy buddy);
	void buddyGroupAdded(in BuddyGroup group);
	void buddyGroupRemoved(in BuddyGroup group);
	void serviceNotification(byte serviceId, String text, byte type);
	void serviceMessage(in ServiceMessage msg);
	void buddySearchResult(byte serviceId, in List<PersonalInfo> infos);
	void visualStyleUpdated();
	void connectionState(byte serviceId, int state);
	void bitmap(byte serviceId, String uid);*/
public void accountConnected(ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((account!=null)) {
_data.writeInt(1);
account.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_accountConnected, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void icon(byte serviceId, java.lang.String uid) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeString(uid);
mRemote.transact(Stub.TRANSACTION_icon, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void contactListUpdated(ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((account!=null)) {
_data.writeInt(1);
account.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_contactListUpdated, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void buddyStateChanged(ua.snuk182.asia.core.dataentity.Buddy buddy) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((buddy!=null)) {
_data.writeInt(1);
buddy.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_buddyStateChanged, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
//void notification(String text, int type);

public void connecting(byte serviceId, int progress) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeInt(progress);
mRemote.transact(Stub.TRANSACTION_connecting, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void accountUpdated(ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((account!=null)) {
_data.writeInt(1);
account.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_accountUpdated, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void serviceMessage(ua.snuk182.asia.core.dataentity.ServiceMessage message) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((message!=null)) {
_data.writeInt(1);
message.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_serviceMessage, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void fileMessage(ua.snuk182.asia.core.dataentity.FileMessage message) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((message!=null)) {
_data.writeInt(1);
message.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_fileMessage, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void searchResult(byte serviceId, java.util.List<ua.snuk182.asia.core.dataentity.PersonalInfo> infos) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeTypedList(infos);
mRemote.transact(Stub.TRANSACTION_searchResult, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void groupAdded(ua.snuk182.asia.core.dataentity.BuddyGroup group, ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((group!=null)) {
_data.writeInt(1);
group.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
if ((account!=null)) {
_data.writeInt(1);
account.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_groupAdded, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void buddyAdded(ua.snuk182.asia.core.dataentity.Buddy buddy, ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((buddy!=null)) {
_data.writeInt(1);
buddy.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
if ((account!=null)) {
_data.writeInt(1);
account.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_buddyAdded, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void buddyRemoved(ua.snuk182.asia.core.dataentity.Buddy buddy, ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((buddy!=null)) {
_data.writeInt(1);
buddy.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
if ((account!=null)) {
_data.writeInt(1);
account.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_buddyRemoved, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void groupRemoved(ua.snuk182.asia.core.dataentity.BuddyGroup group, ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((group!=null)) {
_data.writeInt(1);
group.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
if ((account!=null)) {
_data.writeInt(1);
account.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_groupRemoved, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void buddyEdited(ua.snuk182.asia.core.dataentity.Buddy buddy, ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((buddy!=null)) {
_data.writeInt(1);
buddy.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
if ((account!=null)) {
_data.writeInt(1);
account.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_buddyEdited, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void groupEdited(ua.snuk182.asia.core.dataentity.BuddyGroup group, ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((group!=null)) {
_data.writeInt(1);
group.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
if ((account!=null)) {
_data.writeInt(1);
account.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_groupEdited, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void disconnected(ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((account!=null)) {
_data.writeInt(1);
account.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_disconnected, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void textMessage(ua.snuk182.asia.core.dataentity.TextMessage message) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((message!=null)) {
_data.writeInt(1);
message.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_textMessage, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void accountAdded(ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((account!=null)) {
_data.writeInt(1);
account.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_accountAdded, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void status(ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((account!=null)) {
_data.writeInt(1);
account.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_status, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void accountRemoved(ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((account!=null)) {
_data.writeInt(1);
account.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_accountRemoved, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void visualStyleUpdated() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_visualStyleUpdated, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void fileProgress(long messageId, ua.snuk182.asia.core.dataentity.Buddy buddy, java.lang.String filename, long totalSize, long sizeTransferred, boolean isReceive, java.lang.String error) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeLong(messageId);
if ((buddy!=null)) {
_data.writeInt(1);
buddy.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeString(filename);
_data.writeLong(totalSize);
_data.writeLong(sizeTransferred);
_data.writeInt(((isReceive)?(1):(0)));
_data.writeString(error);
mRemote.transact(Stub.TRANSACTION_fileProgress, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void messageAck(ua.snuk182.asia.core.dataentity.Buddy buddy, long messageId, int level) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((buddy!=null)) {
_data.writeInt(1);
buddy.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeLong(messageId);
_data.writeInt(level);
mRemote.transact(Stub.TRANSACTION_messageAck, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void personalInfo(ua.snuk182.asia.core.dataentity.Buddy buddy, ua.snuk182.asia.core.dataentity.PersonalInfo info) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((buddy!=null)) {
_data.writeInt(1);
buddy.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
if ((info!=null)) {
_data.writeInt(1);
info.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_personalInfo, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void typing(byte serviceId, java.lang.String buddyUid) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeString(buddyUid);
mRemote.transact(Stub.TRANSACTION_typing, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void availableChatsList(byte serviceId, java.util.List<ua.snuk182.asia.core.dataentity.MultiChatRoom> chats) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeTypedList(chats);
mRemote.transact(Stub.TRANSACTION_availableChatsList, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void chatRoomOccupants(byte serviceId, java.lang.String chatId, ua.snuk182.asia.core.dataentity.MultiChatRoomOccupants occupants) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeString(chatId);
if ((occupants!=null)) {
_data.writeInt(1);
occupants.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_chatRoomOccupants, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_accountConnected = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_icon = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_contactListUpdated = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_buddyStateChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_connecting = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_accountUpdated = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_serviceMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_fileMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_searchResult = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_groupAdded = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_buddyAdded = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_buddyRemoved = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_groupRemoved = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_buddyEdited = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_groupEdited = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_disconnected = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_textMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_accountAdded = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
static final int TRANSACTION_status = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
static final int TRANSACTION_accountRemoved = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
static final int TRANSACTION_visualStyleUpdated = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
static final int TRANSACTION_fileProgress = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
static final int TRANSACTION_messageAck = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
static final int TRANSACTION_personalInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 23);
static final int TRANSACTION_typing = (android.os.IBinder.FIRST_CALL_TRANSACTION + 24);
static final int TRANSACTION_availableChatsList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 25);
static final int TRANSACTION_chatRoomOccupants = (android.os.IBinder.FIRST_CALL_TRANSACTION + 26);
}
/*
	public static final short RES_NOP = 0;
	public static final short RES_SAVEPARAMS = 1;
	public static final short RES_CONNECTED = 2;
	public static final short RES_DISCONNECTED = 3;
	public static final short RES_STATUSSET = 4;
	public static final short RES_EXTENDEDSTATUSSET = 5;
	public static final short RES_MESSAGE = 6;
	public static final short RES_FILEMESSAGE = 7;
	public static final short RES_USERINFO = 8;
	public static final short RES_OWNINFO = 9;
	public static final short RES_OWNINFOSET = 10;
	public static final short RES_BUDDYADDED = 11;
	public static final short RES_BUDDYDELETED = 12;
	public static final short RES_CLUPDATED = 13;
	*//*void accountAdded(in AccountView account);
	void accountInfoUpdated(in AccountView account);
	void accountRemoved(in AccountView account);
	void accountStateChanged(in AccountView account);
	void textMessage(in TextMessage message);
	void buddyStateChanged(in Buddy buddy);
	void buddyGroupAdded(in BuddyGroup group);
	void buddyGroupRemoved(in BuddyGroup group);
	void serviceNotification(byte serviceId, String text, byte type);
	void serviceMessage(in ServiceMessage msg);
	void buddySearchResult(byte serviceId, in List<PersonalInfo> infos);
	void visualStyleUpdated();
	void connectionState(byte serviceId, int state);
	void bitmap(byte serviceId, String uid);*/
public void accountConnected(ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException;
public void icon(byte serviceId, java.lang.String uid) throws android.os.RemoteException;
public void contactListUpdated(ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException;
public void buddyStateChanged(ua.snuk182.asia.core.dataentity.Buddy buddy) throws android.os.RemoteException;
//void notification(String text, int type);

public void connecting(byte serviceId, int progress) throws android.os.RemoteException;
public void accountUpdated(ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException;
public void serviceMessage(ua.snuk182.asia.core.dataentity.ServiceMessage message) throws android.os.RemoteException;
public void fileMessage(ua.snuk182.asia.core.dataentity.FileMessage message) throws android.os.RemoteException;
public void searchResult(byte serviceId, java.util.List<ua.snuk182.asia.core.dataentity.PersonalInfo> infos) throws android.os.RemoteException;
public void groupAdded(ua.snuk182.asia.core.dataentity.BuddyGroup group, ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException;
public void buddyAdded(ua.snuk182.asia.core.dataentity.Buddy buddy, ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException;
public void buddyRemoved(ua.snuk182.asia.core.dataentity.Buddy buddy, ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException;
public void groupRemoved(ua.snuk182.asia.core.dataentity.BuddyGroup group, ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException;
public void buddyEdited(ua.snuk182.asia.core.dataentity.Buddy buddy, ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException;
public void groupEdited(ua.snuk182.asia.core.dataentity.BuddyGroup group, ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException;
public void disconnected(ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException;
public void textMessage(ua.snuk182.asia.core.dataentity.TextMessage message) throws android.os.RemoteException;
public void accountAdded(ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException;
public void status(ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException;
public void accountRemoved(ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException;
public void visualStyleUpdated() throws android.os.RemoteException;
public void fileProgress(long messageId, ua.snuk182.asia.core.dataentity.Buddy buddy, java.lang.String filename, long totalSize, long sizeTransferred, boolean isReceive, java.lang.String error) throws android.os.RemoteException;
public void messageAck(ua.snuk182.asia.core.dataentity.Buddy buddy, long messageId, int level) throws android.os.RemoteException;
public void personalInfo(ua.snuk182.asia.core.dataentity.Buddy buddy, ua.snuk182.asia.core.dataentity.PersonalInfo info) throws android.os.RemoteException;
public void typing(byte serviceId, java.lang.String buddyUid) throws android.os.RemoteException;
public void availableChatsList(byte serviceId, java.util.List<ua.snuk182.asia.core.dataentity.MultiChatRoom> chats) throws android.os.RemoteException;
public void chatRoomOccupants(byte serviceId, java.lang.String chatId, ua.snuk182.asia.core.dataentity.MultiChatRoomOccupants occupants) throws android.os.RemoteException;
}
