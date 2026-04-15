package com.example.androidproject.model;

public class CourseList {

    private int courseID;
    private String courseName;
    private String scheme;
    private int batchID;
    private String batchName;
    private int fee;
    private int paid;
    private int remaining;
    private String status;
    private String recieptNo;
    private String reminderDate;
    private String cycleDate;
    private int operatorID;

    public CourseList(int courseID,
                         String courseName,
                         String scheme,
                         int batchID,
                         String batchName,
                         int fee,
                         int paid,
                         int remaining,
                         String status,
                         String recieptNo,
                         String reminderDate,
                         String cycleDate,
                         int operatorID) {

        this.courseID = courseID;
        this.courseName = courseName;
        this.scheme = scheme;
        this.batchID = batchID;
        this.batchName = batchName;
        this.fee = fee;
        this.paid = paid;
        this.remaining = remaining;
        this.status = status;
        this.recieptNo = recieptNo;
        this.reminderDate = reminderDate;
        this.cycleDate = cycleDate;
        this.operatorID = operatorID;
    }

    public int getCourseID() {
        return courseID;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public int getBatchID() {
        return batchID;
    }

    public void setBatchID(int batchID) {
        this.batchID = batchID;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public int getFee() {
        return fee;
    }

    public void setFee(int fee) {
        this.fee = fee;
    }

    public int getPaid() {
        return paid;
    }

    public void setPaid(int paid) {
        this.paid = paid;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRecieptNo() {
        return recieptNo;
    }

    public void setRecieptNo(String recieptNo) {
        this.recieptNo = recieptNo;
    }

    public String getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(String reminderDate) {
        this.reminderDate = reminderDate;
    }

    public String getCycleDate() {
        return cycleDate;
    }

    public void setCycleDate(String cycleDate) {
        this.cycleDate = cycleDate;
    }

    public int getOperatorID() {
        return operatorID;
    }

    public void setOperatorID(int operatorID) {
        this.operatorID = operatorID;
    }
}