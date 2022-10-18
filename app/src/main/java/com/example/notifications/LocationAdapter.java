package com.example.notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

public class LocationAdapter extends ArrayAdapter {
    ArrayList<String> locationNames;
    HashMap<String,Boolean> choosenLocations;
    Context context;
    public LocationAdapter(@NonNull Context context,
                           int resource,
                           ArrayList<String> locationNames,
                           HashMap<String,Boolean> choosenLocations) {
        super(context,resource,locationNames);
        this.locationNames= locationNames;
        this.context=context;
        this.choosenLocations = choosenLocations;
    }
    @NonNull
    @Override
    public String getItem(int position){
        return locationNames.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View view;
        String locationName = getItem(position);
        if(Boolean.TRUE.equals(choosenLocations.get(locationName))) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.cardviewclicked, parent, false);
        }
        else {
            view = LayoutInflater.from(getContext()).inflate(R.layout.cardview, parent, false);
        }
        TextView locationTextView=view.findViewById(R.id.Location);
        locationTextView.setText(getItem(position));

        return view;
    }
}
