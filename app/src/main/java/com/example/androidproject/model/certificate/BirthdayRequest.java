package com.example.androidproject.model.certificate;

// BirthdayRequest.java
public class BirthdayRequest {
    private String birthdayDate;
    private int userID;
    private int instituteID;

    public BirthdayRequest(String birthdayDate, int userID, int instituteID) {
        this.birthdayDate = birthdayDate;
        this.userID       = userID;
        this.instituteID  = instituteID;
    }
}