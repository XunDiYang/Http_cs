package com.socket.http_cs.server.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.socket.http_cs.R;
import com.socket.http_cs.model.CMessage;
import com.socket.http_cs.model.MsgType;
import com.socket.http_cs.server.callback.Callback;
import com.socket.http_cs.server.service.HttpServer;
import com.socket.http_cs.server.service.TcpServer;

import java.io.IOException;

public class ServerActivity extends AppCompatActivity {
    private String serverIp;
    private int serverPort;
    private HttpServer httpServer;
    private TextView txtRcvMsg;
    private TextView txtlocalip;

    private final String TAG = "ServerActivity";
    private Callback<Void> rcvMsgCallback = new Callback<Void>() {
        @Override
        public void onEvent(CMessage cMessage, Void unused) {
            if (cMessage.getCode() == 100) {
                Toast.makeText(ServerActivity.this, "客户端接入", Toast.LENGTH_SHORT).show();
            } else if (cMessage.getCode() == 200) {
                if (cMessage.getType() == MsgType.TEXT && !cMessage.getMsg().isEmpty()) {
                    Toast.makeText(ServerActivity.this, "收到信息", Toast.LENGTH_SHORT).show();
                    String txt = "服务器已收到消息" + cMessage.getMsg() + "\n" + txtRcvMsg.getText().toString();
                    txtRcvMsg.setText(txt);
                }
            } else if (cMessage.getCode() == 400) {
                Toast.makeText(ServerActivity.this, "客户端错误或断开", Toast.LENGTH_SHORT).show();
            } else if (cMessage.getCode() == 500) {
                Toast.makeText(ServerActivity.this, "服务器端断开", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ServerActivity.this, "错误", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        Intent intent = getIntent();
        serverIp = intent.getStringExtra("serverIp");
        serverPort = intent.getIntExtra("serverPort", 8888);

        txtlocalip = findViewById(R.id.localip);
        txtlocalip.setText(toString());
        txtRcvMsg = findViewById(R.id.rcvMsg);
        txtRcvMsg.setMovementMethod(ScrollingMovementMethod.getInstance());

        httpServer = new HttpServer(serverIp,serverPort,rcvMsgCallback);
        httpServer.start();

    }

    @Override
    public void finish() {
        Log.i(TAG, "关闭服务器服务");
        try {
            httpServer.stop();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            super.finish();
        }
    }

    @NonNull
    @Override
    public String toString() {
        return
                "ip='" + serverIp + '\'' +
                        ", port=" + serverPort;
    }
}
