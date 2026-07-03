package com.example.androidproject;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Simple two-slice pie chart (Paid Fees vs Pending Fees) drawn on a Canvas.
 * No external charting library required — matches the look of the
 * web app's "Fee Collection Summary" pie chart.
 */
public class FeesPieChartView extends View {

    private double paidFees = 0;
    private double pendingFees = 0;

    private static final int COLOR_PAID = Color.parseColor("#1E88E5");    // blue
    private static final int COLOR_PENDING = Color.parseColor("#FFA726"); // orange

    private final Paint slicePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint legendTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint legendBoxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF chartRect = new RectF();

    public FeesPieChartView(Context context) {
        super(context);
        init();
    }

    public FeesPieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FeesPieChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        textPaint.setColor(Color.parseColor("#222222"));
        textPaint.setTextSize(spToPx(15));
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        legendTextPaint.setColor(Color.parseColor("#333333"));
        legendTextPaint.setTextSize(spToPx(13));

        legendBoxPaint.setStyle(Paint.Style.FILL);
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    /**
     * Set the data to display. Call this whenever the underlying
     * fees data changes (e.g. after applying a course filter).
     */
    public void setData(double paidFees, double pendingFees) {
        this.paidFees = Math.max(paidFees, 0);
        this.pendingFees = Math.max(pendingFees, 0);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        double total = paidFees + pendingFees;

        // Layout: pie chart on the left half, legend on the right half
        float legendWidth = dpToPx(140);
        float availableForChart = width - legendWidth;
        float diameter = Math.min(availableForChart, height) - dpToPx(20);
        if (diameter < dpToPx(60)) diameter = Math.min(width, height) - dpToPx(20);

        float left = dpToPx(10);
        float top = (height - diameter) / 2f;
        chartRect.set(left, top, left + diameter, top + diameter);

        if (total <= 0) {
            // No data — draw an empty grey circle with a message
            slicePaint.setColor(Color.parseColor("#E0E0E0"));
            canvas.drawOval(chartRect, slicePaint);
            textPaint.setTextSize(spToPx(13));
            canvas.drawText("No fee data", chartRect.centerX(), chartRect.centerY(), textPaint);
            return;
        }

        float paidAngle = (float) (paidFees / total * 360f);
        float pendingAngle = 360f - paidAngle;

        // Paid slice
        slicePaint.setColor(COLOR_PAID);
        canvas.drawArc(chartRect, -90, paidAngle, true, slicePaint);

        // Pending slice
        slicePaint.setColor(COLOR_PENDING);
        canvas.drawArc(chartRect, -90 + paidAngle, pendingAngle, true, slicePaint);

        // Value labels in the middle of each slice
        drawSliceLabel(canvas, -90, paidAngle, paidFees);
        drawSliceLabel(canvas, -90 + paidAngle, pendingAngle, pendingFees);

        // Legend (top-right of the view, vertically centered)
        float legendX = chartRect.right + dpToPx(24);
        float legendStartY = height / 2f - dpToPx(20);
        float boxSize = dpToPx(14);

        legendBoxPaint.setColor(COLOR_PAID);
        canvas.drawRect(legendX, legendStartY, legendX + boxSize, legendStartY + boxSize, legendBoxPaint);
        canvas.drawText("", legendX, legendStartY, legendTextPaint); // no-op keep paint warm
        canvas.save();
        legendTextPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Paid Fees", legendX + boxSize + dpToPx(8), legendStartY + boxSize - dpToPx(2), legendTextPaint);

        float legendY2 = legendStartY + dpToPx(28);
        legendBoxPaint.setColor(COLOR_PENDING);
        canvas.drawRect(legendX, legendY2, legendX + boxSize, legendY2 + boxSize, legendBoxPaint);
        canvas.drawText("Pending Fees", legendX + boxSize + dpToPx(8), legendY2 + boxSize - dpToPx(2), legendTextPaint);
        canvas.restore();
    }

    private void drawSliceLabel(Canvas canvas, float startAngle, float sweepAngle, double value) {
        if (sweepAngle <= 0 || value <= 0) return;

        float midAngle = (float) Math.toRadians(startAngle + sweepAngle / 2f);
        float radius = chartRect.width() / 2f * 0.6f;
        float cx = chartRect.centerX() + (float) (radius * Math.cos(midAngle));
        float cy = chartRect.centerY() + (float) (radius * Math.sin(midAngle));

        String label = formatAmount(value);
        canvas.drawText(label, cx, cy, textPaint);
    }

    private String formatAmount(double value) {
        long rounded = Math.round(value);
        return String.valueOf(rounded);
    }
}