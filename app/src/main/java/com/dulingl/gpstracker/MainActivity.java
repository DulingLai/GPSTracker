package com.dulingl.gpstracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    private SupportMapFragment mGoogleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize the map fragment
        mGoogleMap = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
        mGoogleMap.getMapAsync(this);

        // register the on click callback for enabling location
        registerOnClickCallbackLocation();

        // register the on click callback for enabling transformation


    }

    private void registerOnClickCallbackLocation() {
        final ToggleButton button = (ToggleButton) findViewById(R.id.toggleButton);

        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Intent locationServiceIntent = new Intent(MainActivity.this, LocationService.class);
                if (isChecked){
                    Toast.makeText(getApplication(), "Starting Location Service", Toast.LENGTH_SHORT).show();
                    startService(locationServiceIntent);
                } else {
                    Toast.makeText(getApplication(), "Location Service Stopped", Toast.LENGTH_SHORT).show();
                    stopService(locationServiceIntent);
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}
