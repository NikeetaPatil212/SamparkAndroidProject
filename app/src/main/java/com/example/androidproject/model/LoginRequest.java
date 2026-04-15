package com.example.androidproject.model;

public class LoginRequest {

    private String userName;
    private String password;
    private int userID;
    private int instituteID;

    public LoginRequest(String userName, String password, int userID, int instituteID) {
        this.userName = userName;
        this.password = password;
        this.userID = userID;
        this.instituteID = instituteID;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public int getUserID() {
        return userID;
    }

    public int getInstituteID() {
        return instituteID;
    }
}

