package com.example.androidproject.model.dashboard;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Horizontal bar chart — one row per month, bar length proportional to the
 * "inquiries" value for that month. Matches the "Inquiries Trend (Monthly)"
 * card in the dashboard mock (single series version, since the API returns
 * one inquiries figure per month rather than a separate pending figure).
 */
public class InquiryBarTrendView extends View {

    public static class Entry {
        public final String monthLabel;
        public final int value;

        public Entry(String monthLabel, int value) {
            this.monthLabel = monthLabel;
            this.value = value;
        }
    }

    private List<Entry> entries = new ArrayList<>();

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int BAR_COLOR = Color.parseColor("#5B8DEF");
    private static final int TRACK_COLOR = Color.parseColor("#EEF1F8");
    private static final int LABEL_COLOR = Color.parseColor("#424242");
    private static final int AXIS_COLOR = Color.parseColor("#E0E0E0");

    public InquiryBarTrendView(Context context) {
        super(context);
        init();
    }

    public InquiryBarTrendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        barPaint.setColor(BAR_COLOR);
        barPaint.setStyle(Paint.Style.FILL);

        trackPaint.setColor(TRACK_COLOR);
        trackPaint.setStyle(Paint.Style.FILL);

        labelPaint.setColor(LABEL_COLOR);
        labelPaint.setTextSize(dp(10));
        labelPaint.setTextAlign(Paint.Align.LEFT);

        axisPaint.setColor(AXIS_COLOR);
        axisPaint.setStrokeWidth(dp(1));

        axisLabelPaint.setColor(Color.parseColor("#9E9E9E"));
        axisLabelPaint.setTextSize(dp(9));
        axisLabelPaint.setTextAlign(Paint.Align.CENTER);
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
        int rowHeight = (int) dp(32);
        int height = Math.max(1, entries.size()) * rowHeight + (int) dp(30); // + axis area
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (entries.isEmpty()) return;

        float labelColWidth = dp(70);
        float rightPadding = dp(54); // room for value text at bar end
        float chartLeft = labelColWidth;
        float chartRight = getWidth() - rightPadding;
        float chartWidth = chartRight - chartLeft;

        int maxValue = 1;
        for (Entry e : entries) maxValue = Math.max(maxValue, e.value);
        int axisMax = niceCeiling(maxValue);

        float rowHeight = dp(30);
        float barHeight = dp(10);
        float axisAreaHeight = dp(20);
        float chartBottom = getHeight() - axisAreaHeight;

        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            float rowTop = i * rowHeight;
            float rowCenterY = rowTop + rowHeight / 2f;

            // Month label
            labelPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(entry.monthLabel, 0, rowCenterY + dp(4), labelPaint);

            // Track (full width background)
            RectF track = new RectF(chartLeft, rowCenterY - barHeight / 2f,
                    chartRight, rowCenterY + barHeight / 2f);
            canvas.drawRoundRect(track, dp(7), dp(7), trackPaint);

            // Value bar
            float ratio = (float) entry.value / axisMax;
            float barEnd = chartLeft + chartWidth * ratio;
            RectF bar = new RectF(chartLeft, rowCenterY - barHeight / 2f,
                    Math.max(barEnd, chartLeft + dp(4)), rowCenterY + barHeight / 2f);
            canvas.drawRoundRect(bar, dp(7), dp(7), barPaint);

            // Value label at bar end
            labelPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(String.valueOf(entry.value), bar.right + dp(8), rowCenterY + dp(4), labelPaint);
        }

        // X axis line
        canvas.drawLine(chartLeft, chartBottom, chartRight, chartBottom, axisPaint);

        // X axis scale labels (0, 1/4, 1/2, 3/4, max)
        int ticks = 4;
        for (int t = 0; t <= ticks; t++) {
            float x = chartLeft + chartWidth * t / ticks;
            int value = axisMax * t / ticks;
            canvas.drawText(String.valueOf(value), x, chartBottom + dp(12), axisLabelPaint);
        }
    }

    /** Rounds up to a "nice" axis maximum (multiples of 5/10/etc.) */
    private int niceCeiling(int value) {
        if (value <= 4) return 4;
        int magnitude = 1;
        int v = value;
        while (v >= 10) {
            v /= 10;
            magnitude *= 10;
        }
        int base = (v + 1) * magnitude;
        return base;
    }
}