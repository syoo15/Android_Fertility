package com.example.seokchan.swinedroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;
import java.util.zip.Inflater;

/**
 * Created by SeokChan on 2017-02-09.
 */

public class FragmentOne extends Fragment{
    private BluetoothAdapter BTadapter;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private static final String TAG = "DeviceListActivity";
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    DataPassListener mCallback;
    public interface DataPassListener{
        public void passData(String address);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.device_list, viewGroup, false);
        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        // Set result for the case when user click back button
        getActivity().setResult(Activity.RESULT_CANCELED);

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        ArrayAdapter<String> pairedDevicesArrayAdapter =
                new ArrayAdapter<String>(getContext(), R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.device_name);

        // Find and set up the ListView for paired devices

        ListView pairedListView = (ListView) getView().findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);

        // Find and set up the ListView for newly discovered devices --> Todo: Need to create second Click listener to popup pairing request
        ListView newDevicesListView = (ListView) getView().findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
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
    public void onResume(){
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(mReceiver, filter);

        doDiscovery();
        super.onResume();
        Log.d("wawa", "Resume");

    }

    @Override
    public void onPause()
    {
        super.onPause();
        BTadapter.cancelDiscovery();
        Log.d(TAG, "onPause()");
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.d("wawa", "onSTop");
        // Make sure we're not doing discovery anymore
        if (BTadapter != null) {
            BTadapter.cancelDiscovery();
        }

        getActivity().unregisterReceiver(mReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        // Make sure we're not doing discovery anymore
        if (BTadapter != null) {
            BTadapter.cancelDiscovery();
        }
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
            //Intent intent = new Intent();
            //intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
            //getActivity().startActivity(intent);
            mCallback.passData(address);
            //Log.d("wawa",address);


            /*
            FragmentTwo fragment2 = new FragmentTwo();
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction =  fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.output, fragment2);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            */

            getActivity().setTitle("Main Screen");
            //startActivity(intent);

        }
    };

    private String checkRSSI(int rssi){
        String rssi_switch = "null";
        if(rssi > -70){
            rssi_switch= "Excellent";
        }
        else if((rssi <= -70) && (rssi > -80)){
            rssi_switch = "Good";
        }
        else if((rssi <= -86) && (rssi > -100)){
            rssi_switch = "Fair";
        }
        else if(rssi < -100){
            rssi_switch= "Bad";
        }
        else{
            rssi_switch = "No signal";
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
                String rssi_text = String.valueOf(rssi);
                //Log.d("name", rssi_text);

                //int rssi_value = checkRSSI(rssi);


                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    //Log.d("wawa",device.getName());
                    //mNewDevicesArrayAdapter.add("Device Name: "+ device.getName() + "\n" +"MAC Address: "  +device.getAddress() + "\n" + "Signal Strength: " +checkRSSI(rssi));
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    mNewDevicesArrayAdapter.notifyDataSetChanged();
                }

                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    Log.d("wawa", "Found");
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
                Log.d("wawa", "New Scan");
                // Update discovery result every 12 seconds.
                mNewDevicesArrayAdapter.clear();

                BTadapter.startDiscovery();

            }
        }
    };


}
