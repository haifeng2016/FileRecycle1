/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\AndroidWorkSpace\\FileRecycle\\app\\src\\main\\aidl\\com\\samsung\\android\\recoveryfile\\modelfileworker\\IFileWorkerListener.aidl
 */
package com.samsung.android.recoveryfile.modelfileworker;
// Declare any non-default types here with import statements

public interface IFileWorkerListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.samsung.android.recoveryfile.modelfileworker.IFileWorkerListener
{
private static final java.lang.String DESCRIPTOR = "com.samsung.android.recoveryfile.modelfileworker.IFileWorkerListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.samsung.android.recoveryfile.modelfileworker.IFileWorkerListener interface,
 * generating a proxy if needed.
 */
public static com.samsung.android.recoveryfile.modelfileworker.IFileWorkerListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.samsung.android.recoveryfile.modelfileworker.IFileWorkerListener))) {
return ((com.samsung.android.recoveryfile.modelfileworker.IFileWorkerListener)iin);
}
return new com.samsung.android.recoveryfile.modelfileworker.IFileWorkerListener.Stub.Proxy(obj);
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
case TRANSACTION_onFileOpened:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.onFileOpened(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onFileBackuped:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.onFileBackuped(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.samsung.android.recoveryfile.modelfileworker.IFileWorkerListener
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
@Override public void onFileOpened(java.lang.String name) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(name);
mRemote.transact(Stub.TRANSACTION_onFileOpened, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onFileBackuped(java.lang.String name) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(name);
mRemote.transact(Stub.TRANSACTION_onFileBackuped, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onFileOpened = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onFileBackuped = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
/**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
public void onFileOpened(java.lang.String name) throws android.os.RemoteException;
public void onFileBackuped(java.lang.String name) throws android.os.RemoteException;
}
