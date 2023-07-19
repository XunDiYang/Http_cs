package com.socket.http_cs.server.callback;

import com.socket.http_cs.model.CMessage;

public interface Callback<T> {
    //    void onEvent( int code, String msg, T t);
    void onEvent(CMessage cMessage, T t);
}
