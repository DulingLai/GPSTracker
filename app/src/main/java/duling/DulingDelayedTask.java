package duling;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import java.util.Timer;
import java.util.TimerTask;



public class DulingDelayedTask extends Service implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "DulingDelayedTask";
    private final double MAX_ACCURACY = 7.8;

    public long timeToNextUpdate = 0;
    public long lastLocationTime = 0;
    public long lastActivityTime = 0;
    public double lastSpeed = 0;
    public boolean activityReceived = false;
    public double lastAccuracy = 0;
    public double bestAccuracy = 999;
    public int numOfUpdates = 0;

    public boolean origLocationRequest = false;
    public boolean transLocationRequest = false;

    LocationManager DulingLocManager;
    GoogleApiClient mGoogleApiClient;

    // timer for delayed task
    public Timer timer = new Timer();
    myTimer mTask = new myTimer();

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {

        Log.e(TAG, "onCreate");

        // connect to Google Play Services API client
        buildGoogleClient();

        Log.e(TAG,"Starting Activity Service with intent");

        // register broadcast receivers for activity data
        LocalBroadcastManager.getInstance(this).registerReceiver(DulingActivityDataReceiver, new IntentFilter("DulingActivityRecognition"));

        checkActivityData();
    }

    private void buildGoogleClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();
    }

    private void checkActivityData() {
        // check if we have received activity data frem activity recognition service
        if (activityReceived == false){
            originalLocationRequest();
        } else {
            checkUserActivity(lastSpeed);
        }
    }

    private void checkUserActivity(double speed) {
        if (speed == 1.39) {
            Log.e(TAG,"request transformed location updates in checkUserActivity()");
            requestTransformedLocationUpdates();
        } else if (speed == 0) {
            Log.e(TAG,"removing location updates in checkUserActivity()");
            removeLocationUpdates();
        } else {
            Log.e(TAG,"request original location updates in checkUserActivity()");
            originalLocationRequest();
        }
    }

    private void originalLocationRequest() {
        DulingLocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(DulingDelayedTask.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            DulingLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
            // we also register the original location listener so that it can also received the location update
            origLocationRequest = true;
        }
    }

    private void removeLocationUpdates() {
        DulingLocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        DulingLocManager.removeUpdates(DulingDelayedTask.this);
    }

    private synchronized void requestTransformedLocationUpdates() {
        if (lastAccuracy>0){
            if (lastAccuracy > MAX_ACCURACY) {
                lastAccuracy = MAX_ACCURACY;
            }
        } else {
            lastAccuracy = MAX_ACCURACY;
        }

        // if we register for original location update, remove it
        if (origLocationRequest == true){
            removeLocationUpdates();
            origLocationRequest = false;
        }

        // if already exist a delayed task, remove it
        if (mTask!=null){
            mTask.cancel();
            //debug
            Log.e(TAG,"Scheduled Task Canceled");
        }

        // calculate when we should start to request for location: 2 seconds before we reach the end of accuracy circle
        timeToNextUpdate = (long) (lastAccuracy/lastSpeed*1000 - 2000);

        // debug
        Log.e(TAG, "Accuracy is " + lastAccuracy + "m; " + String.valueOf(timeToNextUpdate) + " ms before next update");

        // register a timer to fire the location update
        mTask = new myTimer();
        timer.schedule(mTask,timeToNextUpdate);
    }

    @Override
    public void onLocationChanged(Location location){

        Log.e(TAG,"New Location Update");
        if (transLocationRequest){
            int MAX_NUM_UPDATES = 3;
            if (numOfUpdates < MAX_NUM_UPDATES){
                if (location.getAccuracy() < bestAccuracy){
                    bestAccuracy = location.getAccuracy();
                }
                numOfUpdates++;
            } else {
                Log.e(TAG,"Max Number of Update received, remove updates");
                removeLocationUpdates();
                transLocationRequest = false;
                numOfUpdates = 0;
                lastAccuracy = bestAccuracy;
                lastLocationTime = location.getTime();
                bestAccuracy = 999;
                checkActivityData();
            }
        } else {
            // update the location accuracy and timestamp
            lastAccuracy = location.getAccuracy();
            lastLocationTime = location.getTime();
            checkActivityData();
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        // stop activity recognition service
        Intent intent = new Intent( this, DulingActivityRecognition.class );
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates( mGoogleApiClient, pendingIntent);

        Log.e(TAG,"Stop Activity Service with intent");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(DulingActivityDataReceiver);

        // stop location updates
        removeLocationUpdates();
    }

    private BroadcastReceiver DulingActivityDataReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // debug
            Log.e(TAG, "Activity Data Received in DelayedTask");

            activityReceived = true;
            lastSpeed = intent.getDoubleExtra("DulingSpeed",lastSpeed);
            lastActivityTime = intent.getLongExtra("DulingActivityTimestamp", lastActivityTime);

           checkUserActivity(lastSpeed);
        }
    };

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.e(TAG,"onStatusChanged");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.e(TAG,"onProviderEnabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.e(TAG,"onProviderDisabled");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // start activity recognition service
        Intent intent = new Intent( this, DulingActivityRecognition.class );
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mGoogleApiClient, 3000, pendingIntent );
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public class myTimer extends TimerTask
    {
        @Override
        public void run() {
            Log.e(TAG,"Timer expires, request a new location update");
            requestTimedLocation();
        }
    }

    public void requestTimedLocation(){
        DulingLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // request location updates and remove it after 2s
        if (ContextCompat.checkSelfPermission(DulingDelayedTask.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            DulingLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            transLocationRequest = true;
        }


    }
}

