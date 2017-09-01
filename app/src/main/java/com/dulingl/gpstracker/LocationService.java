package com.dulingl.gpstracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LocationService extends Service implements
        LocationListener {

    private String TAG = "LocationService";
    private LocationManager lmgr;

    // boolean to enable the test
    private boolean testTransformation = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("LocationService", "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e("LocationService", "onCreate");
        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        lmgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Log.e(TAG, "Starting Periodic Location Updates");
        lmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onDestroy() {
        lmgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Log.e(TAG, "Stop Periodic Location Updates");
        lmgr.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e("onLocationChanged", "LocationService");
        double accuracy = location.getAccuracy();
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        long time = location.getTime();
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date(time);
        String formattedTime = format.format(date);
        Log.e(TAG, "Location: " + lat + " ," + lon + "; Accuracy: " + accuracy + "; Time: " + formattedTime);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
//        Log.e("onStatusChanged", "LocationService");
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.e("onProviderEnabled", "LocationService");
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.e("onProviderDisabled", "LocationService");
    }

}
