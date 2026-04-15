package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;

public class AddStudentRequest {

    @SerializedName("f_name")
    public String fName;

    @SerializedName("m_name")
    public String mName;

    @SerializedName("l_name")
    public String lName;

    @SerializedName("full_name")
    public String fullName;

    public String mobile;
    public String alternateNo;
    public String emailID;
    public String address;
    public String school_name;
    public String dob;
    public String gender;

    public int userID;
    public int instituteID;
    public int operatorID;
}
