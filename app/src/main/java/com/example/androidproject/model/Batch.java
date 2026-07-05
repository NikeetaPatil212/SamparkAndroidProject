package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;

public class Batch {

    @SerializedName(value = "batchID", alternate = {"BatchID", "BatchId", "batchId"})
    private int batchID;

    @SerializedName(value = "courseID", alternate = {"CourseID", "CourseId", "courseId"})
    private int courseID;

    @SerializedName(value = "batchName", alternate = {"BatchName", "Batch_Name", "batch_Name"})
    private String batchName;

    @SerializedName(value = "startDate", alternate = {"StartDate", "start_Date", "Start_Date"})
    private String startDate;

    @SerializedName(value = "endDate", alternate = {"EndDate", "end_Date", "End_Date"})
    private String endDate;

    @SerializedName(value = "lastAction", alternate = {"LastAction", "last_Action", "Last_Action"})
    private String lastAction;

    public Batch(int batchID, int courseID, String batchName,
                 String startDate, String endDate, String lastAction) {
        this.batchID = batchID;
        this.courseID = courseID;
        this.batchName = batchName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.lastAction = lastAction;
    }

    public int getBatchID() {
        return batchID;
    }

    public int getCourseID() {
        return courseID;
    }

    public String getBatchName() {
        return batchName;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getLastAction() {
        return lastAction;
    }
}