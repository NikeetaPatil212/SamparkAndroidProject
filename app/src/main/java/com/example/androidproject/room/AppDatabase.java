package com.example.androidproject.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {
                AdmissionDetail.class,  // ← existing
                CourseEntity.class,     // ← ADD
                BatchEntity.class       // ← ADD
        },
        version = 3,                    // ← bump version from 2 to 3
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract AdmissionDetailDao feeDetailDao();
    public abstract CourseDao courseDao();
    public abstract BatchDao batchDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "admission-db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}