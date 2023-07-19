package com.socket.http_cs.server.service;

import android.os.Handler;
import android.util.Log;
import com.socket.http_cs.model.CMessage;
import com.socket.http_cs.model.MsgType;
import com.socket.http_cs.server.callback.Callback;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class HttpServer extends TcpServer{
    private final String TAG = "TcpServer";

    private final String serverIp;
    private final int mPort;
    private Callback<Void> rcvMsgCallback;
    private Handler handler;

    public HttpServer(String serverIp, int mPort, Callback<Void> rcvMsgCallback) {
        super(serverIp,mPort,"com.socket.http_cs.server.service.HttpServer.HttpHandleClient");
        this.serverIp = serverIp;
        this.mPort = mPort;
        this.rcvMsgCallback = rcvMsgCallback;
        handler = new Handler();
    }

    class HttpHandleClient implements HandleClient{
        Socket client;
        volatile boolean runHandleClient = true;
        public void init(Socket s) {
            client = s;
        }
        @Override
        public void run() {
            try {
                while (runServer && runHandleClient) {
                    InputStream in = client.getInputStream();
                    if (in.available() > 0) {
                        ObjectInputStream ois = new ObjectInputStream(in);
                        Object obj = ois.readObject();
                        Log.d(TAG, "收到消息");
                        CMessage cMessage = (CMessage) obj;
                        Object out = new CMessage(cMessage.getTo(), cMessage.getFrom(), cMessage.getCode(), cMessage.getType(), cMessage.getMsg());
                        cMessage.setMsg("来自" + cMessage.getFrom() + "客户端: " + cMessage.getMsg());
                        if (cMessage.getCode() != 200 || cMessage.getType() == MsgType.TEXT) {
                            handler.post(() -> rcvMsgCallback.onEvent(cMessage, null));
                        }
                        if (out != null) {
                            ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                            oos.writeObject(out);
                            oos.flush();
                            Log.d(TAG, "发送消息");
                        }
                    } else {
                        Thread.sleep(10);
                    }
                }
            } catch (Exception e) {
                CMessage cMessage = new CMessage();
                cMessage.setCode(400);
                handler.post(() -> rcvMsgCallback.onEvent(cMessage, null));
                Log.e(TAG, "handleClient: ", e);
                closeClientSocket();
            } finally {
                closeClientSocket();
            }
            Log.d(TAG, "结束处理数据");
        }

        @Override
        public void closeClientSocket() {
            if (runHandleClient) {
                runHandleClient = false;
            }
            if (client != null) {
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.e(TAG, "关闭：" + client.getRemoteSocketAddress());
        }
    }
}
