package com.example.androidproject.model;

import java.util.List;

public class GetAdmissionResponse {
    private String message;
    private List<AdmissionDetails> details;

    // Getter Setter


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<AdmissionDetails> getDetails() {
        return details;
    }

    public void setDetails(List<AdmissionDetails> details) {
        this.details = details;
    }
}