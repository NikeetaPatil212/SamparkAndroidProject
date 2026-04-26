package com.example.androidproject.model.profile;

import com.google.gson.annotations.SerializedName;

public class StudentDetailsResponse {

    @SerializedName("isSuccesss")
    private boolean isSuccesss;

    @SerializedName("message")
    private String message;

    @SerializedName("details")
    private Details details;

    public boolean isSuccesss() {
        return isSuccesss;
    }

    public String getMessage() {
        return message;
    }

    public Details getDetails() {
        return details;
    }

    public static class Details {

        @SerializedName("full_Name")
        private String fullName;

        @SerializedName("mobile")
        private String mobile;

        @SerializedName("alternateNo")
        private String alternateNo;

        @SerializedName("email")
        private String email;

        @SerializedName("address")
        private String address;

        @SerializedName("dob")
        private String dob;

        @SerializedName("gender")
        private String gender;

        @SerializedName("imgurl")
        private String imgurl;

        @SerializedName("school")
        private String school;

        public String getFullName() { return fullName; }
        public String getMobile() { return mobile; }
        public String getAlternateNo() { return alternateNo; }
        public String getEmail() { return email; }
        public String getAddress() { return address; }
        public String getDob() { return dob; }
        public String getGender() { return gender; }
        public String getImgurl() { return imgurl; }
        public String getSchool() { return school; }
    }
}