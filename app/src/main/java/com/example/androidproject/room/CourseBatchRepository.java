package com.example.androidproject.room;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.room.Room;

import com.example.androidproject.model.Batch;
import com.example.androidproject.model.BatchRequest;
import com.example.androidproject.model.BatchResponse;
import com.example.androidproject.model.Course;
import com.example.androidproject.model.GetCoursesRequest;
import com.example.androidproject.model.GetCoursesResponse;
import com.example.androidproject.utils.PrefManager;
import com.example.androidproject.utils.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseBatchRepository {

    private static volatile CourseBatchRepository INSTANCE;

    public static CourseBatchRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (CourseBatchRepository.class) {
                if (INSTANCE == null)
                    INSTANCE = new CourseBatchRepository(context.getApplicationContext());
            }
        }
        return INSTANCE;
    }

    private final Context         appContext;
    private final CourseDao       courseDao;
    private final BatchDao        batchDao;
    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private CourseBatchRepository(Context context) {
        AppDatabase db = Room.databaseBuilder(context, AppDatabase.class, "admission-db")
                .fallbackToDestructiveMigration()
                .build();
        courseDao = db.courseDao();
        batchDao  = db.batchDao();
        appContext = context;
    }

    // ── Called once at login — fetch & cache all courses ─────────────────────
    public void fetchAndCacheCourses(Runnable onDone) {
        String userId      = PrefManager.getInstance(appContext).getUserId();
        String instituteId = PrefManager.getInstance(appContext).getInstituteId();

        GetCoursesRequest request = new GetCoursesRequest(
                Integer.parseInt(userId), Integer.parseInt(instituteId));

        RetrofitClient.getApiService().getCourses(request)
                .enqueue(new Callback<GetCoursesResponse>() {
                    @Override
                    public void onResponse(Call<GetCoursesResponse> call,
                                           Response<GetCoursesResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Course> courses = response.body().getCouseList();
                            executor.execute(() -> {
                                courseDao.clearAll();
                                List<CourseEntity> entities = new ArrayList<>();
                                for (Course c : courses) {
                                    CourseEntity e = new CourseEntity();
                                    e.courseId   = c.getCouseID();
                                    e.courseName = c.getCouse_Name();
                                    entities.add(e);
                                }
                                courseDao.insertAll(entities);
                                Log.d("CourseBatchRepo", "Courses cached: " + entities.size());
                                if (onDone != null) mainHandler.post(onDone);
                            });
                        }
                    }
                    @Override
                    public void onFailure(Call<GetCoursesResponse> call, Throwable t) {
                        Log.e("CourseBatchRepo", "Course fetch failed: " + t.getMessage());
                    }
                });
    }

    // ── Called when user selects a course — fetch & cache batches ────────────
    public void fetchAndCacheBatches(int courseId, Runnable onDone) {
        String userId      = PrefManager.getInstance(appContext).getUserId();
        String instituteId = PrefManager.getInstance(appContext).getInstituteId();

        BatchRequest request = new BatchRequest(
                Integer.parseInt(userId), Integer.parseInt(instituteId), courseId);

        RetrofitClient.getApiService().getBatch(request)
                .enqueue(new Callback<BatchResponse>() {
                    @Override
                    public void onResponse(Call<BatchResponse> call,
                                           Response<BatchResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Batch> batches = response.body().getBatchList();
                            executor.execute(() -> {
                                List<BatchEntity> entities = new ArrayList<>();
                                for (Batch b : batches) {
                                    BatchEntity e = new BatchEntity();
                                    e.batchId   = b.getBatchID();
                                    e.courseId  = courseId;
                                    e.batchName = b.getBatchName();
                                    entities.add(e);
                                }
                                batchDao.insertAll(entities);
                                Log.d("CourseBatchRepo", "Batches cached for course "
                                        + courseId + ": " + entities.size());
                                if (onDone != null) mainHandler.post(onDone);
                            });
                        }
                    }
                    @Override
                    public void onFailure(Call<BatchResponse> call, Throwable t) {
                        Log.e("CourseBatchRepo", "Batch fetch failed: " + t.getMessage());
                    }
                });
    }

    // ── Read from DB — use these in all activities instead of API calls ───────
    public void getCourses(Consumer<List<CourseEntity>> callback) {
        executor.execute(() -> {
            List<CourseEntity> list = courseDao.getAll();
            mainHandler.post(() -> callback.accept(list));
        });
    }

    public void getBatchesByCourse(int courseId, Consumer<List<BatchEntity>> callback) {
        executor.execute(() -> {
            List<BatchEntity> list = batchDao.getByCourse(courseId);
            if (list == null || list.isEmpty()) {
                // Not cached yet — fetch from API then return
                fetchAndCacheBatches(courseId, () ->
                        executor.execute(() -> {
                            List<BatchEntity> fresh = batchDao.getByCourse(courseId);
                            mainHandler.post(() -> callback.accept(fresh));
                        }));
            } else {
                mainHandler.post(() -> callback.accept(list));
            }
        });
    }

    public interface Consumer<T> {
        void accept(T value);
    }
}