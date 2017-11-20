package com.dulingl.gpstracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public final class LocationReceiver extends BroadcastReceiver {

    private final String TAG = "LocationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,intent.toString());

        Bundle locBundle = intent.getExtras();
        Location newLocation = (Location)locBundle.get(android.location.LocationManager.KEY_LOCATION_CHANGED);

//        Toast.makeText(context, newLocation.toString(), Toast.LENGTH_SHORT).show();

        // Extract location info
        double lat = newLocation.getLatitude();
        double lon = newLocation.getLongitude();
        float accuracy = newLocation.getAccuracy();
        long time = newLocation.getTime();

        // Display location info
        String locInfo = "Latitude: " + lat + "\n Longitude: " + lon + "\n Accuracy: " + accuracy + "\n Time: " + time;
        TextView locDisplay = (TextView)findViewById();
        locDisplay.setText(locInfo);

    }
}
