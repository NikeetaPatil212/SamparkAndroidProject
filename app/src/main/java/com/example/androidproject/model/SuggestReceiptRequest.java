package com.example.androidproject.model;

public class SuggestReceiptRequest {

    private int userID;
    private int instituteID;

    public SuggestReceiptRequest(int userID, int instituteID) {
        this.userID = userID;
        this.instituteID = instituteID;
    }
}