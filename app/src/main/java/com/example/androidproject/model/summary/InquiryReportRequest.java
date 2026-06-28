package com.example.androidproject.model.summary;


import com.google.gson.annotations.SerializedName;

public class InquiryReportRequest {
    @SerializedName("userID")      public int userID;
    @SerializedName("instituteID") public int instituteID;

    public InquiryReportRequest(int userID, int instituteID) {
        this.userID      = userID;
        this.instituteID = instituteID;
    }
}