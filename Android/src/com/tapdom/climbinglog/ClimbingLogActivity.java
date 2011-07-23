package com.tapdom.climbinglog;

import java.io.IOException;
import java.util.Date;
import java.util.List;

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
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ClimbingLogActivity extends Activity {
    private String TAG = "ClimbingLogActivity";

    private static final int DIALOG_ADD_ENTRY = 1;

    private static final int CONTEXT_ITEM_EDIT = 0;
    private static final int CONTEXT_ITEM_DELETE = 1;

    private TextView tvLocation;
    private TextView tvDate;
    private TextView tvCoords;
    private ListView lstLog;
    
    private LocationManager locationManager;
    private Handler mHandler = new Handler();

    private DataBaseHelper dbHelper;
    private LogListAdapter logAdapter;
    
    private Location lastLocation = null;
    private Address lastAddress = null;
    private Date lastDate = null;
    private String lastAddressString = null;
    
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
        
        dbHelper = new DataBaseHelper(this); // Opens and prepares database, manages queries
        showLogListView();
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
        tvCoords = (TextView) layout.findViewById(R.id.tv_coords);
        tvDate = (TextView) layout.findViewById(R.id.tv_date);
                
        Builder dialog = new AlertDialog.Builder(this);
        dialog.setView(layout);
        dialog.setTitle("Start a Climbing Entry");
        dialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Double lat = (lastLocation != null) ? lastLocation.getLatitude() : null;
                Double lng = (lastLocation != null) ? lastLocation.getLongitude() : null;
                Long date = (lastDate != null) ? lastDate.getTime() : null;
                LogEntry entry = new LogEntry(null, lat, lng, lastAddressString, null, null, date, null);
                dbHelper.insert(entry);
                logAdapter.insert(dbHelper.selectLast(), 0);  // querying from db to get the id (for edit, delete)
                lstLog.invalidate();
            }
        });
        dialog.setNegativeButton("Cancel", null);
        
        return dialog.create();
    }
    
    private void updateAddEntryDialog(AlertDialog dialog) {
        // Set date and time
        lastDate = new Date();
        tvDate.setText(lastDate.toGMTString());
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
            lastLocation = location;
            tvCoords.setText(location.getLatitude() + ", " + location.getLongitude());
            mHandler.post(new FindAddressForLocation());
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };

    // Convert a location to an address
    private class FindAddressForLocation implements Runnable {        
        @Override
        public void run() {
            Geocoder geocoder = new Geocoder(ClimbingLogActivity.this);
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            lastAddressString = "";

            if (addresses != null) {
                log("received " + addresses.size() + " addresses");
                if (addresses.size() > 0) {
                    log("address: " + addresses.get(0));
                    lastAddress = addresses.get(0);
                    final int n = lastAddress.getMaxAddressLineIndex();
                    for (int i=0; i<=n; i++) {
                        lastAddressString += lastAddress.getAddressLine(i) + "\n";
                    }
                }
            }

            // Remove trailing newlines
            lastAddressString = lastAddressString.trim();
            
            // Set coords to dialog textview
            tvLocation.setText(lastAddressString);
        }
    }
    
    private void showLogListView() {
        // Instantiate the adapter for attaching to the ListView
        logAdapter = new LogListAdapter(this, R.layout.log_list_entry, dbHelper.selectAll(20));
        
        // Attach more games adapter to listview and set OnItemClickListener
        lstLog = (ListView) findViewById(R.id.lstLog);
        lstLog.setAdapter(logAdapter);
        lstLog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LogEntry item = logAdapter.getItem(position);
                log("clicked on " + item.title);
            }
        });
        registerForContextMenu(lstLog);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.lstLog) {
            menu.add(Menu.NONE, CONTEXT_ITEM_EDIT, 0, "Edit");
            menu.add(Menu.NONE, CONTEXT_ITEM_DELETE, 1, "Delete");
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        
        switch (menuItemIndex) {
        case CONTEXT_ITEM_EDIT:
            log("edit item at pos " + info.id + " (id=" + logAdapter.getItem((int) info.id).id);
            break;

        case CONTEXT_ITEM_DELETE:
            log("delete item at pos " + info.id + " (id=" + logAdapter.getItem((int) info.id).id);
            break;
            
        default:
            break;
        }

        return true;
    }
}