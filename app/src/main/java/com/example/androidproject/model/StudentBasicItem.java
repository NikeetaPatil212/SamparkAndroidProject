package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;

public class StudentBasicItem {

    @SerializedName("admissionID")
    private int admissionId;

    @SerializedName("admissionDate")
    private String admissionDate;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("mobile")
    private String mobile;

    @SerializedName("courseName")
    private String courseName;

    @SerializedName("scheme")
    private String scheme;

    @SerializedName("batchName")
    private String batchName;

    // ── Getters ───────────────────────────────────────────────────────────────
    public int    getAdmissionId()   { return admissionId; }
    public String getAdmissionDate() { return admissionDate; }
    public String getFullName()      { return fullName; }
    public String getMobile()        { return mobile; }
    public String getCourseName()    { return courseName; }
    public String getScheme()        { return scheme; }
    public String getBatchName()     { return batchName; }
}