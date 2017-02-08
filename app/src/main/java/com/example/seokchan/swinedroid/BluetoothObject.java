package com.example.seokchan.swinedroid;

/**
 * Created by SeokChan on 2017-02-05.
 */

public class BluetoothObject {
    private String name;
    private String address;
    private String rssi;


    public BluetoothObject(String name, String address, String rssi) {
        this.name = name;
        this.address = address;
        this.rssi = rssi;
    }

    // Getters
    public String getName(){return name;}
    public String getAddress(){return address;}
    public String getRssi(){return rssi;}

}
