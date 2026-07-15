package com.example.androidproject.model.template;


import com.google.gson.annotations.SerializedName;

public class StudentInfo {

    @SerializedName("studentID")
    public int studentID;

    @SerializedName("admissionID")
    public int admissionID;

    @SerializedName("studentName")
    public String studentName;

    @SerializedName("mobile")
    public String mobile;

    @SerializedName("address")
    public String address;

    @SerializedName("courseName")
    public String courseName;

    @SerializedName("batchName")
    public String batchName;

    @SerializedName("totalFees")
    public double totalFees;

    @SerializedName("paidFees")
    public double paidFees;

    @SerializedName("remainingFees")
    public double remainingFees;
}