package com.example.androidproject.model.summary;

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
 * Simple self-contained bar chart used on the Admission Summary screen.
 * Draws one bar per course, height proportional to totalFees.
 */
public class AdmissionBarChartView extends View {

    public static class BarEntry {
        public final String courseName;
        public final double totalFees;
        public final int admissionCount;

        public BarEntry(String courseName, double totalFees, int admissionCount) {
            this.courseName = courseName;
            this.totalFees = totalFees;
            this.admissionCount = admissionCount;
        }
    }

    private List<BarEntry> entries = new ArrayList<>();

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int BAR_COLOR = Color.parseColor("#2E7D32");
    private static final int AXIS_COLOR = Color.parseColor("#BDBDBD");
    private static final int LABEL_COLOR = Color.parseColor("#333333");

    public AdmissionBarChartView(Context context) {
        super(context);
        init();
    }

    public AdmissionBarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        barPaint.setColor(BAR_COLOR);
        barPaint.setStyle(Paint.Style.FILL);

        axisPaint.setColor(AXIS_COLOR);
        axisPaint.setStrokeWidth(2f);

        labelPaint.setColor(LABEL_COLOR);
        labelPaint.setTextSize(dp(12));
        labelPaint.setTextAlign(Paint.Align.CENTER);

        valuePaint.setColor(BAR_COLOR);
        valuePaint.setTextSize(dp(12));
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setFakeBoldText(true);
    }

    public void setData(List<BarEntry> data) {
        this.entries = data != null ? data : new ArrayList<>();
        requestLayout();
        invalidate();
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) dp(240);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (entries.isEmpty()) {
            String msg = "No data to display";
            labelPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(msg, getWidth() / 2f, getHeight() / 2f, labelPaint);
            return;
        }

        float paddingLeft = dp(48);
        float paddingRight = dp(16);
        float paddingTop = dp(24);
        float paddingBottom = dp(40);

        float chartWidth = getWidth() - paddingLeft - paddingRight;
        float chartHeight = getHeight() - paddingTop - paddingBottom;

        double maxFees = 0;
        for (BarEntry e : entries) {
            maxFees = Math.max(maxFees, e.totalFees);
        }
        if (maxFees == 0) maxFees = 1;

        // Y axis line
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, paddingTop + chartHeight, axisPaint);
        // X axis line
        canvas.drawLine(paddingLeft, paddingTop + chartHeight, paddingLeft + chartWidth, paddingTop + chartHeight, axisPaint);

        // Y axis labels (0, mid, max)
        labelPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(formatFees(0), paddingLeft - dp(6), paddingTop + chartHeight, labelPaint);
        canvas.drawText(formatFees(maxFees / 2), paddingLeft - dp(6), paddingTop + chartHeight / 2f, labelPaint);
        canvas.drawText(formatFees(maxFees), paddingLeft - dp(6), paddingTop + dp(10), labelPaint);

        int count = entries.size();
        float slotWidth = chartWidth / count;
        float barWidth = Math.min(slotWidth * 0.5f, dp(56));

        for (int i = 0; i < count; i++) {
            BarEntry entry = entries.get(i);
            float slotCenterX = paddingLeft + slotWidth * i + slotWidth / 2f;

            float barHeightRatio = (float) (entry.totalFees / maxFees);
            float barHeight = chartHeight * barHeightRatio;

            RectF rect = new RectF(
                    slotCenterX - barWidth / 2f,
                    paddingTop + chartHeight - barHeight,
                    slotCenterX + barWidth / 2f,
                    paddingTop + chartHeight
            );
            canvas.drawRoundRect(rect, dp(6), dp(6), barPaint);

            // Value on top of bar
            labelPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(formatFees(entry.totalFees), slotCenterX, rect.top - dp(6), valuePaint);

            // Course name below axis
            canvas.drawText(entry.courseName, slotCenterX, paddingTop + chartHeight + dp(20), labelPaint);
        }
    }

    private String formatFees(double value) {
        if (value >= 100000) {
            return String.format(Locale.getDefault(), "%.1fL", value / 100000);
        } else if (value >= 1000) {
            return String.format(Locale.getDefault(), "%.1fK", value / 1000);
        }
        return String.format(Locale.getDefault(), "%.0f", value);
    }
}