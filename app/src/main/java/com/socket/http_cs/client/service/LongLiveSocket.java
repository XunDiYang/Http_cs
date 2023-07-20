package com.socket.http_cs.client.service;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;

import com.socket.http_cs.client.callback.ErrorCallback;
import com.socket.http_cs.client.callback.ReadingCallback;
import com.socket.http_cs.client.callback.WritingCallback;
import com.socket.http_cs.model.CMessage;
import com.socket.http_cs.model.MsgType;

import org.json.JSONException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public final class LongLiveSocket extends Thread {
    private static final String TAG = "LongLiveSocket";
    private final String localIp;
    private final String serverIp;
    private final int serverPort;
    private final ReadingCallback mReadingCallback;
    private final ErrorCallback mErrorCallback;
    private final Handler mUIHandler = new Handler(Looper.getMainLooper());
    private HandlerThread mWriterThread;
    private Handler mWriterHandler;
    private Thread mReaderThread;
    private static Socket mSocket;

    private volatile boolean running = false; //连接状态
    private volatile boolean runningWrite = false; //写状态

    public LongLiveSocket(String localIp, String host, int port,
                          ReadingCallback readingCallback, ErrorCallback errorCallback) {
        this.localIp = localIp;
        serverIp = host;
        serverPort = port;
        mReadingCallback = readingCallback;
        mErrorCallback = errorCallback;
    }

    @Override
    public void run() {
        iniSocket();
    }

    private void iniSocket() {
        if (running) return;
        try {
            mSocket = new Socket(serverIp, serverPort);
            mSocket.setKeepAlive(true);
            running = true;
            runningWrite = true;
            mWriterThread = new HandlerThread("socket-writer");
            mWriterThread.start();
            mWriterHandler = new Handler(mWriterThread.getLooper());
            mReaderThread = new Thread(new ReaderTask(mSocket), "socket-reader");
            mReaderThread.start();
            mUIHandler.post(() -> mReadingCallback.onData(new CMessage("", "", 100, MsgType.CONNECT, "")));
        } catch (IOException e) {
            Log.e(TAG, "initSocket: ", e);
            mUIHandler.post(() -> mErrorCallback.onError());
        }
    }

    @Override
    public void interrupt() {
        try {
            if (mSocket != null) {
                sendEndPkg();
                if (mWriterThread != null) {
                    mWriterThread.interrupt();
                }
                if (mReaderThread != null) {
                    mReaderThread.interrupt();
                }
                mSocket.close();
                Log.i(TAG, "关闭客户端的socket");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        super.interrupt();
    }

    public void sendEndPkg() {
        if (runningWrite) {
            if (running) {
                int SDK_INT = android.os.Build.VERSION.SDK_INT;
                if (SDK_INT > 8) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    //your codes here
                    WriterTask endPkg = new WriterTask(new CMessage(localIp, serverIp, 400, MsgType.CONNECT, ""), new WritingCallback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onFail(Object cMessage) {
                        }
                    });
                    endPkg.run();
                }
            }
            runningWrite = false;
        }
    }

    public void stopServerSocket() {
        if (running) {
            sendEndPkg();
            running = false;
        }
        mUIHandler.post(() -> mErrorCallback.onError());
    }

    public void write(Object cMessage, WritingCallback callback) {
        mWriterHandler.post(new WriterTask(cMessage, callback));
    }

    private class WriterTask implements Runnable {
        private Object cMsg;
        private WritingCallback callback;

        public WriterTask(Object cmsg, WritingCallback callback) {
            this.cMsg = cmsg;
            this.callback = callback;
        }

        @Override
        public void run() {
            if (!runningWrite) {
                return;
            }
            if (!running) {
                iniSocket();
            }
            try {
                OutputStream outToServer = mSocket.getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                if (cMsg != null) {
                    out.writeUTF(((CMessage) cMsg).getFrom());
                    out.writeUTF(((CMessage) cMsg).getTo());
                    out.writeInt(((CMessage) cMsg).getCode());
                    out.writeInt(((CMessage) cMsg).getType());
                    out.writeUTF(((CMessage) cMsg).getMsg());
                    out.flush();
                    Log.i(TAG, "发送：\t" + ((CMessage) cMsg).toJsonStr());
                }
                callback.onSuccess();
            } catch (IOException e) {
                Log.e(TAG, "write: ", e);
                runningWrite = false;  //服务器端断开了
                callback.onFail(cMsg);
                stopServerSocket();
            }
        }
    }

    private class ReaderTask implements Runnable {

        private final Socket mSocket;

        public ReaderTask(Socket socket) {
            mSocket = socket;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    readResponse();
                } catch (IOException | JSONException | ClassNotFoundException |
                         InterruptedException e) {
                    Log.e(TAG, "ReaderTask#run: ", e);
                    stopServerSocket();
                }
            }
        }

        private void readResponse() throws IOException, JSONException, ClassNotFoundException, InterruptedException {

            while (running) {
                InputStream inputStream = mSocket.getInputStream();
                DataInputStream in = new DataInputStream(inputStream);

                if (in.available() > 0) {
                    Log.i(TAG, "收到消息");
                    CMessage cMessage = new CMessage();
                    cMessage.setFrom(in.readUTF());
                    cMessage.setTo(in.readUTF());
                    cMessage.setCode(in.readInt());
                    cMessage.setType(in.readInt());
                    cMessage.setMsg(in.readUTF());
                    cMessage.setMsg("来自" + cMessage.getFrom() + "客户端: " + cMessage.getMsg());
                    if (cMessage.getCode() != 200 || cMessage.getType() == MsgType.TEXT) {
                        mUIHandler.post(() -> {
                            mReadingCallback.onData(cMessage);
                        });
                    }
                } else {
                    Thread.sleep(10);
                }
            }
        }
    }
}
