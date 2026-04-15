package com.example.androidproject.utils;

import android.app.Activity;
import android.view.View;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiUrls {

    // Base URL
    public static final String BASE_URL = "http://160.25.62.225:8081/api/InstituteControllersV1/";


        private Activity activity;
        private View loader;

        public ApiUrls(Activity activity, View loader) {
            this.activity = activity;
            this.loader = loader;
        }

        public <T> void call(Call<T> call, Callback<T> callback) {

            // ✅ Show loader
            if (loader != null) {
                loader.setVisibility(View.VISIBLE);
            }

            call.enqueue(new Callback<T>() {
                @Override
                public void onResponse(Call<T> call, Response<T> response) {

                    // ✅ Hide loader
                    if (loader != null) {
                        loader.setVisibility(View.GONE);
                    }

                    callback.onResponse(call, response);
                }

                @Override
                public void onFailure(Call<T> call, Throwable t) {

                    // ✅ Hide loader
                    if (loader != null) {
                        loader.setVisibility(View.GONE);
                    }

                    callback.onFailure(call, t);
                }
            });
        }

}

