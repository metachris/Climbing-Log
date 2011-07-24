package com.tapdom.climbinglog;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class DataBaseHelper {
    private static final String DATABASE_NAME = "main.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "log_entries";

    private Context context;
    private SQLiteDatabase db;
    private OpenHelper openHelper;
    
    private static final String INSERT = "insert into " + TABLE_NAME + 
        "(title, latitude, longitude, address, comment, partners, date_start, date_end) " +
        "values (?, ?, ?, ?, ?, ?, ?, ?)";

    public DataBaseHelper(Context context) {
        this.context = context;
        openHelper = new OpenHelper(this.context);
    }

    public void open() {
        this.db = openHelper.getWritableDatabase();
    }
    
    public void close() {
        openHelper.close();
    }
    
    /**
     * Insert a LogEntry object as new item in the database
     */
    public long insert(LogEntry logEntry) {
        SQLiteStatement insertStmt = this.db.compileStatement(INSERT);
        if (logEntry.title != null) insertStmt.bindString(1, logEntry.title);
        if (logEntry.latitude != null) insertStmt.bindDouble(2, logEntry.latitude);
        if (logEntry.longitude != null) insertStmt.bindDouble(3, logEntry.longitude);
        if (logEntry.address != null) insertStmt.bindString(4, logEntry.address);
        if (logEntry.comment != null) insertStmt.bindString(5, logEntry.comment);
        if (logEntry.partners != null) insertStmt.bindString(6, logEntry.partners);
        if (logEntry.date_start != null) insertStmt.bindLong(7, logEntry.date_start);
        if (logEntry.date_end != null) insertStmt.bindLong(8, logEntry.date_end);
        return insertStmt.executeInsert();
    }

    /**
     * Delete an entry from the DB based on its id
     */
    public void delete(int id) {
        this.db.delete(TABLE_NAME, "id = ?", new String[] { String.valueOf(id)});
    }

    /**
     * Purge database
     */
    public void deleteAll() {
        this.db.delete(TABLE_NAME, null, null);
    }
    
    public int update(LogEntry entry) {
        ContentValues cv = new ContentValues();
        cv.put("title", entry.title);
        cv.put("latitude", entry.latitude);
        cv.put("longitude", entry.longitude);
        cv.put("address", entry.address);
        cv.put("comment", entry.comment);
        cv.put("partners", entry.partners);
        cv.put("date_start", entry.date_start);
        cv.put("date_end", entry.date_end);
        Log.v("db", "=== updating: " + entry);
        return this.db.update(TABLE_NAME, cv, "id=?", new String[] { String.valueOf(entry.id) });
    }

    /**
     * Prepare the cursor for select statements
     */
    private Cursor getSelectCursor() {
        return getSelectCursor(null, null);
    }

    private Cursor getSelectCursor(String selection, String[] selectionArgs) {
        Cursor cursor = this.db.query(TABLE_NAME, new String[] { "id", "title", "latitude", "longitude", 
                "address", "comment", "partners", "date_start", "date_end" }, 
                selection, selectionArgs, null, null, "date_start desc");
        return cursor;
    }
    
    private LogEntry cursorToLogEntry(Cursor cursor) {
        LogEntry entry = new LogEntry(cursor.getString(1), cursor.getDouble(2), 
                cursor.getDouble(3), cursor.getString(4), cursor.getString(5), 
                cursor.getString(6), cursor.getLong(7), cursor.getLong(8));
        return entry;
    }
    
    /**
     * Select last added entry from the database
     */
    public LogEntry selectLast() {
        Cursor cursor = getSelectCursor();
        LogEntry entry = null;

        if (cursor.moveToFirst()) {
            entry = cursorToLogEntry(cursor);
            entry.id = cursor.getInt(0);
        }
        if (cursor != null && !cursor.isClosed()) cursor.close();
        
        return entry;
    }

    /**
     * Select all entries from the database
     */
    public List<LogEntry> selectAll() {
        return selectAll(0);
    }

    /**
     * Select the last n entries from the database
     */
    public List<LogEntry> selectAll(int max_items) {
        Cursor cursor = getSelectCursor();
        List<LogEntry> list = new ArrayList<LogEntry>();
        int count = 0;
        if (cursor.moveToFirst()) {
            do {
                LogEntry entry = cursorToLogEntry(cursor);
                entry.id = cursor.getInt(0);
                list.add(entry);
                
                count++;
                if (count == max_items) break;
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) cursor.close();
        return list;
    }
    
    public LogEntry selectEntry(int id) {
        Cursor cursor = getSelectCursor("id = ?", new String[] { String.valueOf(id) });
        LogEntry entry = null;
        if (cursor.moveToFirst()) {
            entry = cursorToLogEntry(cursor);
            entry.id = cursor.getInt(0);
        }   
        if (cursor != null && !cursor.isClosed()) cursor.close();
        return entry;
    }

    /**
     * The OpenHelper assists with opening a writeable database,
     * creating the tables and updating in case of schema change.
     */
    private static class OpenHelper extends SQLiteOpenHelper {
        OpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + 
                "id INTEGER PRIMARY KEY, " + 
                "title TEXT, " +
                
                "latitude REAL, " +
                "longitude REAL, " +
                
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