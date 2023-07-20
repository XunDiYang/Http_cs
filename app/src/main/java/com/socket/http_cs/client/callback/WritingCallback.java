package com.socket.http_cs.client.callback;

import com.socket.http_cs.model.CMessage;

/**
 * 写数据回调
 */
public interface WritingCallback {
    void onSuccess();

    void onFail(Object cMessage);
}
