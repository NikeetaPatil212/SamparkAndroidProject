package com.example.androidproject.model.summary;


import com.google.gson.annotations.SerializedName;

public class AdmissionSummaryRequest {

    @SerializedName("userID")
    private int userID;

    @SerializedName("instituteID")
    private int instituteID;

    public AdmissionSummaryRequest(int userID, int instituteID) {
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