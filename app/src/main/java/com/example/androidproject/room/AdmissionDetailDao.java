package com.example.androidproject.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AdmissionDetailDao {
    @Insert
    void insert(AdmissionDetail detail);

    @Query("SELECT * FROM admission_details WHERE studentId = :studentId")
    List<AdmissionDetail> getDetailsForStudent(int studentId);
    @Query("DELETE FROM admission_details WHERE studentId = :studentId")
    void deleteForStudent(int studentId);
}

