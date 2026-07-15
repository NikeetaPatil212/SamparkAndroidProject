package com.example.androidproject.model;


import com.google.gson.annotations.SerializedName;

public class CancelAdmissionRequest {

    @SerializedName("studentID")
    private int studentID;

    @SerializedName("admissionID")
    private int admissionID;

    @SerializedName("isRefund")
    private boolean isRefund;

    @SerializedName("userID")
    private int userID;

    @SerializedName("instituteID")
    private int instituteID;

    @SerializedName("operatorID")
    private int operatorID;

    @SerializedName("transactionDate")
    private String transactionDate;

    // Empty Constructor
    public CancelAdmissionRequest() {
    }

    // Parameterized Constructor
    public CancelAdmissionRequest(int studentID, int admissionID, boolean isRefund,
                                  int userID, int instituteID, int operatorID,
                                  String transactionDate) {
        this.studentID = studentID;
        this.admissionID = admissionID;
        this.isRefund = isRefund;
        this.userID = userID;
        this.instituteID = instituteID;
        this.operatorID = operatorID;
        this.transactionDate = transactionDate;
    }

    // Getters and Setters

    public int getStudentID() {
        return studentID;
    }

    public void setStudentID(int studentID) {
        this.studentID = studentID;
    }

    public int getAdmissionID() {
        return admissionID;
    }

    public void setAdmissionID(int admissionID) {
        this.admissionID = admissionID;
    }

    public boolean isRefund() {
        return isRefund;
    }

    public void setRefund(boolean refund) {
        isRefund = refund;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getInstituteID() {
        return instituteID;
    }

    public void setInstituteID(int instituteID) {
        this.instituteID = instituteID;
    }

    public int getOperatorID() {
        return operatorID;
    }

    public void setOperatorID(int operatorID) {
        this.operatorID = operatorID;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }
}