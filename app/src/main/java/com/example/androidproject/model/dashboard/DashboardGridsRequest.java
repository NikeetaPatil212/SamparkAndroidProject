package com.example.androidproject.model.dashboard;

public class DashboardGridsRequest {
    private int userID;
    private int instituteID;

    public DashboardGridsRequest(int userID, int instituteID) {
        this.userID = userID;
        this.instituteID = instituteID;
    }
}