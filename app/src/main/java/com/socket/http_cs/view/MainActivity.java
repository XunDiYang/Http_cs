package com.socket.http_cs.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import com.socket.http_cs.R;
import com.socket.http_cs.client.view.ClientStartActivity;
import com.socket.http_cs.cronet.view.ClientCronetActivity;
import com.socket.http_cs.server.view.ServerStartActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnServer = findViewById(R.id.btnServer);
        btnServer.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ServerStartActivity.class);
            startActivity(intent);
        });

        Button btnClient = findViewById(R.id.btnClient);
        btnClient.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ClientStartActivity.class);
            startActivity(intent);
        });

        Button btnCronetClient = findViewById(R.id.btnCronetClient);
        btnCronetClient.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ClientCronetActivity.class);
            startActivity(intent);
        });
    }
}
