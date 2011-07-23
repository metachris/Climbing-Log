package com.tapdom.climbinglog;

public class LogEntry {
    public String title;

    public Double latitude;
    public Double longitude;
    public String address;
    
    public String partners;
    public String comment;
    
    public Long date_start;
    public Long date_end;

    public LogEntry(String title, Double latitude, Double longitude, String address, String partners, String comment, Long date_start, Long date_end) {
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        
        this.partners = partners;
        this.comment = comment;
        
        this.date_start = date_start;
        this.date_end = date_end;
    }
}