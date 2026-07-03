package com.example.androidproject.model.summary;

public class StudyMaterialReportRequest {

    public int userID;
    public int instituteID;

    public StudyMaterialReportRequest(int userID, int instituteID) {
        this.userID = userID;
        this.instituteID = instituteID;
    }
}