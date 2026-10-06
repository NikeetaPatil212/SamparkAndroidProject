package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;

public class AddInstituteRequest {

    @SerializedName("userID")
    private int userID;

    @SerializedName("instituteName")
    private String instituteName;

    @SerializedName("ownerName")
    private String ownerName;

    @SerializedName("mobile")
    private String mobile;

    @SerializedName("alternate")
    private String alternate;

    @SerializedName("address1")
    private String address1;

    @SerializedName("address2")
    private String address2;

    @SerializedName("email")
    private String email;

    @SerializedName("slogan")
    private String slogan;

    @SerializedName("tehshil")
    private String tehshil;

    @SerializedName("district")
    private String district;

    @SerializedName("state")
    private String state;

    @SerializedName("logoUrl")
    private String logoUrl;

    @SerializedName("lastAction")
    private String lastAction;

    @SerializedName("userName")
    private String userName;

    @SerializedName("password")
    private String password;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    // Required for default instantiation and Gson serialization
    public AddInstituteRequest() {
    }

    public AddInstituteRequest(
            int userID,
            String instituteName,
            String ownerName,
            String mobile,
            String alternate,
            String address1,
            String address2,
            String email,
            String slogan,
            String tehshil,
            String district,
            String state,
            String logoUrl,
            String lastAction,
            String userName,
            String password) {

        this.userID = userID;
        this.instituteName = instituteName;
        this.ownerName = ownerName;
        this.mobile = mobile;
        this.alternate = alternate;
        this.address1 = address1;
        this.address2 = address2;
        this.email = email;
        this.slogan = slogan;
        this.tehshil = tehshil;
        this.district = district;
        this.state = state;
        this.logoUrl = logoUrl;
        this.lastAction = lastAction;
        this.userName = userName;
        this.password = password;
    }

    // ============================================================
    // GETTERS & SETTERS
    // ============================================================

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getInstituteName() {
        return instituteName;
    }

    public void setInstituteName(String instituteName) {
        this.instituteName = instituteName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAlternate() {
        return alternate;
    }

    public void setAlternate(String alternate) {
        this.alternate = alternate;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public String getTehshil() {
        return tehshil;
    }

    public void setTehshil(String tehshil) {
        this.tehshil = tehshil;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getLastAction() {
        return lastAction;
    }

    public void setLastAction(String lastAction) {
        this.lastAction = lastAction;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}