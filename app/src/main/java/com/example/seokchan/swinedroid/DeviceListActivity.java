package com.example.seokchan.swinedroid;

import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ListFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import static android.bluetooth.BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION;

/**
 * Created by SeokChan on 2017-02-05.
 */

public class DeviceListActivity extends Activity {

    private BluetoothAdapter BTadapter;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private static final String TAG = "DeviceListActivity";
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    // For custom ArrayAdapter
    private ArrayList<String> names;
    private ArrayList<Drawable> images;
    private ArrayList<String> addresses;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list);

        // For custom ArrayAdapter...
        names = new ArrayList<String>();
        images = new ArrayList<Drawable>();
        addresses = new ArrayList<String>();

        // Set result for the case when user click back button
        setResult(Activity.RESULT_CANCELED);

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        ArrayAdapter<String> pairedDevicesArrayAdapter =
                new ArrayAdapter<String>(this, R.layout.device_name);
        //mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,0);

        CustomListView adapter = new CustomListView(this, names, images, addresses);

        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Find and set up the ListView for newly discovered devices --> Todo: Need to create second Click listener to popup pairing request
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        //newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        //newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        /* This is for Custom List view demo */
        newDevicesListView.setAdapter(adapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        BTadapter = BluetoothAdapter.getDefaultAdapter();
        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = BTadapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {

                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            pairedDevicesArrayAdapter.add(noDevices);
        }


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

    private void goGraph(){
        // Start this on a new listview activity without passing any data
        Intent intent = new Intent(this, SensorMain.class);
        startActivity(intent);
    }

    /* For getting RSSI singal strength image */

    private Drawable getDrawableByMajorClass(int major) {
        Drawable drawable = null;
            switch (major) {
                case 1:
                    drawable = ContextCompat.getDrawable(this, R.mipmap.rssi_excellent);
                    Log.d("wawa", "excellent signal");
                    break;
                case 2:
                    drawable = ContextCompat.getDrawable(this, R.mipmap.rssi_good);
                    Log.d("wawa", "good signal");
                    break;
                case 3:
                    drawable = ContextCompat.getDrawable(this, R.mipmap.rssi_weak);
                    Log.d("wawa", "weak signal");
                    break;
                case 4:
                    drawable = ContextCompat.getDrawable(this, R.mipmap.rssi_veryweak);
                    Log.d("wawa", "very weak signal");
                    break;
            }
        return drawable;
    }

    private int checkRSSI(int rssi){
        int rssi_switch = 0;
        if(rssi > -70){
            rssi_switch= 1;
        }
        else if((rssi <= -70) && (rssi > -80)){
            rssi_switch = 2;
        }
        else if((rssi <= -86) && (rssi > -100)){
            rssi_switch = 3;
        }
        else if(rssi < -100){
            rssi_switch= 4;
        }
        return rssi_switch;
}


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

                int rssi_value = checkRSSI(rssi);




                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    names.add(device.getName());
                    images.add(getDrawableByMajorClass(rssi_value));
                    addresses.add(device.getAddress());
                    //mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress() + "\n" + rssi + "dBm");
                    //mNewDevicesArrayAdapter.notifyDataSetChanged();
                }

                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                /*
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    Log.d("wawa", "Found");
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
                Log.d("wawa", "New Scan");
                // Update discovery result every 12 seconds.
                mNewDevicesArrayAdapter.clear();
                */
                BTadapter.startDiscovery();

            }
        }
    };
}
