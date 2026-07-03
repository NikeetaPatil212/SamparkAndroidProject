package com.example.androidproject.model.summary;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class AdmissionReportResponse {

    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("message")
    private String message;

    @SerializedName("studentList")
    private List<AdmissionItem> studentList;

    public boolean isSuccess()              { return isSuccess;    }
    public String  getMessage()             { return message;      }
    public List<AdmissionItem> getStudentList() { return studentList; }
}
