/*
 *   Author: Seokchan Yoo
 *   Date: 2017-02-11
 *   Description: Main Sensor screen after opening up connection to the UART capable BLE device.
 *   Note: Need to add graph feature here to plot the data upon receiving RX data from the sensor.
 */

package com.example.seokchan.swinedroid;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SensorActivity extends Activity {

    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;

    // UI elements
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    private Button btnSend, btnClear, btnMeasure, btnShare;
    private EditText edtMessage;
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private String mDeviceName;
    private String mDeviceAddress;

    // Create string array to save data received and save that received data into csv file
    private ArrayList<String> saved_data = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uart_main);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        messageListView = (ListView) findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);
        btnSend = (Button) findViewById(R.id.sendButton);
        btnClear = (Button) findViewById(R.id.onclick_clear);
        btnShare = (Button) findViewById(R.id.onclick_share);
        btnMeasure = (Button) findViewById(R.id.onclick_measure);
        edtMessage = (EditText) findViewById(R.id.sendText);
        service_init();
        init_UI();
        Log.d(TAG, "Oncreate Finished");
    }

    private void init_UI() {

        // Handler Send button
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.sendText);
                String message = editText.getText().toString();
                byte[] value;
                try {
                    //send data to service
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("[" + currentDateTimeString + "] TX: " + message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });

        // Clear the message list (This feature should be removed when graph is added.)
        // Not working properly....
        btnClear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showMessage("Text screen is cleared!");
                listAdapter.clear();
                listAdapter.notifyDataSetChanged();
            }
        });

        // Measure button that sends character "A" to the sensor...
        btnMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = "A";
                byte[] value;
                try {
                    //send data to service
                    value = msg.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("[" + currentDateTimeString + "] TX: " + msg);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });

        // Dummy button that sends character "B" to the sensor...
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = "B";
                byte[] value;
                try {
                    //send data to service
                    value = msg.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("[" + currentDateTimeString + "] TX: " + msg);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
    }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            Log.d(TAG, "Connected to: " + mDeviceAddress);
            mService.connect(mDeviceAddress);

        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
        }
    };
    private Handler mHandler = new Handler() {
        @Override

        //Handler events that received from UART service
        public void handleMessage(Message msg) {

        }
    };


    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            //*******Service Connected**************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        //btnConnectDisconnect.setText("Disconnect");
                        edtMessage.setEnabled(true);
                        btnSend.setEnabled(true);
                        // ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - ready");
                        //listAdapter.add("["+currentDateTimeString+"] Connected to: "+ mDevice.getName());
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        mState = UART_PROFILE_CONNECTED;
                    }
                });
            }

            //*********Service Disconnected************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        //btnConnectDisconnect.setText("Connect");
                        edtMessage.setEnabled(false);
                        btnSend.setEnabled(false);
                        //listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        //setUiState();

                    }
                });
            }


            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String text = new String(txValue, "UTF-8");
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());

                            // save received data into string array and logcat for debugging..
                            //saved_data.add(text);
                            //Log.d("wawa", text);

                            listAdapter.add("[" + currentDateTimeString + "] RX: " + text);
                            messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);

                            /*
                            if (text == "End") {
                                //save_csv();
                            }
                            */

                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }
            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }
        }
    };


    // Message Handler
    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService = null;

    }


    // OnCreate, called once to initialize the activity.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    //((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");

                    mService.connect(deviceAddress);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }


    /*
    public void save_csv() {
        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
        String filename = "Measurement" + currentDateTimeString;

        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("Data", saved_data);
            return jsonObj.toString();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return null;

    }
    */
}