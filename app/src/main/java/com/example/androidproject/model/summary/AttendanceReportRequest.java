package com.example.androidproject.model.summary;

public class AttendanceReportRequest {

    public int userID;
    public int instituteID;
    public String classDate; // ISO-8601 timestamp, e.g. 2026-07-02T13:51:30.925Z

    public AttendanceReportRequest(int userID, int instituteID, String classDate) {
        this.userID = userID;
        this.instituteID = instituteID;
        this.classDate = classDate;
    }
}