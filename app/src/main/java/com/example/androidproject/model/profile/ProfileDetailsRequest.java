package com.example.androidproject.model.profile;

import com.google.gson.annotations.SerializedName;

public class ProfileDetailsRequest {

    @SerializedName("studentID")
    private int studentID;

    @SerializedName("admissionID")
    private int admissionID;

    @SerializedName("userID")
    private int userID;

    @SerializedName("instituteID")
    private int instituteID;

    public ProfileDetailsRequest(int studentID, int admissionID, int userID, int instituteID) {
        this.studentID   = studentID;
        this.admissionID = admissionID;
        this.userID      = userID;
        this.instituteID = instituteID;
    }

    public int getStudentID()   { return studentID; }
    public int getAdmissionID() { return admissionID; }
    public int getUserID()      { return userID; }
    public int getInstituteID() { return instituteID; }
}