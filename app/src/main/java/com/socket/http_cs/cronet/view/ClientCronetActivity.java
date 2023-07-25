package com.socket.http_cs.cronet.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.socket.http_server_client.R;
import com.socket.http_server_client.cronet.service.ChromiumLibraryLoader;
import com.socket.http_server_client.utils.NetUtils;

import org.chromium.net.CronetEngine;
import org.chromium.net.CronetException;
import org.chromium.net.HostResolver;
import org.chromium.net.UrlRequest;
import org.chromium.net.UrlResponseInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.List;

public class ClientCronetActivity extends AppCompatActivity {

    private String TAG = "CLIENTCRONET";
    private String localIp;
    private String serverIp;
    private int serverPort;
    private EditText txtSndMsg;
    private TextView txtRcvMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_cronet);

        localIp = "127.0.0.1";
        serverIp = "127.0.0.1";
        serverPort = 8888;

        TextView txtServerIp = findViewById(R.id.server_ip);
        TextView txtServerPort = findViewById(R.id.server_port);
        txtSndMsg = findViewById(R.id.sndMsg);
        txtRcvMsg = findViewById(R.id.rcvMsg);
        txtRcvMsg.setMovementMethod(ScrollingMovementMethod.getInstance());

        TextView txtlocalIp = findViewById(R.id.localip);
        try {
            localIp = NetUtils.getInnetIp();
            txtlocalIp.setText(localIp);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        CronetEngine.Builder myCronetBuilder = new CronetEngine.Builder(this);
        myCronetBuilder.setStoragePath(getCacheDir().getAbsolutePath())
                .setHostResolver(new HostResolver() {
                    @Override
                    public List<InetAddress> resolve(String s) throws UnknownHostException {
                        return null;
                    }
                })
                .setLibraryLoader(new ChromiumLibraryLoader(this))
                .enableHttp2(true)
                .enableQuic(true);

        Button btnConnServer = findViewById(R.id.btnConnServer);
        btnConnServer.setOnClickListener(v -> {
            if (TextUtils.isEmpty(txtServerIp.getText()) || TextUtils.isEmpty(txtServerPort.getText()) || TextUtils.isEmpty(txtSndMsg.getText())) {
                Toast.makeText(ClientCronetActivity.this, "请键入正确的Ip,端口号和消息", Toast.LENGTH_LONG).show();
            } else {
                serverIp = txtServerIp.getText().toString();
                serverPort = Integer.parseInt(txtServerPort.getText().toString());
                String sndMsg = txtSndMsg.getText().toString();
            }
        });

    }


    class SimpleUrlRequestCallback extends UrlRequest.Callback {
        private ByteArrayOutputStream mBytesReceived = new ByteArrayOutputStream();
        private WritableByteChannel mReceiveChannel = Channels.newChannel(mBytesReceived);

        @Override
        public void onRedirectReceived(
                UrlRequest request, UrlResponseInfo info, String newLocationUrl) {
            Log.i(TAG, "****** onRedirectReceived ******");
            request.followRedirect();
        }

        @Override
        public void onResponseStarted(UrlRequest request, UrlResponseInfo info) {
            Log.i(TAG, "****** Response Started ******");
            Log.i(TAG, "*** Headers Are *** " + info.getAllHeaders());

            request.read(ByteBuffer.allocateDirect(32 * 1024));
        }

        @Override
        public void onReadCompleted(
                UrlRequest request, UrlResponseInfo info, ByteBuffer byteBuffer) {
            byteBuffer.flip();
            Log.i(TAG, "****** onReadCompleted ******" + byteBuffer);

            try {
                mReceiveChannel.write(byteBuffer);
            } catch (IOException e) {
                Log.i(TAG, "IOException during ByteBuffer read. Details: ", e);
            }
            byteBuffer.clear();
            request.read(byteBuffer);
        }

        @Override
        public void onSucceeded(UrlRequest request, UrlResponseInfo info) {
            Log.i(TAG, "****** Request Completed, status code is " + info.getHttpStatusCode()
                    + ", total received bytes is " + info.getReceivedByteCount());

            final String receivedData = mBytesReceived.toString();
            final String url = info.getUrl();
            final String text = "Completed " + url + " (" + info.getHttpStatusCode() + ")\n";
            ClientCronetActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtRcvMsg.setText(text + receivedData);
                }
            });
        }

        @Override
        public void onFailed(UrlRequest request, UrlResponseInfo info, CronetException error) {
            Log.i(TAG, "****** onFailed, error is: " + error.getMessage());
            final String url = serverIp;
            final String text = "Failed " + url + " (" + error.getMessage() + ")\n";
            ClientCronetActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtRcvMsg.setText(text);
                }
            });
        }
    }

}
