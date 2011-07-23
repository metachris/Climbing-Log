package com.tapdom.climbinglog;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class AddEntryActivity extends Activity {
    private String TAG = "AddEntryActivity";
    private Handler mHandler = new Handler();
    private LocationManager locationManager;
    
    private TextView tvLocation;
    private TextView tvDate;
    private TextView tvTimeStart;
    
    private void log(String s) {
        Log.v(TAG, s);
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_entry);
        
        // Get references to views we'll manipulate
        tvLocation = (TextView) findViewById(R.id.tv_location);
        tvDate = (TextView) findViewById(R.id.tv_date);
        tvTimeStart = (TextView) findViewById(R.id.tv_time_start);
        
        // Set date and time
        Calendar c = Calendar.getInstance(); 
        tvDate.setText(c.get(Calendar.DAY_OF_MONTH) + "." + c.get(Calendar.MONTH) + "." + c.get(Calendar.YEAR));
        tvTimeStart.setText(c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE));
        
        // Start finding the current location
        startFindingLocation();
    }
    
    private void startFindingLocation() {
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    // Define a listener that responds to location updates
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            locationManager.removeUpdates(this);
            
            log("got location: " + location.getLatitude() + ", " + location.getLongitude());
            mHandler.post(new FindAddressForLocation(location));
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };

    // Convert a location to an address
    private class FindAddressForLocation implements Runnable {
        private Location location;
        
        public FindAddressForLocation (Location location) {
            this.location = location;
        }
        
        @Override
        public void run() {
            Geocoder geocoder = new Geocoder(AddEntryActivity.this);
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            String addr = location.getLatitude() + ", " + location.getLongitude();

            if (addresses != null) {
                log("received " + addresses.size() + " addresses");
                if (addresses.size() > 0) {
                    log("address: " + addresses.get(0));
                    Address address = addresses.get(0);
                    final int n = address.getMaxAddressLineIndex();
                    for (int i=0; i<=n; i++) {
                        addr += "\n" + address.getAddressLine(i);
                    }
                }
            }

            tvLocation.setText(addr);
        }
    }
}
