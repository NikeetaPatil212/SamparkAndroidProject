package com.example.androidproject.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "batches")
public class BatchEntity {
    @PrimaryKey
    public int batchId;
    public int courseId;
    public String batchName;
}