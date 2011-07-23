package com.tapdom.climbinglog;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ClimbingLogActivity extends Activity {
    private String TAG = "ClimbingLogActivity";

    private static final int DIALOG_ADD_ENTRY = 1;
    
    private TextView tvLocation;
    private TextView tvDate;
    private TextView tvTimeStart;
    
    private LocationManager locationManager;
    private Handler mHandler = new Handler();

    private DataBaseHelper dbHelper;
    
    private void log(String s) {
        Log.v(TAG, s);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button btn_add_entry = (Button) findViewById(R.id.btn_add_entry);
        btn_add_entry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // startActivity(new Intent(ClimbingLogActivity.this, AddEntryActivity.class));
                showDialog(DIALOG_ADD_ENTRY);
            }
        });
        
        openDatabase();
    }


    /*
     ****************
     * Dialog Stuff *
     ****************
     */
    
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        
        switch(id) {
        case DIALOG_ADD_ENTRY:
            dialog = buildAddEntryDialog();
        }
        
        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch(id) {
        case DIALOG_ADD_ENTRY:
            updateAddEntryDialog((AlertDialog) dialog);
            startFindingLocation();
            break;
        }
    }
    
    private Dialog buildAddEntryDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.add_entry, null);

        tvLocation = (TextView) layout.findViewById(R.id.tv_location);
        tvDate = (TextView) layout.findViewById(R.id.tv_date);
        tvTimeStart = (TextView) layout.findViewById(R.id.tv_time_start);
                
        Builder dialog = new AlertDialog.Builder(this);
        dialog.setView(layout);
        dialog.setTitle("Start a Climbing Entry");
        dialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.setNegativeButton("Cancel", null);
        
        return dialog.create();
    }
    
    private void updateAddEntryDialog(AlertDialog dialog) {
        // Set date and time
        Calendar c = Calendar.getInstance(); 
        tvDate.setText(c.get(Calendar.DAY_OF_MONTH) + "." + c.get(Calendar.MONTH) + "." + c.get(Calendar.YEAR));
        tvTimeStart.setText(c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE));
        tvLocation.setText("Loading location...");
    }    


    /*
     ******************
     * Location Stuff *
     ******************
     */
    
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
            Geocoder geocoder = new Geocoder(ClimbingLogActivity.this);
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
    
    /*
     ******************
     * Database Stuff *
     ******************
     */
    public static String generateString(Random rng, String characters, int length) {
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }
    
    private static String rndStr(Random rng, int length) {
        return generateString(rng, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", length);
    }
    
    private LogEntry createRandomLogEntry() {
        Random r = new Random();
        java.util.Date d = new java.util.Date();
        long d1 = d.getTime() - 100000;
        long d2 = d.getTime() - 40000;
        LogEntry entry = new LogEntry(rndStr(r, 20), r.nextDouble(), r.nextDouble(), rndStr(r, 30), "", rndStr(r, 20), d1, d2);
        return entry;
    }
    
    private void openDatabase() {
        dbHelper = new DataBaseHelper(this);
        dbHelper.deleteAll();
        dbHelper.insert(createRandomLogEntry());
        dbHelper.insert(createRandomLogEntry());
        dbHelper.insert(createRandomLogEntry());
        dbHelper.insert(createRandomLogEntry());
        dbHelper.insert(createRandomLogEntry());
        dbHelper.insert(createRandomLogEntry());
        
    }
}