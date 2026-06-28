package com.example.androidproject.model.summary;


import com.google.gson.annotations.SerializedName;

public class InquirySummaryRequest {

    @SerializedName("filterType")
    private String filterType;

    @SerializedName("userID")
    private int userID;

    @SerializedName("instituteID")
    private int instituteID;

    public InquirySummaryRequest(String filterType, int userID, int instituteID) {
        this.filterType  = filterType;
        this.userID      = userID;
        this.instituteID = instituteID;
    }

    public String getFilterType()  { return filterType; }
    public int getUserID()         { return userID; }
    public int getInstituteID()    { return instituteID; }
}