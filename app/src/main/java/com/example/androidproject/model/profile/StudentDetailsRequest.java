package com.example.androidproject.model.profile;

import com.google.gson.annotations.SerializedName;

public class StudentDetailsRequest {

    @SerializedName("studentID")
    private int studentID;

    @SerializedName("userID")
    private int userID;

    @SerializedName("instituteID")
    private int instituteID;

    public StudentDetailsRequest(int studentID, int userID, int instituteID) {
        this.studentID = studentID;
        this.userID = userID;
        this.instituteID = instituteID;
    }
}
