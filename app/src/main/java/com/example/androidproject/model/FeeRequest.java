package com.example.androidproject.model;

public class FeeRequest {

    private int userID;
    private int instituteID;

    public FeeRequest(int userID, int instituteID) {
        this.userID = userID;
        this.instituteID = instituteID;
    }

    public int getUserID() {
        return userID;
    }

    public int getInstituteID() {
        return instituteID;
    }
}