package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;

public class InstituteItem {

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

    // Getters

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

    // Setters

    public void setInstituteID(int instituteID) {
        this.instituteID = instituteID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void setInstituteName(String instituteName) {
        this.instituteName = instituteName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public void setFinalAmount(double finalAmount) {
        this.finalAmount = finalAmount;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}