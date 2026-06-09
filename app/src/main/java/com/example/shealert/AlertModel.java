package com.example.shealert;

public class AlertModel {

    public String triggerType;
    public String timestamp;
    public String latitude;
    public String longitude;
    public String battery;

    public AlertModel() {
        // Required empty constructor for Firebase
    }

    public AlertModel(String triggerType, String timestamp,
                      String latitude, String longitude,
                      String battery) {

        this.triggerType = triggerType;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.battery = battery;
    }
}