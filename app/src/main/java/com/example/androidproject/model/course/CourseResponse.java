package com.example.androidproject.model.course;

import com.google.gson.annotations.SerializedName;

public class CourseResponse {
    @SerializedName("isSuccess") public boolean isSuccess;
    @SerializedName("message")   public String  message;
}