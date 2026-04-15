package com.example.androidproject.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "admission_details")
public class AdmissionDetail {

    @PrimaryKey(autoGenerate = true)
    private int id;              // unique row ID

    private int studentId;       // link details to a student
    private String courseName;
    private String courseId;
    private String batchName;
    private int totalFee;
    private int paidFee;
    private int remainingFee;
    private String batchId;

    // ✅ No-arg constructor (needed for Room and for new AdmissionDetail())
    public AdmissionDetail() {
    }

    // Parameterized constructor
    public AdmissionDetail(int studentId, String courseName, String courseId,
                           String batchName, int totalFee, int paidFee,
                           int remainingFee, String batchId) {
        this.studentId = studentId;
        this.courseName = courseName;
        this.courseId = courseId;
        this.batchName = batchName;
        this.totalFee = totalFee;
        this.paidFee = paidFee;
        this.remainingFee = remainingFee;
        this.batchId = batchId;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }

    public int getTotalFee() { return totalFee; }
    public void setTotalFee(int totalFee) { this.totalFee = totalFee; }

    public int getPaidFee() { return paidFee; }
    public void setPaidFee(int paidFee) { this.paidFee = paidFee; }

    public int getRemainingFee() { return remainingFee; }
    public void setRemainingFee(int remainingFee) { this.remainingFee = remainingFee; }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
}
