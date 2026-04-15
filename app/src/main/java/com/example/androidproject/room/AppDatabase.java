package com.example.androidproject.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {AdmissionDetail.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AdmissionDetailDao feeDetailDao();

}

