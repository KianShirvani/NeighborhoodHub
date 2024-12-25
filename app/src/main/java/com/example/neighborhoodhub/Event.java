package com.example.neighborhoodhub;

import java.io.Serializable;

/* This is a helper class that helps to extract Event information, and passing information across activities */

public class Event implements Serializable {
    private String titleEvent;
    private String dateTime;
    private String locationName;
    private String locationAddress;
    private String user_id;

    /* Constructor with arguments */
    public Event(String dateTime, String titleEvent, String locationName, String locationAddress, String user_id) {
        this.titleEvent = titleEvent;
        this.dateTime = dateTime;
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.user_id = user_id;
    }

    /* Getters and setters methods */
    public String getTitle() { return titleEvent; }
    public void setTitle(String title) { this.titleEvent = titleEvent; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getLocationAddress() { return locationAddress; }
    public void setLocationAddress(String locationAddress) { this.locationAddress = locationAddress; }

    public String getUserId(){return user_id;}
    public void setUserId(String user_id){this.user_id = user_id;}
}
