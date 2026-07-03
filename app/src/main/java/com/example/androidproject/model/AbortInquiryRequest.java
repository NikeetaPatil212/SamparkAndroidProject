package com.example.androidproject.model;


public class AbortInquiryRequest {
    private int studentID;
    private int userID;
    private int instituteID;
    private int operatorID;

    public AbortInquiryRequest(int studentID, int userID, int instituteID, int operatorID) {
        this.studentID   = studentID;
        this.userID      = userID;
        this.instituteID = instituteID;
        this.operatorID  = operatorID;
    }
}