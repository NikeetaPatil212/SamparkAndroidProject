package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class InstituteListResponse {

    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("message")
    private String message;

    @SerializedName("instituteList")
    private List<InstituteItem> instituteList;

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public List<InstituteItem> getInstituteList() {
        return instituteList;
    }

    // =========================================================
    // INSTITUTE ITEM
    // =========================================================

    public static class InstituteItem {

        @SerializedName("instituteID")
        private int instituteID;

        @SerializedName("userID")
        private int userID;

        @SerializedName("instituteName")
        private String instituteName;

        @SerializedName("ownerName")
        private String ownerName;

        @SerializedName("mobile")
        private String mobile;

        @SerializedName("district")
        private String district;

        @SerializedName("expiryDate")
        private String expiryDate;

        @SerializedName("licenseType")
        private String licenseType;

        @SerializedName("finalAmount")
        private double finalAmount;

        @SerializedName("amountPaid")
        private double amountPaid;

        @SerializedName("status")
        private String status;

        // =====================================================
        // GETTERS
        // =====================================================

        public int getInstituteID() {
            return instituteID;
        }

        public int getUserID() {
            return userID;
        }

        public String getInstituteName() {
            return instituteName;
        }

        public String getOwnerName() {
            return ownerName;
        }

        public String getMobile() {
            return mobile;
        }

        public String getDistrict() {
            return district;
        }

        public String getExpiryDate() {
            return expiryDate;
        }

        public String getLicenseType() {
            return licenseType;
        }

        public double getFinalAmount() {
            return finalAmount;
        }

        public double getAmountPaid() {
            return amountPaid;
        }

        public String getStatus() {
            return status;
        }
    }
}