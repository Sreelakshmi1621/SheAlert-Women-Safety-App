package com.example.shealert;

public class NotificationModel {

    public String id;
    public String title;
    public String message;
    public String timestamp;
    public String type;

    public NotificationModel() { }

    public NotificationModel(String title,
                             String message,
                             String timestamp,
                             String type) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
    }
}