package com.example.seokchan.swinedroid;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {

    // Button declaration
    private Button btn_scan;
    private TextView status_txt;
    private String TAG;

    // Bluetooth related declaration
    //private String BT_TAG;
    private BluetoothAdapter BTadapter;


    // Begin onCreate
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //BT_TAG = getResources().getString(R.string.app_name);
        BTadapter =BluetoothAdapter.getDefaultAdapter();
        status_txt = (TextView)findViewById(R.id.txt_result);

        btn_scan = (Button)findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(new View.OnClickListener(){
            @Override
                    public void onClick(View v){
                            scanForBTdevice();
            }
        });

    } // End of onCreate

    // Method for requesting BT turn ON
    private void turnOnBT(){
        Intent intent = new Intent(BTadapter.ACTION_REQUEST_ENABLE);
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        startActivityForResult(intent, 1);
    }

    // Activity result check to turn off the app if user does not turn on BT
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED){
            Toast.makeText(getApplicationContext(), "Bluetooth must be enabled to continue", Toast.LENGTH_SHORT).show();
            finish();
        }
        else if(resultCode == RESULT_OK){
            Toast.makeText(getApplicationContext(), "Bluetooth is ON now", Toast.LENGTH_SHORT).show();
            status_txt.setText("ON");
            status_txt.setTextColor(Color.parseColor("#5EEB5B"));
        }
    }

    // Monitor Bluetooth status change using Broadcast Receiver
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        status_txt.setText("ON");
                        status_txt.setTextColor(Color.parseColor("#5EEB5B"));
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        status_txt.setText("OFF");
                        status_txt.setTextColor(Color.parseColor("#B9314F"));
                }
            }
        }
    };

    // Scanning BT Device Intent to open up list view activity
    private void scanForBTdevice(){
        // Start this on a new listview activity without passing any data
        Intent intent = new Intent(this, DeviceListActivity.class);
        startActivity(intent);
    }

    @Override
    public void onPause(){
        super.onPause();

    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d("wawa","onResume()");
        if(BTadapter==null){
            Toast.makeText(getApplicationContext(), "No bluetooth detected", Toast.LENGTH_SHORT).show();
            finish();
        }
        else{
            if(!BTadapter.isEnabled()){
                Log.d("wawa","not on");
                Toast.makeText(getApplicationContext(), "Your Bluetooth is OFF", Toast.LENGTH_SHORT).show();
                turnOnBT();
            }
            else if(BTadapter.isEnabled()){
                Log.d("wawa","enabled");
                status_txt.setText("ON");
                status_txt.setTextColor(Color.parseColor("#5EEB5B"));
            }
        }
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

    }

    @Override
    public void onDestroy(){
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

} // End of Main Activity
