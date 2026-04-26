package com.example.androidproject.model;


import com.google.gson.annotations.SerializedName;

public class StudentBasicRequest {

        @SerializedName("userID")
        private int userID;

        @SerializedName("instituteID")
        private int instituteID;

        @SerializedName("courseID")
        private int courseID;

        @SerializedName("batchID")
        private int batchID;

        public StudentBasicRequest(int userID, int instituteID, int courseID, int batchID) {
            this.userID = userID;
            this.instituteID = instituteID;
            this.courseID = courseID;
            this.batchID = batchID;
        }

        public int getUserID() {
            return userID;
        }

        public void setUserID(int userID) {
            this.userID = userID;
        }

        public int getInstituteID() {
            return instituteID;
        }

        public void setInstituteID(int instituteID) {
            this.instituteID = instituteID;
        }

        public int getCourseID() {
            return courseID;
        }

        public void setCourseID(int courseID) {
            this.courseID = courseID;
        }

        public int getBatchID() {
            return batchID;
        }

        public void setBatchID(int batchID) {
            this.batchID = batchID;
        }
    }

