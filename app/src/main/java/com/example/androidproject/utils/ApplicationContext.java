package com.example.androidproject.utils;

import android.content.Context;

public class ApplicationContext {
    public static String userId, instituteId;
    public static void init(Context context) {
        userId = PrefManager.getInstance(context).getUserId();
        instituteId = PrefManager.getInstance(context).getInstituteId();
    }
}
