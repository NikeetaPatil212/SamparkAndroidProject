package com.example.androidproject.model.notification;

// NotificationStudentRequest.java
public class NotificationStudentRequest {
    private int userID;
    private int instituteID;

    public NotificationStudentRequest(int userID, int instituteID) {
        this.userID      = userID;
        this.instituteID = instituteID;
    }
}
