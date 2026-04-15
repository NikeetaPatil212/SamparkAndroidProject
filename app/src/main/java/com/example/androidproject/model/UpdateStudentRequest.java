package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;

public class UpdateStudentRequest {

    @SerializedName("f_name")
    public String fName;

    @SerializedName("m_name")
    public String mName;

    @SerializedName("l_name")
    public String lName;

    @SerializedName("full_name")
    public String fullName;

    @SerializedName("mobile")
    public String mobile;

    @SerializedName("alternateNo")
    public String alternateNo;

    @SerializedName("emailID")
    public String emailID;

    @SerializedName("address")
    public String address;

    @SerializedName("imgurl")
    public String imgurl;

    @SerializedName("inquiry_date")
    public String inquiryDate;

    @SerializedName("studentID")
    public int studentID;

    @SerializedName("userID")
    public int userID;

    @SerializedName("instituteID")
    public int instituteID;
}
