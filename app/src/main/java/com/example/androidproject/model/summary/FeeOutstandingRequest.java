package com.example.androidproject.model.summary;

public class FeeOutstandingRequest {

    private int userID;
    private int instituteID;

    public FeeOutstandingRequest(int userID, int instituteID) {
        this.userID = userID;
        this.instituteID = instituteID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getInstituteID() {
        return instituteID;
    }

    public void setInstituteID(int instituteID) {
        this.instituteID = instituteID;
    }
}