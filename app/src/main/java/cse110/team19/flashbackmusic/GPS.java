package cse110.team19.flashbackmusic;

import android.Manifest;
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

/**
 * Created by Tyler on 2/17/18.
 */

// TODO THIS CODE IS VERY SIMILAR TO ANDROIDHIVE BUT
// TODO THE FIELDS AND CONSTRUCTORS ARE ALL 'REQUIRED'
public class GPS extends Service implements LocationListener {
    private final Context context;
    boolean GPSEnabled = false;
    boolean networkEnabled = false;
    boolean canGetLoc = false;
    Location location;
    double latitude;
    double longitude;

    private static final long minDistanceToUpdate = 10;
    private static final long minTimeBetweenUpdates = 1000 * 60 * 1;

    protected LocationManager locationManager;

    public GPS(Context c) {
        context = c;
        getLocation();
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) context
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            GPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            networkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!GPSEnabled && !networkEnabled) {
                // no network provider is enabled
            } else {
                canGetLoc = true;
                // First get location from Network Provider
                if (networkEnabled) {
                    // TODO WHAT IS THIS
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        //return TODO;
                    }

                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            minTimeBetweenUpdates,
                            minDistanceToUpdate, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (GPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                minTimeBetweenUpdates,
                                minDistanceToUpdate, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    public double getLatitude() {
        if (location != null) {
            return location.getLatitude();
        }
        return 0.00;
    }

    public double getLongitude() {
        if (location != null) {
            return location.getLongitude();
        }
        return 0.00;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    // TODO MAYBE: prompt user to turn on GPS with a toast message
    // TODO MAYBE: ask user to stop using location
}
