package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FeeReceiptRequest {

    @SerializedName("userID")
    private int userId;

    @SerializedName("instituteID")
    private int instituteId;

    @SerializedName("studentID")
    private int studentId;

    @SerializedName("admissionID")
    private int admissionId;

    public FeeReceiptRequest(int userId, int instituteId, int studentId, int admissionId) {
        this.userId = userId;
        this.instituteId = instituteId;
        this.studentId = studentId;
        this.admissionId = admissionId;
    }

    // Getters
    public int getUserId()      { return userId; }
    public int getInstituteId() { return instituteId; }
    public int getStudentId()   { return studentId; }
    public int getAdmissionId() { return admissionId; }
}







