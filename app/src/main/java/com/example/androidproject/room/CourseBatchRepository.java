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
import com.google.gson.Gson;

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
                                    CourseEntity e  = new CourseEntity();
                                    e.courseId      = c.getCouseID();
                                    e.courseName    = c.getCouse_Name();
                                    e.fees          = Double.parseDouble(c.getFees());          // add getter in Course model
                                    e.scheme        = c.getScheme();        // add getter in Course model
                                    e.certificate   = c.getCertificate();   // add getter in Course model
                                    e.duration      = c.getDuration();      // add getter in Course model
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
                        if (onDone != null) mainHandler.post(onDone);
                    }
                });
    }

    // ── Called when user selects a course, and after any save/update —
    //    ALWAYS hits the API and replaces the cached rows for that course.
    //    This is the method that must run after a save/edit, not
    //    getBatchesByCourse() (which will skip the network call if the
    //    cache already has rows — see note there). ─────────────────────
    public void fetchAndCacheBatches(int courseId, Runnable onDone) {
        String userId      = PrefManager.getInstance(appContext).getUserId();
        String instituteId = PrefManager.getInstance(appContext).getInstituteId();
        Log.d("123456789", "fetchAndCacheBatches: " );
        BatchRequest request = new BatchRequest(
                Integer.parseInt(userId), Integer.parseInt(instituteId), courseId);

        RetrofitClient.getApiService().getBatch(request)
                .enqueue(new Callback<BatchResponse>() {
                    @Override
                    public void onResponse(Call<BatchResponse> call,
                                           Response<BatchResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getBatchList() != null) {
                            Log.d("BATCH_RAW_JSON", new com.google.gson.Gson().toJson(response.body()));

                            // Add this ONE line inside onResponse, right after the null check:
                            Log.d("BATCH_RAW", new Gson().toJson(response.body()));
                            List<Batch> batches = response.body().getBatchList();
                            executor.execute(() -> {
                                // Clear stale rows for THIS course only — not a
                                // global clearAll(), so other courses' cached
                                // batches are untouched.
                                batchDao.deleteByCourse(courseId);

                                List<BatchEntity> entities = new ArrayList<>();
                                for (Batch b : batches) {
                                    BatchEntity e = new BatchEntity();
                                    e.batchId   = b.getBatchID();
                                    e.courseId  = courseId;
                                    e.batchName = b.getBatchName();
                                    e.startDate = b.getStartDate(); // ← was misqsing
                                    e.endDate   = b.getEndDate();   // ← was missing
                                    entities.add(e);
                                }
                                batchDao.insertAll(entities);
                                Log.d("CourseBatchRepo", "Batches cached for course "
                                        + courseId + ": " + entities.size());
                                if (onDone != null) mainHandler.post(onDone);
                            });
                        } else {
                            // Still surface completion so the UI doesn't spin
                            // forever waiting for a callback that never comes.
                            if (onDone != null) mainHandler.post(onDone);
                        }
                    }
                    @Override
                    public void onFailure(Call<BatchResponse> call, Throwable t) {
                        Log.e("CourseBatchRepo", "Batch fetch failed: " + t.getMessage());
                        if (onDone != null) mainHandler.post(onDone);
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

    // NOTE: this only calls the network if the cache is EMPTY. It is meant
    // for "first time opening this course" convenience, NOT for refreshing
    // after a save/update — call fetchAndCacheBatches() explicitly for that,
    // then read via this method (it'll just return the now-fresh cache).
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