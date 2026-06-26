package com.example.androidproject.model.certificate;


import com.google.gson.annotations.SerializedName;

public class CertificateRequest {

    @SerializedName("userID")      public int userID;
    @SerializedName("instituteID") public int instituteID;
    @SerializedName("courseID")    public int courseID;

    public CertificateRequest(int userID, int instituteID, int courseID) {
        this.userID      = userID;
        this.instituteID = instituteID;
        this.courseID    = courseID;
    }
}
