/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\AndroidWorkSpace\\FileRecycle\\app\\src\\main\\aidl\\com\\samsung\\android\\recoveryfile\\modelfileworker\\IFileWorkerService.aidl
 */
package com.samsung.android.recoveryfile.modelfileworker;
// Declare any non-default types here with import statements

public interface IFileWorkerService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.samsung.android.recoveryfile.modelfileworker.IFileWorkerService
{
private static final java.lang.String DESCRIPTOR = "com.samsung.android.recoveryfile.modelfileworker.IFileWorkerService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.samsung.android.recoveryfile.modelfileworker.IFileWorkerService interface,
 * generating a proxy if needed.
 */
public static com.samsung.android.recoveryfile.modelfileworker.IFileWorkerService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.samsung.android.recoveryfile.modelfileworker.IFileWorkerService))) {
return ((com.samsung.android.recoveryfile.modelfileworker.IFileWorkerService)iin);
}
return new com.samsung.android.recoveryfile.modelfileworker.IFileWorkerService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
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
case TRANSACTION_start:
{
data.enforceInterface(DESCRIPTOR);
this.start();
reply.writeNoException();
return true;
}
case TRANSACTION_stop:
{
data.enforceInterface(DESCRIPTOR);
this.stop();
reply.writeNoException();
return true;
}
case TRANSACTION_available:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.available();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_openFile:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _result = this.openFile(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_backupFile:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
int _result = this.backupFile(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setFileWorkerListener:
{
data.enforceInterface(DESCRIPTOR);
com.samsung.android.recoveryfile.modelfileworker.IFileWorkerListener _arg0;
_arg0 = com.samsung.android.recoveryfile.modelfileworker.IFileWorkerListener.Stub.asInterface(data.readStrongBinder());
this.setFileWorkerListener(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.samsung.android.recoveryfile.modelfileworker.IFileWorkerService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
@Override public void start() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_start, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void stop() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_stop, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public boolean available() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_available, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int openFile(java.lang.String name) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(name);
mRemote.transact(Stub.TRANSACTION_openFile, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int backupFile(java.lang.String sname, java.lang.String dname) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(sname);
_data.writeString(dname);
mRemote.transact(Stub.TRANSACTION_backupFile, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void setFileWorkerListener(com.samsung.android.recoveryfile.modelfileworker.IFileWorkerListener cb) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((cb!=null))?(cb.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_setFileWorkerListener, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_start = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_stop = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_available = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_openFile = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_backupFile = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_setFileWorkerListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
}
/**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
public void start() throws android.os.RemoteException;
public void stop() throws android.os.RemoteException;
public boolean available() throws android.os.RemoteException;
public int openFile(java.lang.String name) throws android.os.RemoteException;
public int backupFile(java.lang.String sname, java.lang.String dname) throws android.os.RemoteException;
public void setFileWorkerListener(com.samsung.android.recoveryfile.modelfileworker.IFileWorkerListener cb) throws android.os.RemoteException;
}
