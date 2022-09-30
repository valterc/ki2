package com.valterc.ki2.services;

import android.os.RemoteException;

public interface UnsafeBroadcastInvoker<TCallback, TData> {

    void invoke(TCallback callback, TData data) throws RemoteException;

}
