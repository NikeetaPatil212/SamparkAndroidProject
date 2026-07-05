package com.example.androidproject.model.course;


import com.google.gson.annotations.SerializedName;

public class BatchAddRequest {

    @SerializedName("courseID")    public int    courseID;
    @SerializedName("batchID")     public int    batchID;
    @SerializedName("batchName")   public String batchName;
    @SerializedName("startDate")   public String startDate;
    @SerializedName("endDate")     public String endDate;
    @SerializedName("userID")      public int    userID;
    @SerializedName("instituteID") public int    instituteID;

    public BatchAddRequest(int courseID, int batchID, String batchName,
                        String startDate, String endDate,
                        int userID, int instituteID) {
        this.courseID    = courseID;
        this.batchID     = batchID;
        this.batchName   = batchName;
        this.startDate   = startDate;
        this.endDate     = endDate;
        this.userID      = userID;
        this.instituteID = instituteID;
    }
}