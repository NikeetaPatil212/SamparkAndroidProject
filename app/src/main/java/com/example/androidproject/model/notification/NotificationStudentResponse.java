package com.example.androidproject.model.notification;

import com.google.gson.annotations.SerializedName;

import java.util.List;

// NotificationStudentResponse.java
public class NotificationStudentResponse {
    @SerializedName("isSuccess")    private boolean isSuccess;
    @SerializedName("message")      private String  message;
    @SerializedName("studentList")  private List<NotificationStudent> studentList;

    public boolean isSuccess()                          { return isSuccess; }
    public String getMessage()                          { return message; }
    public List<NotificationStudent> getStudentList()   { return studentList; }
}
