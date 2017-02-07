package com.example.seokchan.swinedroid;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by SeokChan on 2017-02-07.
 */

public class CustomListView extends ArrayAdapter<String> {

    private Activity mContext;
    private ArrayList<String> mNames;
    private ArrayList<Drawable> mImages;

    //The ArrayAdapter constructor
    public CustomListView(Activity context, ArrayList<String> names, ArrayList<Drawable> images, ArrayList<String> values) {
            super(context, R.layout.device_item_custom, values);
        //Set the value of variables
        mNames = names;
        mImages = images;
    }

    //Here the ListView will be displayed
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View layoutView = mContext.getLayoutInflater().inflate(R.layout.device_item_custom, null, true);
        TextView mTextView = (TextView) layoutView.findViewById(R.id.adapter_text);
        ImageView mImageView = (ImageView) layoutView.findViewById(R.id.adapter_image);
        mTextView.setText(mNames.get(position));
        mImageView.setImageDrawable(mImages.get(position));
        return layoutView;
    }
}
