package com.example.seokchan.swinedroid;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SeokChan on 2017-02-08.
 */

public class CustomAdapter extends ArrayAdapter<BluetoothObject> {

    private Context context;
    private List<BluetoothObject> btDevice;

    //constructor, call on creation
    public CustomAdapter(Context context, int resource, ArrayList<BluetoothObject> objects) {
        super(context, resource, objects);

        this.context = context;
        this.btDevice = objects;
    }

    //called when rendering the list
    public View getView(int position, View convertView, ViewGroup parent) {

        //get the property we are displaying
        BluetoothObject property = btDevice.get(position);

        //get the inflater and inflate the XML layout for each item
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.device_item_custom, null);

        TextView description = (TextView) view.findViewById(R.id.name_of_device);
        TextView address = (TextView) view.findViewById(R.id.device_address);
        ImageView image = (ImageView) view.findViewById(R.id.image);

        //set address and description
        String completeAddress = property.getName() + " " + property.getAddress() + ", " + property.getRssi();
        address.setText(completeAddress);

        //get the image associated with this property
        int imageID = context.getResources().getIdentifier(property.getRssi(), "drawable", context.getPackageName());
        image.setImageResource(imageID);

        return view;
    }

}
