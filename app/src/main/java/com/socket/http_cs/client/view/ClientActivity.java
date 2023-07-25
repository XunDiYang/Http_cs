package com.socket.http_cs.client.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.socket.http_cs.R;
import com.socket.http_cs.client.callback.ErrorCallback;
import com.socket.http_cs.client.callback.ReadingCallback;
import com.socket.http_cs.client.callback.WritingCallback;
import com.socket.http_cs.client.service.LongLiveSocket;
import com.socket.http_cs.model.CMessage;
import com.socket.http_cs.model.MsgType;

public class ClientActivity extends AppCompatActivity {
    private String TAG = "CLIENT";
    private String localIp;
    private String serverIp;
    private int serverPort;
    private EditText txtSndMsg;
    private TextView txtRcvMsg;
    private LongLiveSocket clientThread;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        Intent intent = getIntent();
        localIp = intent.getStringExtra("localIp");
        serverIp = intent.getStringExtra("serverIp");
        serverPort = intent.getIntExtra("serverPort", 8888);

        TextView txtlocalIp = findViewById(R.id.localip);
        txtlocalIp.setText(ipPortToString());

        clientThread = new LongLiveSocket(localIp,
                serverIp, serverPort,
                readingCallback,
                errorCallback);
        clientThread.start();


        txtSndMsg = findViewById(R.id.sndMsg);
        txtRcvMsg = findViewById(R.id.rcvMsg);
        txtRcvMsg.setMovementMethod(ScrollingMovementMethod.getInstance());
        Button btnSndMsg = findViewById(R.id.btnSndMsg);

        btnSndMsg.setOnClickListener(v -> {
            String sndMsg = txtSndMsg.getText().toString();
            if (TextUtils.isEmpty(sndMsg)) {
                return;
            }
            CMessage cMessage = new CMessage(localIp, serverIp, 200, MsgType.TEXT, sndMsg);
            WritingCallback writingCallback = new WritingCallback() {
                @Override
                public void onSuccess() {
                    Log.i(TAG, "onSuccess: 发送成功");
                }

                @Override
                public void onFail(Object cMsg) {
                    Log.w(TAG, "onFail: fail to write: " + ((CMessage)cMsg).toJsonStr());
                }
            };
            clientThread.write(cMessage, writingCallback);
        });
    }

    @Override
    public void finish() {
        clientThread.interrupt();
        super.finish();
    }

    private ReadingCallback readingCallback = (cMsg) -> {
        txtSndMsg.setText("");
        if(cMsg.getCode() == 100){
            Toast.makeText(ClientActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
        }
        else if (cMsg.getCode() == 200) {
            Log.i(TAG, "EchoClient: received: " + cMsg.toString());
            Toast.makeText(ClientActivity.this, "收到回复", Toast.LENGTH_SHORT).show();
            if (cMsg.getType() == MsgType.TEXT && !cMsg.getMsg().isEmpty()) {
                String txt = "服务器反馈：" + cMsg.getMsg() + "\n" + txtRcvMsg.getText().toString();
                txtRcvMsg.setText(txt);
            }
        } else {
            Log.i(TAG, "EchoClient: received: " + cMsg.toString());
            Toast.makeText(ClientActivity.this, "服务器端错误", Toast.LENGTH_SHORT).show();
        }
    };

    private ErrorCallback errorCallback = ()->{
        Toast.makeText(ClientActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
        finish();
    };

    public String ipPortToString() {
        return
                "client ip='" + localIp + '\'' +
                        "\nserver ip='" + serverIp + '\'' +
                        ", port=" + serverPort;
    }
}
