package com.example.androidproject.model.template;

import com.google.gson.annotations.SerializedName;

public class InstituteProfileResponse {
    @SerializedName("isSuccess")        public boolean isSuccess;
    @SerializedName("message")          public String message;
    @SerializedName("instituteDetails") public InstituteDetails instituteDetails;

    public static class InstituteDetails {
        @SerializedName("instituteName") public String instituteName;
        @SerializedName("mobile")        public String mobile;
        @SerializedName("alternate")     public String alternate;
        @SerializedName("email")         public String email;
        @SerializedName("address1")      public String address1;
        @SerializedName("address2")      public String address2;
    }
}