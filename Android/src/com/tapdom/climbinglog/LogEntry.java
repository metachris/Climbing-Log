package com.tapdom.climbinglog;

import java.util.Date;


public class LogEntry {
    public String title;

    public double latitude;
    public double longitude;
    public String address;
    
    public String partners;
    public String comment;
    
    public long date_start;
    public long date_end;

    public LogEntry(String title, double latitude, double longitude, String address, String partners, String comment, long date_start, long date_end) {
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        
        this.partners = partners;
        this.comment = comment;
        
        this.date_start = date_start;
        this.date_end = date_end;
    }

    public LogEntry(String title, double latitude, double longitude, String address, String partners, String comment, Date date_start, Date date_end) {
        this(title, latitude, longitude, address, partners, comment, date_start.getTime(), date_end.getTime());
    }
}