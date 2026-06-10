package com.example.androidproject.room;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.androidproject.utils.PrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class InstituteProfileRepository {

    private static final String BASE_URL = "http://160.187.87.113:8081/";

    public interface InstituteProfileCallback {
        void onSuccess();
        void onError(String error);
    }

    public static void fetchAndSave(Context context, int userID, int instituteID,
                                    InstituteProfileCallback callback) {

        OkHttpClient client = new OkHttpClient();

        JSONObject body = new JSONObject();
        try {
            body.put("userID",      userID);
            body.put("instituteID", instituteID);
        } catch (JSONException e) {
            callback.onError("JSON build error");
            return;
        }

        RequestBody requestBody = RequestBody.create(
                body.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(BASE_URL + "api/InstituteControllersV1/InstituteProfile")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    callback.onError("HTTP " + response.code());
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    Log.d("InstituteAPI", "Response: " + responseBody); // ← ADD THIS

                    JSONObject json     = new JSONObject(responseBody);

                    if (!json.optBoolean("isSuccess", false)) {
                        callback.onError(json.optString("message", "Unknown error"));
                        return;
                    }

                    JSONObject details = json.getJSONObject("instituteDetails");

                    PrefManager.getInstance(context).saveInstituteProfile(
                            details.optString("instituteName", ""),
                            details.optString("mobile",        ""),
                            details.optString("alternate",     ""),
                            details.optString("email",         ""),
                            details.optString("address1",      ""),
                            details.optString("address2",      "")
                    );

                    callback.onSuccess();

                } catch (JSONException | IOException e) {
                    callback.onError("Parse error: " + e.getMessage());
                }
            }

        });
    }
}