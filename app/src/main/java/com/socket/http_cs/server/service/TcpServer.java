package com.socket.http_server_client.server.service;

import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class TcpServer {
    private final String TAG = "LongTcpServer";
    protected  String serverIp;
    protected  int mPort;
    protected  ExecutorService mExecutorService;
    protected  ServerSocket serverSocket;
    protected static volatile boolean runServer = true;
    protected String httpServerClassPath;
    protected String handleClientClassPath;

    public Handler handler;

    public TcpServer(){

    }

    public TcpServer(String serverIp, int port, String httpServerClassPath, String handleClientClassPath) {
        this.serverIp = serverIp;
        mPort = port;
        this.httpServerClassPath =  httpServerClassPath;
        this.handleClientClassPath =  handleClientClassPath;

        mExecutorService = Executors.newFixedThreadPool(5);
        try {
            serverSocket = new ServerSocket(mPort);
            Log.i(TAG, "服务器启动成功");
        } catch (IOException e) {
            Log.e(TAG, "服务器启动失败" + e.getMessage());
            Log.e(TAG, "服务器启动失败" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void stop() throws IOException {
        runServer = false;
        mExecutorService.shutdown();
        serverSocket.close();
    }

    public void start(){
        mExecutorService.execute(new ConnWatchDog());
    }

    public class ConnWatchDog implements Runnable {
        @Override
        public void run(){
            while (runServer) {
                try {
                    Socket client = serverSocket.accept();
                    client.setKeepAlive(true);
                    Log.i(TAG, "有客户端请求链接");
                    Class c = Class.forName(httpServerClassPath);
                    Class clz = Class.forName(handleClientClassPath);
                    Object handleClient = clz.getDeclaredConstructors()[0].newInstance(c.newInstance());
                    Method iniClientSocket = clz.getDeclaredMethod("initClientSocket",Socket.class);
                    iniClientSocket.invoke(handleClient,client);
                    mExecutorService.submit((Runnable) handleClient);
                } catch (IOException e) {
                    Log.e(TAG, "有客户端链接失败" + e.getMessage());
                }  catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static abstract class  HandleClient implements Runnable {
        @Override
        public abstract void run();

        public abstract void initClientSocket(Socket s);

        public abstract void closeClientSocket();
    }

}


