/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\studyworkspace\\Asia\\src\\ua\\snuk182\\asia\\services\\IRuntimeService.aidl
 */
package ua.snuk182.asia.services;
public interface IRuntimeService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements ua.snuk182.asia.services.IRuntimeService
{
private static final java.lang.String DESCRIPTOR = "ua.snuk182.asia.services.IRuntimeService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an ua.snuk182.asia.services.IRuntimeService interface,
 * generating a proxy if needed.
 */
public static ua.snuk182.asia.services.IRuntimeService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof ua.snuk182.asia.services.IRuntimeService))) {
return ((ua.snuk182.asia.services.IRuntimeService)iin);
}
return new ua.snuk182.asia.services.IRuntimeService.Stub.Proxy(obj);
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
case TRANSACTION_sendMessage:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.TextMessage _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.TextMessage.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
byte _arg1;
_arg1 = data.readByte();
java.lang.String _result = this.sendMessage(_arg0, _arg1);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_createAccount:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.AccountView _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.AccountView.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
byte _result = this.createAccount(_arg0);
reply.writeNoException();
reply.writeByte(_result);
return true;
}
case TRANSACTION_deleteAccount:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.AccountView _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.AccountView.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.deleteAccount(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_editAccount:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.AccountView _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.AccountView.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.editAccount(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getAccounts:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
java.util.List<ua.snuk182.asia.core.dataentity.AccountView> _result = this.getAccounts(_arg0);
reply.writeNoException();
reply.writeTypedList(_result);
return true;
}
case TRANSACTION_getBuddy:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
java.lang.String _arg1;
_arg1 = data.readString();
ua.snuk182.asia.core.dataentity.Buddy _result = this.getBuddy(_arg0, _arg1);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_getBuddies:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
java.util.List<java.lang.String> _arg1;
_arg1 = data.createStringArrayList();
java.util.List<ua.snuk182.asia.core.dataentity.Buddy> _result = this.getBuddies(_arg0, _arg1);
reply.writeNoException();
reply.writeTypedList(_result);
return true;
}
case TRANSACTION_getSavedTabs:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<ua.snuk182.asia.core.dataentity.TabInfo> _result = this.getSavedTabs();
reply.writeNoException();
reply.writeTypedList(_result);
return true;
}
case TRANSACTION_saveTabs:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<ua.snuk182.asia.core.dataentity.TabInfo> _arg0;
_arg0 = data.createTypedArrayList(ua.snuk182.asia.core.dataentity.TabInfo.CREATOR);
this.saveTabs(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getAccountView:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
ua.snuk182.asia.core.dataentity.AccountView _result = this.getAccountView(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_connect:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
this.connect(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_isDataSetInvalid:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
long _arg1;
_arg1 = data.readLong();
boolean _result = this.isDataSetInvalid(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_connectAll:
{
data.enforceInterface(DESCRIPTOR);
this.connectAll();
reply.writeNoException();
return true;
}
case TRANSACTION_disconnectAll:
{
data.enforceInterface(DESCRIPTOR);
this.disconnectAll();
reply.writeNoException();
return true;
}
case TRANSACTION_disconnect:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
this.disconnect(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_registerCallback:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.services.IRuntimeServiceCallback _arg0;
_arg0 = ua.snuk182.asia.services.IRuntimeServiceCallback.Stub.asInterface(data.readStrongBinder());
this.registerCallback(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setUnread:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.Buddy _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.Buddy.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
ua.snuk182.asia.core.dataentity.TextMessage _arg1;
if ((0!=data.readInt())) {
_arg1 = ua.snuk182.asia.core.dataentity.TextMessage.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
this.setUnread(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_setServiceMessageUnread:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
boolean _arg1;
_arg1 = (0!=data.readInt());
ua.snuk182.asia.core.dataentity.ServiceMessage _arg2;
if ((0!=data.readInt())) {
_arg2 = ua.snuk182.asia.core.dataentity.ServiceMessage.CREATOR.createFromParcel(data);
}
else {
_arg2 = null;
}
this.setServiceMessageUnread(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_savePreference:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
byte _arg2;
_arg2 = data.readByte();
this.savePreference(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_getApplicationOptions:
{
data.enforceInterface(DESCRIPTOR);
android.os.Bundle _result = this.getApplicationOptions();
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_getProtocolServiceOptions:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
android.os.Bundle _result = this.getProtocolServiceOptions(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_saveProtocolServiceOptions:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
android.os.Bundle _arg1;
if ((0!=data.readInt())) {
_arg1 = android.os.Bundle.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
this.saveProtocolServiceOptions(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_askForXStatus:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.Buddy _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.Buddy.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.askForXStatus(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_addBuddy:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.Buddy _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.Buddy.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.addBuddy(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_removeBuddy:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.Buddy _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.Buddy.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.removeBuddy(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_renameBuddy:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.Buddy _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.Buddy.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.renameBuddy(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_moveBuddy:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.Buddy _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.Buddy.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
ua.snuk182.asia.core.dataentity.BuddyGroup _arg1;
if ((0!=data.readInt())) {
_arg1 = ua.snuk182.asia.core.dataentity.BuddyGroup.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
ua.snuk182.asia.core.dataentity.BuddyGroup _arg2;
if ((0!=data.readInt())) {
_arg2 = ua.snuk182.asia.core.dataentity.BuddyGroup.CREATOR.createFromParcel(data);
}
else {
_arg2 = null;
}
this.moveBuddy(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_addGroup:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.BuddyGroup _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.BuddyGroup.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.addGroup(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_removeGroup:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.BuddyGroup _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.BuddyGroup.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.util.List<ua.snuk182.asia.core.dataentity.Buddy> _arg1;
_arg1 = data.createTypedArrayList(ua.snuk182.asia.core.dataentity.Buddy.CREATOR);
ua.snuk182.asia.core.dataentity.BuddyGroup _arg2;
if ((0!=data.readInt())) {
_arg2 = ua.snuk182.asia.core.dataentity.BuddyGroup.CREATOR.createFromParcel(data);
}
else {
_arg2 = null;
}
this.removeGroup(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_renameGroup:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.BuddyGroup _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.BuddyGroup.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.renameGroup(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setGroupCollapsed:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
int _arg1;
_arg1 = data.readInt();
boolean _arg2;
_arg2 = (0!=data.readInt());
this.setGroupCollapsed(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_getBuddiesFromGroup:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.BuddyGroup _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.BuddyGroup.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.util.List<ua.snuk182.asia.core.dataentity.Buddy> _result = this.getBuddiesFromGroup(_arg0);
reply.writeNoException();
reply.writeTypedList(_result);
return true;
}
case TRANSACTION_requestBuddyShortInfo:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
java.lang.String _arg1;
_arg1 = data.readString();
this.requestBuddyShortInfo(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_requestBuddyFullInfo:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
java.lang.String _arg1;
_arg1 = data.readString();
this.requestBuddyFullInfo(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_requestAuthorization:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.Buddy _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.Buddy.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.lang.String _arg1;
_arg1 = data.readString();
this.requestAuthorization(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_respondAuthorization:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.Buddy _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.Buddy.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
boolean _arg1;
_arg1 = (0!=data.readInt());
this.respondAuthorization(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_searchUsersByUid:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
java.lang.String _arg1;
_arg1 = data.readString();
this.searchUsersByUid(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_respondFileMessage:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.FileMessage _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.FileMessage.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
boolean _arg1;
_arg1 = (0!=data.readInt());
this.respondFileMessage(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_sendFile:
{
data.enforceInterface(DESCRIPTOR);
android.os.Bundle _arg0;
if ((0!=data.readInt())) {
_arg0 = android.os.Bundle.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
ua.snuk182.asia.core.dataentity.Buddy _arg1;
if ((0!=data.readInt())) {
_arg1 = ua.snuk182.asia.core.dataentity.Buddy.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
this.sendFile(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_cancelFileTransfer:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
long _arg1;
_arg1 = data.readLong();
this.cancelFileTransfer(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_getServiceMessages:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
java.lang.String _arg1;
_arg1 = data.readString();
java.util.List<ua.snuk182.asia.core.dataentity.ServiceMessage> _result = this.getServiceMessages(_arg0, _arg1);
reply.writeNoException();
reply.writeTypedList(_result);
return true;
}
case TRANSACTION_setStatus:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
byte _arg1;
_arg1 = data.readByte();
this.setStatus(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_setXStatus:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.AccountView _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.AccountView.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.setXStatus(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_prepareExit:
{
data.enforceInterface(DESCRIPTOR);
this.prepareExit();
reply.writeNoException();
return true;
}
case TRANSACTION_sendTyping:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
java.lang.String _arg1;
_arg1 = data.readString();
this.sendTyping(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_editBuddyVisibility:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.Buddy _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.Buddy.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.editBuddyVisibility(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_editBuddy:
{
data.enforceInterface(DESCRIPTOR);
ua.snuk182.asia.core.dataentity.Buddy _arg0;
if ((0!=data.readInt())) {
_arg0 = ua.snuk182.asia.core.dataentity.Buddy.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.editBuddy(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_editMyVisibility:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
byte _arg1;
_arg1 = data.readByte();
this.editMyVisibility(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_requestAvailableChatRooms:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
this.requestAvailableChatRooms(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_createChat:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _arg3;
_arg3 = data.readString();
java.lang.String _arg4;
_arg4 = data.readString();
byte _result = this.createChat(_arg0, _arg1, _arg2, _arg3, _arg4);
reply.writeNoException();
reply.writeByte(_result);
return true;
}
case TRANSACTION_joinExistingChat:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
java.lang.String _arg1;
_arg1 = data.readString();
byte _result = this.joinExistingChat(_arg0, _arg1);
reply.writeNoException();
reply.writeByte(_result);
return true;
}
case TRANSACTION_leaveChat:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
java.lang.String _arg1;
_arg1 = data.readString();
byte _result = this.leaveChat(_arg0, _arg1);
reply.writeNoException();
reply.writeByte(_result);
return true;
}
case TRANSACTION_joinChat:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _arg3;
_arg3 = data.readString();
byte _result = this.joinChat(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
reply.writeByte(_result);
return true;
}
case TRANSACTION_checkGroupChatsAvailability:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
boolean _result = this.checkGroupChatsAvailability(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getChatRoomOccupants:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
java.lang.String _arg1;
_arg1 = data.readString();
ua.snuk182.asia.core.dataentity.MultiChatRoomOccupants _result = this.getChatRoomOccupants(_arg0, _arg1);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_getChatInfo:
{
data.enforceInterface(DESCRIPTOR);
byte _arg0;
_arg0 = data.readByte();
java.lang.String _arg1;
_arg1 = data.readString();
ua.snuk182.asia.core.dataentity.PersonalInfo _result = this.getChatInfo(_arg0, _arg1);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_setCurrentTabs:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<java.lang.String> _arg0;
_arg0 = data.createStringArrayList();
this.setCurrentTabs(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements ua.snuk182.asia.services.IRuntimeService
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
public java.lang.String sendMessage(ua.snuk182.asia.core.dataentity.TextMessage message, byte serviceId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((message!=null)) {
_data.writeInt(1);
message.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeByte(serviceId);
mRemote.transact(Stub.TRANSACTION_sendMessage, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public byte createAccount(ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
byte _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((account!=null)) {
_data.writeInt(1);
account.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_createAccount, _data, _reply, 0);
_reply.readException();
_result = _reply.readByte();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void deleteAccount(ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException
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
mRemote.transact(Stub.TRANSACTION_deleteAccount, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void editAccount(ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException
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
mRemote.transact(Stub.TRANSACTION_editAccount, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public java.util.List<ua.snuk182.asia.core.dataentity.AccountView> getAccounts(boolean disabledToo) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List<ua.snuk182.asia.core.dataentity.AccountView> _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((disabledToo)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_getAccounts, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArrayList(ua.snuk182.asia.core.dataentity.AccountView.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public ua.snuk182.asia.core.dataentity.Buddy getBuddy(byte serviceId, java.lang.String buddyProtocolUid) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
ua.snuk182.asia.core.dataentity.Buddy _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeString(buddyProtocolUid);
mRemote.transact(Stub.TRANSACTION_getBuddy, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = ua.snuk182.asia.core.dataentity.Buddy.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public java.util.List<ua.snuk182.asia.core.dataentity.Buddy> getBuddies(byte serviceId, java.util.List<java.lang.String> buddyProtocolUid) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List<ua.snuk182.asia.core.dataentity.Buddy> _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeStringList(buddyProtocolUid);
mRemote.transact(Stub.TRANSACTION_getBuddies, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArrayList(ua.snuk182.asia.core.dataentity.Buddy.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public java.util.List<ua.snuk182.asia.core.dataentity.TabInfo> getSavedTabs() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List<ua.snuk182.asia.core.dataentity.TabInfo> _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSavedTabs, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArrayList(ua.snuk182.asia.core.dataentity.TabInfo.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void saveTabs(java.util.List<ua.snuk182.asia.core.dataentity.TabInfo> tabInfos) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeTypedList(tabInfos);
mRemote.transact(Stub.TRANSACTION_saveTabs, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public ua.snuk182.asia.core.dataentity.AccountView getAccountView(byte serviceId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
ua.snuk182.asia.core.dataentity.AccountView _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
mRemote.transact(Stub.TRANSACTION_getAccountView, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = ua.snuk182.asia.core.dataentity.AccountView.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void connect(byte serviceId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
mRemote.transact(Stub.TRANSACTION_connect, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
//void setAppVisible(boolean visible);

public boolean isDataSetInvalid(byte serviceId, long lastUpdateTime) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeLong(lastUpdateTime);
mRemote.transact(Stub.TRANSACTION_isDataSetInvalid, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void connectAll() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_connectAll, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void disconnectAll() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_disconnectAll, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void disconnect(byte serviceId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
mRemote.transact(Stub.TRANSACTION_disconnect, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void registerCallback(ua.snuk182.asia.services.IRuntimeServiceCallback callback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callback!=null))?(callback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void setUnread(ua.snuk182.asia.core.dataentity.Buddy buddy, ua.snuk182.asia.core.dataentity.TextMessage message) throws android.os.RemoteException
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
if ((message!=null)) {
_data.writeInt(1);
message.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_setUnread, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void setServiceMessageUnread(byte serviceId, boolean unread, ua.snuk182.asia.core.dataentity.ServiceMessage message) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeInt(((unread)?(1):(0)));
if ((message!=null)) {
_data.writeInt(1);
message.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_setServiceMessageUnread, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void savePreference(java.lang.String key, java.lang.String value, byte serviceId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(key);
_data.writeString(value);
_data.writeByte(serviceId);
mRemote.transact(Stub.TRANSACTION_savePreference, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public android.os.Bundle getApplicationOptions() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.os.Bundle _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getApplicationOptions, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = android.os.Bundle.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public android.os.Bundle getProtocolServiceOptions(byte serviceId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.os.Bundle _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
mRemote.transact(Stub.TRANSACTION_getProtocolServiceOptions, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = android.os.Bundle.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void saveProtocolServiceOptions(byte serviceId, android.os.Bundle options) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
if ((options!=null)) {
_data.writeInt(1);
options.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_saveProtocolServiceOptions, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void askForXStatus(ua.snuk182.asia.core.dataentity.Buddy buddy) throws android.os.RemoteException
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
mRemote.transact(Stub.TRANSACTION_askForXStatus, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void addBuddy(ua.snuk182.asia.core.dataentity.Buddy buddy) throws android.os.RemoteException
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
mRemote.transact(Stub.TRANSACTION_addBuddy, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void removeBuddy(ua.snuk182.asia.core.dataentity.Buddy buddy) throws android.os.RemoteException
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
mRemote.transact(Stub.TRANSACTION_removeBuddy, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void renameBuddy(ua.snuk182.asia.core.dataentity.Buddy buddy) throws android.os.RemoteException
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
mRemote.transact(Stub.TRANSACTION_renameBuddy, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void moveBuddy(ua.snuk182.asia.core.dataentity.Buddy buddy, ua.snuk182.asia.core.dataentity.BuddyGroup oldGroup, ua.snuk182.asia.core.dataentity.BuddyGroup newGroup) throws android.os.RemoteException
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
if ((oldGroup!=null)) {
_data.writeInt(1);
oldGroup.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
if ((newGroup!=null)) {
_data.writeInt(1);
newGroup.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_moveBuddy, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void addGroup(ua.snuk182.asia.core.dataentity.BuddyGroup group) throws android.os.RemoteException
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
mRemote.transact(Stub.TRANSACTION_addGroup, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void removeGroup(ua.snuk182.asia.core.dataentity.BuddyGroup group, java.util.List<ua.snuk182.asia.core.dataentity.Buddy> buddy, ua.snuk182.asia.core.dataentity.BuddyGroup newGroupForBuddies) throws android.os.RemoteException
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
_data.writeTypedList(buddy);
if ((newGroupForBuddies!=null)) {
_data.writeInt(1);
newGroupForBuddies.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_removeGroup, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void renameGroup(ua.snuk182.asia.core.dataentity.BuddyGroup group) throws android.os.RemoteException
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
mRemote.transact(Stub.TRANSACTION_renameGroup, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void setGroupCollapsed(byte serviceId, int groupId, boolean collapsed) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeInt(groupId);
_data.writeInt(((collapsed)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_setGroupCollapsed, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public java.util.List<ua.snuk182.asia.core.dataentity.Buddy> getBuddiesFromGroup(ua.snuk182.asia.core.dataentity.BuddyGroup group) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List<ua.snuk182.asia.core.dataentity.Buddy> _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((group!=null)) {
_data.writeInt(1);
group.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_getBuddiesFromGroup, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArrayList(ua.snuk182.asia.core.dataentity.Buddy.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void requestBuddyShortInfo(byte serviceId, java.lang.String uid) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeString(uid);
mRemote.transact(Stub.TRANSACTION_requestBuddyShortInfo, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void requestBuddyFullInfo(byte serviceId, java.lang.String uid) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeString(uid);
mRemote.transact(Stub.TRANSACTION_requestBuddyFullInfo, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void requestAuthorization(ua.snuk182.asia.core.dataentity.Buddy buddy, java.lang.String reason) throws android.os.RemoteException
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
_data.writeString(reason);
mRemote.transact(Stub.TRANSACTION_requestAuthorization, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void respondAuthorization(ua.snuk182.asia.core.dataentity.Buddy buddy, boolean authorized) throws android.os.RemoteException
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
_data.writeInt(((authorized)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_respondAuthorization, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void searchUsersByUid(byte serviceId, java.lang.String buddyUid) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeString(buddyUid);
mRemote.transact(Stub.TRANSACTION_searchUsersByUid, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void respondFileMessage(ua.snuk182.asia.core.dataentity.FileMessage msg, boolean accept) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((msg!=null)) {
_data.writeInt(1);
msg.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeInt(((accept)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_respondFileMessage, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void sendFile(android.os.Bundle bu, ua.snuk182.asia.core.dataentity.Buddy buddy) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((bu!=null)) {
_data.writeInt(1);
bu.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
if ((buddy!=null)) {
_data.writeInt(1);
buddy.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_sendFile, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void cancelFileTransfer(byte serviceId, long messageId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeLong(messageId);
mRemote.transact(Stub.TRANSACTION_cancelFileTransfer, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public java.util.List<ua.snuk182.asia.core.dataentity.ServiceMessage> getServiceMessages(byte serviceId, java.lang.String uid) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List<ua.snuk182.asia.core.dataentity.ServiceMessage> _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeString(uid);
mRemote.transact(Stub.TRANSACTION_getServiceMessages, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArrayList(ua.snuk182.asia.core.dataentity.ServiceMessage.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void setStatus(byte serviceId, byte status) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeByte(status);
mRemote.transact(Stub.TRANSACTION_setStatus, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void setXStatus(ua.snuk182.asia.core.dataentity.AccountView acccount) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((acccount!=null)) {
_data.writeInt(1);
acccount.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_setXStatus, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void prepareExit() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_prepareExit, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
//void log(String log);

public void sendTyping(byte serviceId, java.lang.String buddyUid) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeString(buddyUid);
mRemote.transact(Stub.TRANSACTION_sendTyping, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void editBuddyVisibility(ua.snuk182.asia.core.dataentity.Buddy buddy) throws android.os.RemoteException
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
mRemote.transact(Stub.TRANSACTION_editBuddyVisibility, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void editBuddy(ua.snuk182.asia.core.dataentity.Buddy buddy) throws android.os.RemoteException
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
mRemote.transact(Stub.TRANSACTION_editBuddy, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void editMyVisibility(byte serviceId, byte visibility) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeByte(visibility);
mRemote.transact(Stub.TRANSACTION_editMyVisibility, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void requestAvailableChatRooms(byte serviceId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
mRemote.transact(Stub.TRANSACTION_requestAvailableChatRooms, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public byte createChat(byte serviceId, java.lang.String chatId, java.lang.String chatNickname, java.lang.String chatName, java.lang.String chatPassword) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
byte _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeString(chatId);
_data.writeString(chatNickname);
_data.writeString(chatName);
_data.writeString(chatPassword);
mRemote.transact(Stub.TRANSACTION_createChat, _data, _reply, 0);
_reply.readException();
_result = _reply.readByte();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public byte joinExistingChat(byte serviceId, java.lang.String chatId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
byte _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeString(chatId);
mRemote.transact(Stub.TRANSACTION_joinExistingChat, _data, _reply, 0);
_reply.readException();
_result = _reply.readByte();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public byte leaveChat(byte serviceId, java.lang.String chatId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
byte _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeString(chatId);
mRemote.transact(Stub.TRANSACTION_leaveChat, _data, _reply, 0);
_reply.readException();
_result = _reply.readByte();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public byte joinChat(byte serviceId, java.lang.String chatId, java.lang.String chatNickname, java.lang.String chatPassword) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
byte _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeString(chatId);
_data.writeString(chatNickname);
_data.writeString(chatPassword);
mRemote.transact(Stub.TRANSACTION_joinChat, _data, _reply, 0);
_reply.readException();
_result = _reply.readByte();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public boolean checkGroupChatsAvailability(byte serviceId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
mRemote.transact(Stub.TRANSACTION_checkGroupChatsAvailability, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public ua.snuk182.asia.core.dataentity.MultiChatRoomOccupants getChatRoomOccupants(byte serviceId, java.lang.String chatId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
ua.snuk182.asia.core.dataentity.MultiChatRoomOccupants _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeString(chatId);
mRemote.transact(Stub.TRANSACTION_getChatRoomOccupants, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = ua.snuk182.asia.core.dataentity.MultiChatRoomOccupants.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public ua.snuk182.asia.core.dataentity.PersonalInfo getChatInfo(byte serviceId, java.lang.String chatId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
ua.snuk182.asia.core.dataentity.PersonalInfo _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByte(serviceId);
_data.writeString(chatId);
mRemote.transact(Stub.TRANSACTION_getChatInfo, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = ua.snuk182.asia.core.dataentity.PersonalInfo.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void setCurrentTabs(java.util.List<java.lang.String> tabs) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStringList(tabs);
mRemote.transact(Stub.TRANSACTION_setCurrentTabs, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_sendMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_createAccount = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_deleteAccount = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_editAccount = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_getAccounts = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_getBuddy = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_getBuddies = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_getSavedTabs = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_saveTabs = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_getAccountView = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_connect = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_isDataSetInvalid = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_connectAll = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_disconnectAll = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_disconnect = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_registerCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_setUnread = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_setServiceMessageUnread = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
static final int TRANSACTION_savePreference = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
static final int TRANSACTION_getApplicationOptions = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
static final int TRANSACTION_getProtocolServiceOptions = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
static final int TRANSACTION_saveProtocolServiceOptions = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
static final int TRANSACTION_askForXStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
static final int TRANSACTION_addBuddy = (android.os.IBinder.FIRST_CALL_TRANSACTION + 23);
static final int TRANSACTION_removeBuddy = (android.os.IBinder.FIRST_CALL_TRANSACTION + 24);
static final int TRANSACTION_renameBuddy = (android.os.IBinder.FIRST_CALL_TRANSACTION + 25);
static final int TRANSACTION_moveBuddy = (android.os.IBinder.FIRST_CALL_TRANSACTION + 26);
static final int TRANSACTION_addGroup = (android.os.IBinder.FIRST_CALL_TRANSACTION + 27);
static final int TRANSACTION_removeGroup = (android.os.IBinder.FIRST_CALL_TRANSACTION + 28);
static final int TRANSACTION_renameGroup = (android.os.IBinder.FIRST_CALL_TRANSACTION + 29);
static final int TRANSACTION_setGroupCollapsed = (android.os.IBinder.FIRST_CALL_TRANSACTION + 30);
static final int TRANSACTION_getBuddiesFromGroup = (android.os.IBinder.FIRST_CALL_TRANSACTION + 31);
static final int TRANSACTION_requestBuddyShortInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 32);
static final int TRANSACTION_requestBuddyFullInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 33);
static final int TRANSACTION_requestAuthorization = (android.os.IBinder.FIRST_CALL_TRANSACTION + 34);
static final int TRANSACTION_respondAuthorization = (android.os.IBinder.FIRST_CALL_TRANSACTION + 35);
static final int TRANSACTION_searchUsersByUid = (android.os.IBinder.FIRST_CALL_TRANSACTION + 36);
static final int TRANSACTION_respondFileMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 37);
static final int TRANSACTION_sendFile = (android.os.IBinder.FIRST_CALL_TRANSACTION + 38);
static final int TRANSACTION_cancelFileTransfer = (android.os.IBinder.FIRST_CALL_TRANSACTION + 39);
static final int TRANSACTION_getServiceMessages = (android.os.IBinder.FIRST_CALL_TRANSACTION + 40);
static final int TRANSACTION_setStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 41);
static final int TRANSACTION_setXStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 42);
static final int TRANSACTION_prepareExit = (android.os.IBinder.FIRST_CALL_TRANSACTION + 43);
static final int TRANSACTION_sendTyping = (android.os.IBinder.FIRST_CALL_TRANSACTION + 44);
static final int TRANSACTION_editBuddyVisibility = (android.os.IBinder.FIRST_CALL_TRANSACTION + 45);
static final int TRANSACTION_editBuddy = (android.os.IBinder.FIRST_CALL_TRANSACTION + 46);
static final int TRANSACTION_editMyVisibility = (android.os.IBinder.FIRST_CALL_TRANSACTION + 47);
static final int TRANSACTION_requestAvailableChatRooms = (android.os.IBinder.FIRST_CALL_TRANSACTION + 48);
static final int TRANSACTION_createChat = (android.os.IBinder.FIRST_CALL_TRANSACTION + 49);
static final int TRANSACTION_joinExistingChat = (android.os.IBinder.FIRST_CALL_TRANSACTION + 50);
static final int TRANSACTION_leaveChat = (android.os.IBinder.FIRST_CALL_TRANSACTION + 51);
static final int TRANSACTION_joinChat = (android.os.IBinder.FIRST_CALL_TRANSACTION + 52);
static final int TRANSACTION_checkGroupChatsAvailability = (android.os.IBinder.FIRST_CALL_TRANSACTION + 53);
static final int TRANSACTION_getChatRoomOccupants = (android.os.IBinder.FIRST_CALL_TRANSACTION + 54);
static final int TRANSACTION_getChatInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 55);
static final int TRANSACTION_setCurrentTabs = (android.os.IBinder.FIRST_CALL_TRANSACTION + 56);
}
public java.lang.String sendMessage(ua.snuk182.asia.core.dataentity.TextMessage message, byte serviceId) throws android.os.RemoteException;
public byte createAccount(ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException;
public void deleteAccount(ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException;
public void editAccount(ua.snuk182.asia.core.dataentity.AccountView account) throws android.os.RemoteException;
public java.util.List<ua.snuk182.asia.core.dataentity.AccountView> getAccounts(boolean disabledToo) throws android.os.RemoteException;
public ua.snuk182.asia.core.dataentity.Buddy getBuddy(byte serviceId, java.lang.String buddyProtocolUid) throws android.os.RemoteException;
public java.util.List<ua.snuk182.asia.core.dataentity.Buddy> getBuddies(byte serviceId, java.util.List<java.lang.String> buddyProtocolUid) throws android.os.RemoteException;
public java.util.List<ua.snuk182.asia.core.dataentity.TabInfo> getSavedTabs() throws android.os.RemoteException;
public void saveTabs(java.util.List<ua.snuk182.asia.core.dataentity.TabInfo> tabInfos) throws android.os.RemoteException;
public ua.snuk182.asia.core.dataentity.AccountView getAccountView(byte serviceId) throws android.os.RemoteException;
public void connect(byte serviceId) throws android.os.RemoteException;
//void setAppVisible(boolean visible);

public boolean isDataSetInvalid(byte serviceId, long lastUpdateTime) throws android.os.RemoteException;
public void connectAll() throws android.os.RemoteException;
public void disconnectAll() throws android.os.RemoteException;
public void disconnect(byte serviceId) throws android.os.RemoteException;
public void registerCallback(ua.snuk182.asia.services.IRuntimeServiceCallback callback) throws android.os.RemoteException;
public void setUnread(ua.snuk182.asia.core.dataentity.Buddy buddy, ua.snuk182.asia.core.dataentity.TextMessage message) throws android.os.RemoteException;
public void setServiceMessageUnread(byte serviceId, boolean unread, ua.snuk182.asia.core.dataentity.ServiceMessage message) throws android.os.RemoteException;
public void savePreference(java.lang.String key, java.lang.String value, byte serviceId) throws android.os.RemoteException;
public android.os.Bundle getApplicationOptions() throws android.os.RemoteException;
public android.os.Bundle getProtocolServiceOptions(byte serviceId) throws android.os.RemoteException;
public void saveProtocolServiceOptions(byte serviceId, android.os.Bundle options) throws android.os.RemoteException;
public void askForXStatus(ua.snuk182.asia.core.dataentity.Buddy buddy) throws android.os.RemoteException;
public void addBuddy(ua.snuk182.asia.core.dataentity.Buddy buddy) throws android.os.RemoteException;
public void removeBuddy(ua.snuk182.asia.core.dataentity.Buddy buddy) throws android.os.RemoteException;
public void renameBuddy(ua.snuk182.asia.core.dataentity.Buddy buddy) throws android.os.RemoteException;
public void moveBuddy(ua.snuk182.asia.core.dataentity.Buddy buddy, ua.snuk182.asia.core.dataentity.BuddyGroup oldGroup, ua.snuk182.asia.core.dataentity.BuddyGroup newGroup) throws android.os.RemoteException;
public void addGroup(ua.snuk182.asia.core.dataentity.BuddyGroup group) throws android.os.RemoteException;
public void removeGroup(ua.snuk182.asia.core.dataentity.BuddyGroup group, java.util.List<ua.snuk182.asia.core.dataentity.Buddy> buddy, ua.snuk182.asia.core.dataentity.BuddyGroup newGroupForBuddies) throws android.os.RemoteException;
public void renameGroup(ua.snuk182.asia.core.dataentity.BuddyGroup group) throws android.os.RemoteException;
public void setGroupCollapsed(byte serviceId, int groupId, boolean collapsed) throws android.os.RemoteException;
public java.util.List<ua.snuk182.asia.core.dataentity.Buddy> getBuddiesFromGroup(ua.snuk182.asia.core.dataentity.BuddyGroup group) throws android.os.RemoteException;
public void requestBuddyShortInfo(byte serviceId, java.lang.String uid) throws android.os.RemoteException;
public void requestBuddyFullInfo(byte serviceId, java.lang.String uid) throws android.os.RemoteException;
public void requestAuthorization(ua.snuk182.asia.core.dataentity.Buddy buddy, java.lang.String reason) throws android.os.RemoteException;
public void respondAuthorization(ua.snuk182.asia.core.dataentity.Buddy buddy, boolean authorized) throws android.os.RemoteException;
public void searchUsersByUid(byte serviceId, java.lang.String buddyUid) throws android.os.RemoteException;
public void respondFileMessage(ua.snuk182.asia.core.dataentity.FileMessage msg, boolean accept) throws android.os.RemoteException;
public void sendFile(android.os.Bundle bu, ua.snuk182.asia.core.dataentity.Buddy buddy) throws android.os.RemoteException;
public void cancelFileTransfer(byte serviceId, long messageId) throws android.os.RemoteException;
public java.util.List<ua.snuk182.asia.core.dataentity.ServiceMessage> getServiceMessages(byte serviceId, java.lang.String uid) throws android.os.RemoteException;
public void setStatus(byte serviceId, byte status) throws android.os.RemoteException;
public void setXStatus(ua.snuk182.asia.core.dataentity.AccountView acccount) throws android.os.RemoteException;
public void prepareExit() throws android.os.RemoteException;
//void log(String log);

public void sendTyping(byte serviceId, java.lang.String buddyUid) throws android.os.RemoteException;
public void editBuddyVisibility(ua.snuk182.asia.core.dataentity.Buddy buddy) throws android.os.RemoteException;
public void editBuddy(ua.snuk182.asia.core.dataentity.Buddy buddy) throws android.os.RemoteException;
public void editMyVisibility(byte serviceId, byte visibility) throws android.os.RemoteException;
public void requestAvailableChatRooms(byte serviceId) throws android.os.RemoteException;
public byte createChat(byte serviceId, java.lang.String chatId, java.lang.String chatNickname, java.lang.String chatName, java.lang.String chatPassword) throws android.os.RemoteException;
public byte joinExistingChat(byte serviceId, java.lang.String chatId) throws android.os.RemoteException;
public byte leaveChat(byte serviceId, java.lang.String chatId) throws android.os.RemoteException;
public byte joinChat(byte serviceId, java.lang.String chatId, java.lang.String chatNickname, java.lang.String chatPassword) throws android.os.RemoteException;
public boolean checkGroupChatsAvailability(byte serviceId) throws android.os.RemoteException;
public ua.snuk182.asia.core.dataentity.MultiChatRoomOccupants getChatRoomOccupants(byte serviceId, java.lang.String chatId) throws android.os.RemoteException;
public ua.snuk182.asia.core.dataentity.PersonalInfo getChatInfo(byte serviceId, java.lang.String chatId) throws android.os.RemoteException;
public void setCurrentTabs(java.util.List<java.lang.String> tabs) throws android.os.RemoteException;
}
