package com.example.androidproject;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class InquiryBarChartView extends View {

    public static class BarEntry {
        public String label;
        public int total, active, converted;
        public BarEntry(String label, int total, int active, int converted) {
            this.label = label; this.total = total;
            this.active = active; this.converted = converted;
        }
    }

    private List<BarEntry> entries = new ArrayList<>();
    private final Paint paintTotal    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintActive   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintConverted= new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintLabel    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintValue    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintAxis     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintGrid     = new Paint(Paint.ANTI_ALIAS_FLAG);

    public InquiryBarChartView(Context context) { super(context); init(); }
    public InquiryBarChartView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public InquiryBarChartView(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); init(); }

    private void init() {
        paintTotal.setColor(Color.parseColor("#1565C0"));
        paintActive.setColor(Color.parseColor("#FF8F00"));
        paintConverted.setColor(Color.parseColor("#2E7D32"));
        paintLabel.setColor(Color.parseColor("#555555"));
        paintLabel.setTextSize(spToPx(10));
        paintLabel.setTextAlign(Paint.Align.CENTER);
        paintValue.setColor(Color.parseColor("#333333"));
        paintValue.setTextSize(spToPx(9));
        paintValue.setTextAlign(Paint.Align.CENTER);
        paintValue.setTypeface(Typeface.DEFAULT_BOLD);
        paintAxis.setColor(Color.parseColor("#CCCCCC"));
        paintAxis.setStrokeWidth(1f);
        paintGrid.setColor(Color.parseColor("#EEEEEE"));
        paintGrid.setStrokeWidth(1f);
    }

    public void setData(List<BarEntry> data) {
        this.entries = data;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (entries == null || entries.isEmpty()) return;

        float w = getWidth();
        float h = getHeight();
        float paddingLeft   = dpToPx(36);
        float paddingRight  = dpToPx(8);
        float paddingTop    = dpToPx(16);
        float paddingBottom = dpToPx(32);

        float chartH = h - paddingTop - paddingBottom;
        float chartW = w - paddingLeft - paddingRight;

        // Max value
        int maxVal = 1;
        for (BarEntry e : entries) maxVal = Math.max(maxVal, e.total);

        // Grid lines (4 horizontal)
        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            float y = paddingTop + chartH - (chartH * i / gridLines);
            canvas.drawLine(paddingLeft, y, w - paddingRight, y, paintGrid);
            // Y axis labels
            int val = maxVal * i / gridLines;
            paintLabel.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.valueOf(val), paddingLeft - dpToPx(4), y + spToPx(4), paintLabel);
        }
        paintLabel.setTextAlign(Paint.Align.CENTER);

        // Axis line
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, paddingTop + chartH, paintAxis);
        canvas.drawLine(paddingLeft, paddingTop + chartH, w - paddingRight, paddingTop + chartH, paintAxis);

        int n = entries.size();
        float groupW = chartW / n;
        float barW   = groupW * 0.22f;
        float gap    = barW * 0.15f;
        float cornerR = dpToPx(3);

        for (int i = 0; i < n; i++) {
            BarEntry e = entries.get(i);
            float groupCenterX = paddingLeft + groupW * i + groupW / 2f;

            // 3 bars per group: Total, Active, Converted
            float x1 = groupCenterX - barW - gap - barW / 2f;  // Total
            float x2 = groupCenterX - barW / 2f;               // Active
            float x3 = groupCenterX + gap + barW / 2f;         // Converted

            drawBar(canvas, x1, barW, e.total,  maxVal, paddingTop, chartH, paintTotal,     cornerR);
            drawBar(canvas, x2, barW, e.active, maxVal, paddingTop, chartH, paintActive,    cornerR);
            drawBar(canvas, x3, barW, e.converted, maxVal, paddingTop, chartH, paintConverted, cornerR);

            // Course label below axis
            String label = e.label.length() > 8 ? e.label.substring(0, 7) + "…" : e.label;
            canvas.drawText(label, groupCenterX, paddingTop + chartH + dpToPx(14), paintLabel);
        }
    }

    private void drawBar(Canvas canvas, float centerX, float barW, int value, int maxVal,
                         float paddingTop, float chartH, Paint paint, float r) {
        if (value <= 0) return;
        float barH  = chartH * value / maxVal;
        float left  = centerX - barW / 2f;
        float right = centerX + barW / 2f;
        float top   = paddingTop + chartH - barH;
        float bottom= paddingTop + chartH;

        RectF rect = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(rect, r, r, paint);

        // Value label on top of bar
        canvas.drawText(String.valueOf(value), centerX, top - dpToPx(2), paintValue);
    }

    private float dpToPx(float dp) {
        return dp * getContext().getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getContext().getResources().getDisplayMetrics().scaledDensity;
    }
}