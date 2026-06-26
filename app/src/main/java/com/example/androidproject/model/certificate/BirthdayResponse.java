package com.example.androidproject.model.certificate;

import com.google.gson.annotations.SerializedName;

import java.util.List;

// BirthdayResponse.java
public class BirthdayResponse {
    @SerializedName("isSuccess")  private boolean isSuccess;
    @SerializedName("message")    private String  message;
    @SerializedName("studentList") private List<BirthdayStudent> studentList;

    public boolean isSuccess()                   { return isSuccess; }
    public String getMessage()                   { return message; }
    public List<BirthdayStudent> getStudentList(){ return studentList; }

    public static class BirthdayStudent {
        @SerializedName("studentID")   private int    studentID;
        @SerializedName("studentName") private String studentName;
        @SerializedName("mobile")      private String mobile;
        @SerializedName("location")    private String location;
        @SerializedName("dob")         private String dob;

        public int    getStudentID()   { return studentID; }
        public String getStudentName() { return studentName; }
        public String getMobile()      { return mobile; }
        public String getDob()         { return dob; }
    }
}