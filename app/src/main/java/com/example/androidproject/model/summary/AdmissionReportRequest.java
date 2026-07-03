package com.example.androidproject.model.summary;


public class AdmissionReportRequest {
    private int userID;
    private int instituteID;

    public AdmissionReportRequest(int userID, int instituteID) {
        this.userID      = userID;
        this.instituteID = instituteID;
    }
}