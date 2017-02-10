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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

public class DeviceListActivity2 extends FragmentActivity implements FragmentOne.DataPassListener {
    private Fragment fragment = null;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_main);
        fragment = new FragmentOne();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.output, fragment);
        transaction.commit();

    }

    // Backpress button go back to the start page.
    @Override
    public void onBackPressed()
    {
        AlertDialog diaBox = AskOption();
        diaBox.show();
    }

    // AlerDialog asking for going back to the start page.
    // Todo: Need to change background color and title color of AlertDialog....

    private AlertDialog AskOption()
    {
        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Are you sure you want to exit?")


                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        return myQuittingDialogBox;

    }

    @Override
    public void passData(String data) {
        FragmentTwo fragment2 = new FragmentTwo();
        Bundle args = new Bundle();
        args.putString(FragmentTwo.DATA_RECEIVE, data);
        fragment2.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.output, fragment2)
                .commit();
    }
}

