package com.example.androidproject.model.course;

import com.google.gson.annotations.SerializedName;

public class BatchTimeAddRequest {

    @SerializedName("timeID")
    public int timeID;

    @SerializedName("description")
    public String description;

    @SerializedName("startTime")
    public String startTime;

    @SerializedName("endTime")
    public String endTime;

    @SerializedName("userID")
    public int userID;

    @SerializedName("instituteID")
    public int instituteID;

    @SerializedName("capacity")
    public int capacity;

    public BatchTimeAddRequest(int timeID, String description, String startTime,
                               String endTime, int userID, int instituteID,
                               int capacity) {
        this.timeID = timeID;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.userID = userID;
        this.instituteID = instituteID;
        this.capacity = capacity;
    }
}
