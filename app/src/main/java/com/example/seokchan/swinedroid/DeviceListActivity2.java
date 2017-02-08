package com.example.seokchan.swinedroid;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by SeokChan on 2017-02-05.
 */

public class DeviceListActivity2 extends Activity {

    private BluetoothAdapter BTadapter;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private static final String TAG = "DeviceListActivity";
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private ArrayList<BluetoothObject> btDevice = new ArrayList<>();
    private ArrayAdapter<BluetoothObject> adapter;



    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list);

        // Set result for the case when user click back button
        setResult(Activity.RESULT_CANCELED);

        // Initialize array adapters. One for already paired devices and

        //mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        btDevice.add(new BluetoothObject("null", "null","sample"));

        ArrayAdapter<BluetoothObject> adapter = new CustomAdapter(this, 0, btDevice);
        ListView newDevice = (ListView) findViewById(R.id.new_devices);
        newDevice.setAdapter(adapter);
        newDevice.setOnItemClickListener(mDeviceClickListener);



        BTadapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    protected  void onResume(){
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        doDiscovery();
        super.onResume();
        Log.d("wawa", "Resume");
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        BTadapter.cancelDiscovery();
        Log.d(TAG, "onPause()");
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d("wawa", "onSTop");
        this.unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        // Make sure we're not doing discovery anymore
        if (BTadapter != null) {
            BTadapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        // If we're already discovering, stop it
        if (BTadapter.isDiscovering()) {
            BTadapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        BTadapter.startDiscovery();
    }

    /**
     * The on-click listener for all devices in the ListViews
     */
    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            BTadapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);


            Intent myIntent = new Intent(v.getContext(), SensorMain.class);
            startActivityForResult(myIntent, 0);
        }
    };



    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d("wawa", "Device Found");
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Get RSSI and paste it
                int  rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                String rssi_text = String.valueOf(rssi);
                //Log.d("name", rssi_text);

                //int rssi_value = checkRSSI(rssi);


                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {

                    //btDevice.add(new BluetoothObject(device.getName(), device.getAddress(),"sample"));
                    //names.add(device.getName());
                    //Log.d("name",device.getName());
                    //images.add(getDrawableByMajorClass(rssi_value));
                    //addresses.add(device.getAddress());
                    //mNewDevicesArrayAdapter.add(device.getName());
                    //mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress() + "\n" + rssi + "dBm");
                    //mNewDevicesArrayAdapter.notifyDataSetChanged();
                }

                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                Log.d("wawa", "New Scan");
                // Update discovery result every 12 seconds.
                adapter.clear();

                BTadapter.startDiscovery();

            }
        }
    };
}
