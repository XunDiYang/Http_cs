package com.socket.http_cs.server.service;


import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpServer {
    private final String TAG = "TcpServer";
    protected final String serverIp;
    protected final int serverPort;
    protected final ExecutorService mExecutorService;
    protected ServerSocket serverSocket;
    protected volatile boolean runServer = true;
    protected String handleClientClassPath;

    public TcpServer(String serverIp, int port,String handleClientClassPath) {
        this.serverIp = serverIp;
        serverPort = port;
        this.handleClientClassPath = handleClientClassPath;
        mExecutorService = Executors.newFixedThreadPool(5);
        try {
            serverSocket = new ServerSocket(serverPort);
            Log.e(TAG, "服务器启动成功");
        } catch (IOException e) {
            Log.e(TAG, "服务器启动失败" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void start() {
        mExecutorService.execute(new ConnWatchDog());
    }

    public void stop() throws IOException {
        runServer = false;
        mExecutorService.shutdown();
        serverSocket.close();
    }

    class ConnWatchDog implements Runnable {
        @Override
        public void run() {
            while (runServer) {
                try {
                    Socket client = serverSocket.accept();
                    client.setKeepAlive(true);
                    Log.d(TAG, "有客户端请求链接");
                    Class clz = Class.forName(handleClientClassPath);
                    Constructor handleClientConstructor = clz.getConstructor();
                    Object handleClient = handleClientConstructor.newInstance();
                    Method iniMethod = clz.getMethod("init",Socket.class);
                    iniMethod.invoke(handleClient,client);
                    mExecutorService.submit((Runnable) handleClient);
                } catch (IOException e) {
                    Log.e(TAG, "有客户端链接失败" + e.getMessage());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public interface HandleClient extends Runnable {
        public void init(Socket s);
        @Override
        public void run();
        public void closeClientSocket();
    }
}


