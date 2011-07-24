package com.tapdom.climbinglog;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class EditEntryActivity extends Activity implements TextWatcher {
    private String TAG = "EditEntryActivity";
    private static final int DIALOG_CHANGE_DATE = 0;
    private static final int DIALOG_CHANGE_TIME = 1;
    private static final int DIALOG_QUIT_UNSAVED = 2;

    private EditText etAddress;
    private EditText etComments;
    private EditText etCoords;
    private TextView tvDate;
    private Button btnChangeDate;
    private Button btnSave;
    
    private DataBaseHelper dbHelper;
    private LogEntry entry;
    
    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMinute;

    private boolean hasChanged = false;
    
    private void log(String s) {
        Log.v(TAG, s);
    }
    
    private void alert(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_entry);
        
        etAddress = (EditText) findViewById(R.id.et_address);
        etComments = (EditText) findViewById(R.id.et_comments);
        etCoords = (EditText) findViewById(R.id.et_coords);
        tvDate = (TextView) findViewById(R.id.tv_date);
        btnChangeDate = (Button) findViewById(R.id.btn_change_date_start);
        btnSave = (Button) findViewById(R.id.btn_save);
        
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        int id = bundle.getInt("entry_id");
        
        dbHelper = new DataBaseHelper(this); // Opens and prepares database, manages queries
        dbHelper.open();
        this.entry = dbHelper.selectEntry(id);
        dbHelper.close();
        
        log("Read entry from db: " + this.entry);
        
        etAddress.setText(entry.address);
        etComments.setText(entry.comment);
        etCoords.setText(entry.latitude + ", " + entry.longitude);

        etAddress.addTextChangedListener(this);
        etCoords.addTextChangedListener(this);
        etComments.addTextChangedListener(this);

        java.sql.Date dateStarted = new java.sql.Date(entry.date_start);
        mYear = dateStarted.getYear();
        mMonth = dateStarted.getMonth();
        mDay = dateStarted.getDate();

        java.sql.Time timeStarted = new java.sql.Time(entry.date_start);
        mHour = timeStarted.getHours();
        mMinute = timeStarted.getMinutes();

        tvDate.setText(dateStarted.toLocaleString());
        
        btnChangeDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_CHANGE_DATE);
            }
        });
        
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });        
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_CHANGE_DATE:
            return new DatePickerDialog(this, mDateSetListener, mYear+1900, mMonth, mDay);
        case DIALOG_CHANGE_TIME:
            return new TimePickerDialog(this, mTimeSetListener, mHour, mMinute, false);
        case DIALOG_QUIT_UNSAVED:
            return buildQuitUnsavedDialog();
        }
        return null;
    }
    

    // the callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            if (mYear != year - 1900 || mMonth != monthOfYear || mDay != dayOfMonth) {
                hasChanged = true;
            }
        
            mYear = year - 1900;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            updateDisplay();
            showDialog(DIALOG_CHANGE_TIME);
        }
    };
    
    // the callback received when the user "sets" the time in the dialog
    private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if (mHour != hourOfDay || mMinute != minute) {
                hasChanged = true;
            }
            
            mHour = hourOfDay;
            mMinute = minute;
            updateDisplay();
        }
    };

    protected void updateDisplay() {
        Date date = new java.util.Date(mYear, mMonth, mDay, mHour, mMinute);
        tvDate.setText(date.toLocaleString());
    }

    @Override
    public void afterTextChanged(Editable s) {
        hasChanged = true;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    /**
     * Confirm whether to save changes before exiting
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!hasChanged)
            return super.onKeyDown(keyCode, event);

        // Back button: Ask the user if he really wants to quit
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            showDialog(DIALOG_QUIT_UNSAVED);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
    
    private Dialog buildQuitUnsavedDialog() {
        Builder builder = new AlertDialog.Builder(this)
        .setMessage("You have unsaved changes. Do you want to save them now?")
        .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                save();
                finish();
            }
        })
        .setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        
        return builder.create();
    }

    protected void save() {
        Date date = new java.util.Date(mYear, mMonth, mDay, mHour, mMinute);
        entry.date_start = date.getTime();
        entry.address = etAddress.getText().toString();
        entry.comment = etComments.getText().toString();
        
        String[] coords = etCoords.getText().toString().split(",");
        if (coords.length == 2) {
            entry.latitude = Double.valueOf(coords[0]);
            entry.longitude = Double.valueOf(coords[1]);
        } else {
            entry.latitude = null;
            entry.longitude = null;            
        }
        
        dbHelper.open();
        int r = dbHelper.update(entry);
        dbHelper.close();
        
        hasChanged = false;
        
        alert("Entry Updated");
    }    
}
