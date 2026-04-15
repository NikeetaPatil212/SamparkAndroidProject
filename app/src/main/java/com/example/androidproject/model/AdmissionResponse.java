package com.example.androidproject.model;

import java.util.List;
public class AdmissionResponse {

    private boolean isSuccess;
    private String message;
    private int admissionID;

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public int getAdmissionID() {
        return admissionID;
    }
}