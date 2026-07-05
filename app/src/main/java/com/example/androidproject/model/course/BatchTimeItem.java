package com.example.androidproject.model.course;

import com.google.gson.annotations.SerializedName;

public class BatchTimeItem {

    @SerializedName(value = "timingID", alternate = {"timeID", "TimeID", "TimingID"})
    public int timingID;

    @SerializedName(value = "timing_Description", alternate = {"description", "timingDescription", "Timing_Description"})
    public String timingDescription;

    @SerializedName(value = "startTime", alternate = {"StartTime"})
    public String startTime;

    @SerializedName(value = "endTime", alternate = {"EndTime"})
    public String endTime;

    @SerializedName(value = "capacity", alternate = {"Capacity"})
    public int capacity;

    @SerializedName(value = "availableSeats", alternate = {"AvailableSeats"})
    public int availableSeats;

    @SerializedName(value = "filled", alternate = {"Filled"})
    public int filled;
}
