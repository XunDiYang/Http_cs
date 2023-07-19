package com.socket.http_cs.client.callback;

import com.socket.http_cs.model.CMessage;

/**
 * 读数据回调
 */
public interface ReadingCallback {
    void onData(CMessage data);
}

