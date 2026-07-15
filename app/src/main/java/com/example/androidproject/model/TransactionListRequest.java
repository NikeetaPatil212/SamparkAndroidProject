package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;

public class TransactionListRequest {

    @SerializedName("studentID")
    public int studentID;

    @SerializedName("admissionID")
    public int admissionID;

    @SerializedName("userID")
    public int userID;

    @SerializedName("instituteID")
    public int instituteID;

    public TransactionListRequest(int studentID, int admissionID, int userID, int instituteID) {
        this.studentID   = studentID;
        this.admissionID = admissionID;
        this.userID      = userID;
        this.instituteID = instituteID;
    }
}