package com.example.androidproject.model;

public class Batch {
    private int batchID;
    private int courseID;
    private String batchName;
    private String startDate;
    private String endDate;
    private String lastAction;

    public Batch(int batchID, int courseID,String batchName,String startDate,String endDate,String lastAction ) {
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
