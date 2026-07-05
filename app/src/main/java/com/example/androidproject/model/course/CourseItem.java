package com.example.androidproject.model.course;

import com.google.gson.annotations.SerializedName;

public class CourseItem {
    @SerializedName("courseID")   public int    courseID;
    @SerializedName("courseName") public String courseName;
    @SerializedName("fees")       public double fees;
    @SerializedName("scheme")     public String scheme;
    @SerializedName("certificate")public int    certificate;
    @SerializedName("duration")   public int    duration;
}