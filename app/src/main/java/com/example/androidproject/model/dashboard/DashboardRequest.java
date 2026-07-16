package com.example.androidproject.model.dashboard;

import com.google.gson.annotations.SerializedName;

/**
 * Shared request body for both:
 *   /api/InstituteControllersV1/DashboardCards
 *   /api/InstituteControllersV1/DashboardCharts
 */
public class DashboardRequest {

    @SerializedName("userID")
    private int userID;

    @SerializedName("instituteID")
    private int instituteID;

    public DashboardRequest(int userID, int instituteID) {
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