package com.example.androidproject.model.dashboard;

import com.google.gson.annotations.SerializedName;

public class RecentEntry {
    @SerializedName("studentName")
    private String studentName;

    @SerializedName("courseName")
    private String courseName;

    @SerializedName("amount")
    private Integer amount; // Integer so it can be null for inquiries/admissions

    public String getStudentName() { return studentName; }
    public String getCourseName() { return courseName; }
    public Integer getAmount() { return amount; }
}