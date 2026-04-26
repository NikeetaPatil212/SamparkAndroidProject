package com.example.androidproject.model.profile;


import com.google.gson.annotations.SerializedName;

public class EditProfileRequest {

    @SerializedName("f_name")
    private String fName;

    @SerializedName("m_name")
    private String mName;

    @SerializedName("l_name")
    private String lName;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("mobile")
    private String mobile;

    @SerializedName("alternateNo")
    private String alternateNo;

    @SerializedName("emailID")
    private String emailID;

    @SerializedName("address")
    private String address;

    @SerializedName("dob")
    private String dob;

    @SerializedName("gender")
    private String gender;

    @SerializedName("imgurl")
    private String imgurl;

    @SerializedName("studentID")
    private int studentID;

    @SerializedName("userID")
    private int userID;

    @SerializedName("instituteID")
    private int instituteID;

    @SerializedName("operatorID")
    private int operatorID;

    public EditProfileRequest(String fName, String mName, String lName,
                              String fullName, String mobile, String alternateNo,
                              String emailID, String address, String dob,
                              String gender, String imgurl, int studentID,
                              int userID, int instituteID, int operatorID) {

        this.fName = fName;
        this.mName = mName;
        this.lName = lName;
        this.fullName = fullName;
        this.mobile = mobile;
        this.alternateNo = alternateNo;
        this.emailID = emailID;
        this.address = address;
        this.dob = dob;
        this.gender = gender;
        this.imgurl = imgurl;
        this.studentID = studentID;
        this.userID = userID;
        this.instituteID = instituteID;
        this.operatorID = operatorID;
    }
}