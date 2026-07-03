package com.example.androidproject.model.summary;
public class CertificateReportRequest {

    public int userID;
    public int instituteID;

    public CertificateReportRequest(int userID, int instituteID) {
        this.userID = userID;
        this.instituteID = instituteID;
    }
}