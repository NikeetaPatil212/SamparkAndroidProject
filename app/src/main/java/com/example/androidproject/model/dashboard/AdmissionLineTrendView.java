package com.example.androidproject.model.dashboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Line + gradient-area chart — one point per month, matches the
 * "Admissions Trend" card in the dashboard mock.
 */
public class AdmissionLineTrendView extends View {

    public static class Entry {
        public final String monthLabel;
        public final int value;

        public Entry(String monthLabel, int value) {
            this.monthLabel = monthLabel;
            this.value = value;
        }
    }

    private List<Entry> entries = new ArrayList<>();

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint monthLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int LINE_COLOR = Color.parseColor("#43A047");
    private static final int GRID_COLOR = Color.parseColor("#E0E0E0");

    public AdmissionLineTrendView(Context context) {
        super(context);
        init();
    }

    public AdmissionLineTrendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint.setColor(LINE_COLOR);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp(3));
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        dotPaint.setColor(LINE_COLOR);
        dotPaint.setStyle(Paint.Style.FILL);

        dotStrokePaint.setColor(Color.WHITE);
        dotStrokePaint.setStyle(Paint.Style.FILL);

        fillPaint.setStyle(Paint.Style.FILL);

        gridPaint.setColor(GRID_COLOR);
        gridPaint.setStrokeWidth(dp(1));
        gridPaint.setPathEffect(new android.graphics.DashPathEffect(new float[]{dp(4), dp(4)}, 0));

        axisLabelPaint.setColor(Color.parseColor("#9E9E9E"));
        axisLabelPaint.setTextSize(dp(11));
        axisLabelPaint.setTextAlign(Paint.Align.RIGHT);

        monthLabelPaint.setColor(Color.parseColor("#616161"));
        monthLabelPaint.setTextSize(dp(12));
    }

    public void setData(List<Entry> entries) {
        this.entries = entries != null ? entries : new ArrayList<>();
        requestLayout();
        invalidate();
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) dp(220);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (entries.isEmpty()) return;

        float paddingLeft = dp(28);
        float paddingRight = dp(12);
        float paddingTop = dp(16);
        float paddingBottom = dp(28);

        float chartLeft = paddingLeft;
        float chartRight = getWidth() - paddingRight;
        float chartTop = paddingTop;
        float chartBottom = getHeight() - paddingBottom;
        float chartHeight = chartBottom - chartTop;
        float chartWidth = chartRight - chartLeft;

        int maxValue = 1;
        for (Entry e : entries) maxValue = Math.max(maxValue, e.value);
        int axisMax = niceCeiling(maxValue);

        // Horizontal grid lines + y labels
        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            float y = chartBottom - chartHeight * i / gridLines;
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint);
            int value = axisMax * i / gridLines;
            canvas.drawText(String.valueOf(value), chartLeft - dp(6), y + dp(4), axisLabelPaint);
        }

        int count = entries.size();
        float[] xs = new float[count];
        float[] ys = new float[count];

        for (int i = 0; i < count; i++) {
            float x = count == 1
                    ? chartLeft + chartWidth / 2f
                    : chartLeft + chartWidth * i / (count - 1);
            float ratio = (float) entries.get(i).value / axisMax;
            float y = chartBottom - chartHeight * ratio;
            xs[i] = x;
            ys[i] = y;
        }

        // Gradient fill under the line
        if (count > 1) {
            Path fillPath = new Path();
            fillPath.moveTo(xs[0], chartBottom);
            for (int i = 0; i < count; i++) fillPath.lineTo(xs[i], ys[i]);
            fillPath.lineTo(xs[count - 1], chartBottom);
            fillPath.close();

            fillPaint.setShader(new LinearGradient(
                    0, chartTop, 0, chartBottom,
                    Color.parseColor("#5543A047"), Color.parseColor("#0043A047"),
                    Shader.TileMode.CLAMP));
            canvas.drawPath(fillPath, fillPaint);
        }

        // Line
        if (count > 1) {
            Path linePath = new Path();
            linePath.moveTo(xs[0], ys[0]);
            for (int i = 1; i < count; i++) linePath.lineTo(xs[i], ys[i]);
            canvas.drawPath(linePath, linePaint);
        }

        // Dots + value labels + month labels
        for (int i = 0; i < count; i++) {
            canvas.drawCircle(xs[i], ys[i], dp(6), dotStrokePaint);
            canvas.drawCircle(xs[i], ys[i], dp(4), dotPaint);

            // Show month label only for first/last (avoids crowding, matches mock)
            if (i == 0 || i == count - 1) {
                float textX = (i == 0) ? xs[i] : xs[i];
                monthLabelPaint.setTextAlign(i == 0 ? Paint.Align.LEFT : Paint.Align.RIGHT);
                canvas.drawText(entries.get(i).monthLabel, textX, chartBottom + dp(20), monthLabelPaint);
            }
        }
    }

    private int niceCeiling(int value) {
        if (value <= 4) return 4;
        int magnitude = 1;
        int v = value;
        while (v >= 10) {
            v /= 10;
            magnitude *= 10;
        }
        return (v + 1) * magnitude;
    }
}