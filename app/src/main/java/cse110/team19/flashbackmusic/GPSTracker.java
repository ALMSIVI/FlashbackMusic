package cse110.team19.flashbackmusic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by Tyler on 3/10/18.
 * Tutorial: https://www.youtube.com/watch?v=lvcGh2ZgHeA
 */

public class GPSTracker extends Service {

    private LocationListener locationListener;
    private LocationListener locationListenerr;
    private LocationManager locationManagerr;
    private LocationManager locationManager;
    private Context context;
    private Location currentLocation;

    public GPSTracker () {
        super();
    }

    public GPSTracker(Context context) {
        this.context = context;
    }

    @SuppressLint("MissingPermission")
    public Location getLocation() {

        Location currentLocation = new Location("");

        if (!permissionRequest()) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);

            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            String provider = locationManager.getBestProvider(criteria, true);

            try {
                currentLocation = locationManager.getLastKnownLocation(provider);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return currentLocation;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Intent i = new Intent("Location Updated");
                i.putExtra("Coordinates", location.getLongitude() + " " + location.getLatitude());
                sendBroadcast(i);
                Log.d("onLocationChanged", "locationChanged");
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            // if user has not enabled location access, redirect them to settings
            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };



        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }



    /**
     * Ensure no memory leaks
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    /**
     * Request Permissions
     */
    public boolean permissionRequest() {
        // request permissions
        if (Build.VERSION.SDK_INT >= 23 && ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity)context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    100);

            return true;
        }
        Log.d(TAG, "Permission not granted");
        //Permission not granted by user so cancel the further execution.
        return false;
    }

}
