package com.example.androidproject.model.template;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TemplateRepository {

    private static final String TAG          = "TemplateRepository";
    private static final long   CACHE_TTL_MS = 24 * 60 * 60 * 1000L;

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static volatile TemplateRepository INSTANCE;

    public static TemplateRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TemplateRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TemplateRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    // ── Fields ────────────────────────────────────────────────────────────────
    private final Context         appContext;
    private final TemplateDao     dao;        // ← comes from TemplateAppDatabase only
    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    private TemplateRepository(Context context) {
        this.appContext = context;
        // ✅ Always uses TemplateAppDatabase — never AppDatabase
        dao = TemplateAppDatabase.getInstance(context).templateDao();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Call from LoginActivity after successful login
    // ─────────────────────────────────────────────────────────────────────────
    public void fetchAndCache(TemplateCallback callback) {
        fetchFromApi(callback);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Call from any Activity/Fragment — reads from Room, no network call
    // ─────────────────────────────────────────────────────────────────────────
    public void getTemplates(TemplateCallback callback) {
        executor.execute(() -> {
            List<TemplateEntity> cached = dao.getAllActiveTemplates();
            if (cached == null || cached.isEmpty()) {
                fetchFromApi(callback);
            } else {
                mainHandler.post(() -> callback.onSuccess(cached));
                if (isCacheStale()) fetchFromApi(null);
            }
        });
    }

    public void getTemplateByCategory(String category, SingleTemplateCallback callback) {
        executor.execute(() -> {
            TemplateEntity entity = dao.getTemplateByCategory(category);
            if (entity == null) {
                // DB empty — fetch all then retry
                fetchFromApi(new TemplateCallback() {
                    @Override
                    public void onSuccess(List<TemplateEntity> templates) {
                        executor.execute(() -> {
                            TemplateEntity fresh = dao.getTemplateByCategory(category);
                            mainHandler.post(() -> {
                                if (fresh != null) callback.onSuccess(fresh);
                                else callback.onError("Template not found: " + category);
                            });
                        });
                    }
                    @Override
                    public void onError(String error) {
                        mainHandler.post(() -> callback.onError(error));
                    }
                });
            } else {
                mainHandler.post(() -> callback.onSuccess(entity));
                if (isCacheStale()) fetchFromApi(null);
            }
        });
    }

    public static String fillTemplate(String templateText, Map<String, String> values) {
        if (templateText == null) return "";
        String result = templateText;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace(
                    "{" + entry.getKey() + "}",
                    entry.getValue() != null ? entry.getValue() : ""
            );
        }
        return result;
    }

    public void clearCache() {
        executor.execute(dao::clearAll);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Private
    // ─────────────────────────────────────────────────────────────────────────
    private boolean isCacheStale() {
        long oldest = dao.getOldestCacheTimestamp();
        return oldest == 0 || (System.currentTimeMillis() - oldest) > CACHE_TTL_MS;
    }

    private void fetchFromApi(TemplateCallback callback) {
        String userId      = PrefManager.getInstance(appContext).getUserId();
        String instituteId = PrefManager.getInstance(appContext).getInstituteId();

        if (userId == null || userId.isEmpty() || instituteId == null || instituteId.isEmpty()) {
            if (callback != null)
                mainHandler.post(() -> callback.onError("User not logged in"));
            return;
        }

        TemplateRequest request = new TemplateRequest(
                Integer.parseInt(userId),
                Integer.parseInt(instituteId)
        );

        RetrofitClient.getApiService()
                .getTemplates(request)
                .enqueue(new Callback<TemplateResponse>() {

                    @Override
                    public void onResponse(Call<TemplateResponse> call,
                                           Response<TemplateResponse> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()
                                && response.body().getTemplateList() != null) {

                            List<TemplateEntity> entities =
                                    mapToEntities(response.body().getTemplateList());

                            executor.execute(() -> {
                                dao.clearAll();
                                dao.insertAll(entities);
                                Log.d(TAG, "Templates cached: " + entities.size());
                                if (callback != null)
                                    mainHandler.post(() -> callback.onSuccess(entities));
                            });

                        } else {
                            String err = "Template API error: " + response.code();
                            Log.e(TAG, err);
                            if (callback != null)
                                mainHandler.post(() -> callback.onError(err));
                        }
                    }

                    @Override
                    public void onFailure(Call<TemplateResponse> call, Throwable t) {
                        Log.e(TAG, "Template network error: " + t.getMessage());
                        if (callback != null)
                            mainHandler.post(() -> callback.onError(t.getMessage()));
                    }
                });
    }

    private List<TemplateEntity> mapToEntities(List<TemplateResponse.TemplateModel> models) {
        long now = System.currentTimeMillis();
        List<TemplateEntity> list = new ArrayList<>();
        for (TemplateResponse.TemplateModel m : models) {
            TemplateEntity e = new TemplateEntity();
            e.templateID  = m.templateID;
            e.category    = m.category;
            e.accessToken = m.accessToken;
            e.instanceID  = m.instanceID;
            e.wa_MR       = m.wa_MR;
            e.wa_HI       = m.wa_HI;
            e.wa_EN       = m.wa_EN;
            e.sms_MR      = m.sms_MR;
            e.sms_HI      = m.sms_HI;
            e.sms_EN      = m.sms_EN;
            e.userID      = m.userID;
            e.instituteID = m.instituteID;
            e.isActive    = m.isActive;
            e.cachedAt    = now;
            list.add(e);
        }
        return list;
    }

    // ── Callback interfaces ───────────────────────────────────────────────────
    public interface TemplateCallback {
        void onSuccess(List<TemplateEntity> templates);
        void onError(String error);
    }

    public interface SingleTemplateCallback {
        void onSuccess(TemplateEntity template);
        void onError(String error);
    }
}