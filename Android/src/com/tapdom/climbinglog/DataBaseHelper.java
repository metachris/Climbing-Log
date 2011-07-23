package com.tapdom.climbinglog;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class DataBaseHelper {
    private static final String DATABASE_NAME = "main.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "log_entries";

    private Context context;
    private SQLiteDatabase db;

    private SQLiteStatement insertStmt;
    private static final String INSERT = "insert into " + TABLE_NAME + 
        "(title, latitude, longitude, address, comment, partners, date_start, date_end) " +
        "values (?, ?, ?, ?, ?, ?, ?, ?)";

    public DataBaseHelper(Context context) {
        this.context = context;
        OpenHelper openHelper = new OpenHelper(this.context);
        this.db = openHelper.getWritableDatabase();
        this.insertStmt = this.db.compileStatement(INSERT);
    }

    public long insert(LogEntry logEntry) {
        if (logEntry.title != null) this.insertStmt.bindString(1, logEntry.title);
        if (logEntry.latitude != null) this.insertStmt.bindDouble(2, logEntry.latitude);
        if (logEntry.longitude != null) this.insertStmt.bindDouble(3, logEntry.longitude);
        if (logEntry.address != null) this.insertStmt.bindString(4, logEntry.address);
        if (logEntry.comment != null) this.insertStmt.bindString(5, logEntry.comment);
        if (logEntry.partners != null) this.insertStmt.bindString(6, logEntry.partners);
        if (logEntry.date_start != null) this.insertStmt.bindLong(7, logEntry.date_start);
        if (logEntry.date_end != null) this.insertStmt.bindLong(8, logEntry.date_end);
        return this.insertStmt.executeInsert();
    }

    public void deleteAll() {
        this.db.delete(TABLE_NAME, null, null);
    }

    public List<LogEntry> selectAll() {
        return selectAll(0);
    }
    
    public List<LogEntry> selectAll(int max_items) {
        List<LogEntry> list = new ArrayList<LogEntry>();
        Cursor cursor = this.db.query(TABLE_NAME, new String[] { "title", "latitude", "longitude", 
                "address", "comment", "partners", "date_start", "date_end" }, 
                null, null, null, null, "id desc");

        int count = 0;
        if (cursor.moveToFirst()) {
            do {
                list.add(new LogEntry(cursor.getString(0), Double.valueOf(cursor.getLong(1)), 
                        Double.valueOf(cursor.getLong(2)), cursor.getString(3), 
                        cursor.getString(4), cursor.getString(5), cursor.getLong(6), 
                        cursor.getLong(7)));
                
                count++;
                if (count == max_items) break;
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return list;
    }

    private static class OpenHelper extends SQLiteOpenHelper {
        OpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + 
                "id INTEGER PRIMARY KEY, " + 
                "title TEXT, " +
                "latitude INTEGER, " +
                "longitude INTEGER, " +
                "address TEXT, " + 
                "comment TEXT, " + 
                "partners TEXT, " + 

                "date_start INTEGER, " +
                "date_end INTEGER" +
            ")");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("Example", "Upgrading database, this will drop tables and recreate.");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}