package com.dulingl.gpstracker;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity{

    private LocationManager mLocationManager;
    final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start regular location request (default)
        requestLocation();

//        // register broadcast receivers for activity data
//        LocalBroadcastManager.getInstance(this).registerReceiver(DulingLocationReceiver, new IntentFilter("DulingDelayedTask"));

        // register the on click callback for enabling activity-aware location requests
        registerOnClickCallbackLocation();
    }

    private void requestLocation() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //Location Permission already granted

            // Create a pending intent
            Intent LocationIntent = new Intent("com.duling.GPSTracker.NEW_LOCATION_RECEIVED");
            PendingIntent pendingLocIntent = PendingIntent.getBroadcast( getApplicationContext(), 0, LocationIntent, PendingIntent.FLAG_CANCEL_CURRENT );

            // request location updates
            mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, pendingLocIntent);
        } else {
            //Request Location Permission
            checkLocationPermission();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        // create pending intent
        Intent LocationIntent = new Intent("com.duling.GPSTracker.NEW_LOCATION_RECEIVED");
        PendingIntent pendingLocIntent = PendingIntent.getBroadcast( MainActivity.this, 0, LocationIntent, PendingIntent.FLAG_NO_CREATE);

        // remove location updates
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.removeUpdates(pendingLocIntent);
    }

    private void registerOnClickCallbackLocation() {
        final ToggleButton button = (ToggleButton) findViewById(R.id.toggleButton);

        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Intent locationServiceIntent = new Intent(MainActivity.this, duling.DulingDelayedTask.class);
                if (isChecked){
                    /*
                        Enable transformation
                     */

                    // Remove original location updates
                    mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                    Intent LocationIntent = new Intent("com.duling.GPSTracker.NEW_LOCATION_RECEIVED");
                    PendingIntent pendingLocIntent = PendingIntent.getBroadcast( MainActivity.this, 0, LocationIntent, PendingIntent.FLAG_NO_CREATE);
                    mLocationManager.removeUpdates(pendingLocIntent);

                    Toast.makeText(getApplication(), "Starting Location Tracking Transformation", Toast.LENGTH_SHORT).show();

                    // Start activity-aware location service
                    if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        startService(locationServiceIntent);
                    } else {
                        checkLocationPermission();
                    }
                } else {

                    // Stop activity-aware location service
                    Toast.makeText(getApplication(), "Location Tracking Transformation Stopped", Toast.LENGTH_SHORT).show();
                    stopService(locationServiceIntent);

                    // Request original location updates
                    requestLocation();
                }
            }
        });
    }

//    private BroadcastReceiver DulingLocationReceiver = new BroadcastReceiver() {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            // debug
//            Log.e(TAG, "New Location received");
//            Bundle locIntentBundle = intent.getExtras();
//            Location newLocation = (Location)locIntentBundle.get(android.location.LocationManager.KEY_LOCATION_CHANGED);
//            displayLocation(newLocation);
//        }
//    };

    public void displayLocation(Location location) {
        // extract location info
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        float locAccuracy = location.getAccuracy();
        long locTime = location.getTime();
        String locInfo = "Latitude: " + lat + "\n Longitude: " + lon + "\n Accuracy: " + locAccuracy + "\n Time: " + locTime;

        // Display the location info
        TextView locDisplay = (TextView) findViewById(R.id.locationDisplay);
        locDisplay.setText(locInfo);
    }

    /* methods to request permission at run-time*/
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        requestLocation();
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
