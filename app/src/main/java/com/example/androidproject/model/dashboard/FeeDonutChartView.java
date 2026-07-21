package com.example.androidproject.model.dashboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FeeDonutChartView extends LinearLayout {

    public static class Slice {
        public final String label;
        public final double amount;
        public final int color;
        public Slice(String label, double amount, int color) {
            this.label = label; this.amount = amount; this.color = color;
        }
    }

    private DonutCanvasView donutCanvasView;
    private TextView tvCenterTitle;
    private TextView tvCenterAmount;
    private LinearLayout legendContainer;

    public FeeDonutChartView(Context context) { super(context); init(); }
    public FeeDonutChartView(Context context, AttributeSet attrs) { super(context, attrs); init(); }

    private void init() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        FrameLayout donutFrame = new FrameLayout(getContext());
        addView(donutFrame, new LayoutParams(0, dp(150), 1.1f));

        donutCanvasView = new DonutCanvasView(getContext());
        donutFrame.addView(donutCanvasView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        LinearLayout centerLabel = new LinearLayout(getContext());
        centerLabel.setOrientation(VERTICAL);
        centerLabel.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams centerParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        centerParams.gravity = Gravity.CENTER;
        donutFrame.addView(centerLabel, centerParams);

        tvCenterTitle = new TextView(getContext());
        tvCenterTitle.setTextColor(Color.parseColor("#757575"));
        tvCenterTitle.setTextSize(11f);
        tvCenterTitle.setGravity(Gravity.CENTER);
        centerLabel.addView(tvCenterTitle);

        tvCenterAmount = new TextView(getContext());
        tvCenterAmount.setTextColor(Color.parseColor("#1A1A1A"));
        tvCenterAmount.setTextSize(15f);
        tvCenterAmount.setTypeface(null, android.graphics.Typeface.BOLD);
        tvCenterAmount.setGravity(Gravity.CENTER);
        centerLabel.addView(tvCenterAmount);

        legendContainer = new LinearLayout(getContext());
        legendContainer.setOrientation(VERTICAL);
        legendContainer.setGravity(Gravity.CENTER_VERTICAL);
        LayoutParams legendParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        legendParams.setMarginStart(dp(12));
        addView(legendContainer, legendParams);
    }

    public void setData(List<Slice> slices, String totalLabel, double totalValue) {
        donutCanvasView.setSlices(slices);
        tvCenterTitle.setText(totalLabel);
        tvCenterAmount.setText(formatCurrency(totalValue));
        rebuildLegend(slices);
    }

    /** ★★ Public flag — properly forwarded to canvas now ★★ */
    public void setShowSliceLabels(boolean show) {
        donutCanvasView.setShowSliceLabels(show);
    }

    private void rebuildLegend(List<Slice> slices) {
        legendContainer.removeAllViews();
        if (slices == null) return;

        for (Slice slice : slices) {
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LayoutParams rowParams = new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            rowParams.bottomMargin = dp(6);
            row.setLayoutParams(rowParams);

            View dot = new View(getContext());
            LayoutParams dotParams = new LayoutParams(dp(12), dp(12));
            dotParams.setMarginEnd(dp(8));
            dotParams.topMargin = dp(4);
            dot.setLayoutParams(dotParams);
            dot.setBackground(makeCircleDrawable(slice.color));

            LinearLayout textCol = new LinearLayout(getContext());
            textCol.setOrientation(VERTICAL);

            TextView tvAmount = new TextView(getContext());
            tvAmount.setText(formatCurrency(slice.amount));
            tvAmount.setTextColor(Color.parseColor("#1A1A1A"));
            tvAmount.setTextSize(15f);
            tvAmount.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView tvLabel = new TextView(getContext());
            tvLabel.setText(slice.label);
            tvLabel.setTextColor(Color.parseColor("#757575"));
            tvLabel.setTextSize(12f);

            textCol.addView(tvAmount);
            textCol.addView(tvLabel);

            row.addView(dot);
            row.addView(textCol);
            legendContainer.addView(row);
        }
    }

    private android.graphics.drawable.Drawable makeCircleDrawable(int color) {
        android.graphics.drawable.GradientDrawable d = new android.graphics.drawable.GradientDrawable();
        d.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        d.setColor(color);
        return d;
    }

    private String formatCurrency(double value) {
        if (value >= 100000) return String.format(Locale.getDefault(), "₹%.2fL", value / 100000);
        return String.format(Locale.getDefault(), "₹%,.0f", value);
    }

    private int dp(float value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    // ── Inner canvas ─────────────────────────────────────────────
    private static class DonutCanvasView extends View {

        private List<Slice> slices = new ArrayList<>();
        private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint percentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF ringRect = new RectF();

        /** ★★ New flag lives HERE, on the canvas that actually draws text ★★ */
        private boolean showSliceLabels = true;

        DonutCanvasView(Context context) {
            super(context);
            arcPaint.setStyle(Paint.Style.STROKE);
            arcPaint.setStrokeCap(Paint.Cap.BUTT);
            percentPaint.setColor(Color.WHITE);
            percentPaint.setTextAlign(Paint.Align.CENTER);
            percentPaint.setFakeBoldText(true);
            percentPaint.setTextSize(TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP, 13, getResources().getDisplayMetrics()));
        }

        void setSlices(List<Slice> slices) {
            this.slices = slices != null ? slices : new ArrayList<>();
            invalidate();
        }

        void setShowSliceLabels(boolean show) {
            this.showSliceLabels = show;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (slices.isEmpty()) return;

            double total = 0;
            for (Slice s : slices) total += s.amount;
            if (total <= 0) return;

            float size = Math.min(getWidth(), getHeight()) * 0.85f;
            float strokeWidth = size * 0.22f;
            arcPaint.setStrokeWidth(strokeWidth);

            float left = (getWidth() - size) / 2f + strokeWidth / 2f;
            float top = (getHeight() - size) / 2f + strokeWidth / 2f;
            float right = (getWidth() + size) / 2f - strokeWidth / 2f;
            float bottom = (getHeight() + size) / 2f - strokeWidth / 2f;
            ringRect.set(left, top, right, bottom);

            float startAngle = -90f;
            float radius = (right - left) / 2f;
            for (Slice slice : slices) {
                if (slice.amount <= 0) continue;                       // skip 0-value arcs
                float sweep = (float) (360.0 * slice.amount / total);
                if (sweep <= 0) continue;

                arcPaint.setColor(slice.color);
                canvas.drawArc(ringRect, startAngle, sweep, false, arcPaint);

                // ★★ ONLY draw percentage if flag is ON ★★
                if (showSliceLabels && sweep > 18f) {
                    float midAngle = (float) Math.toRadians(startAngle + sweep / 2f);
                    float cx = ringRect.centerX() + radius * (float) Math.cos(midAngle);
                    float cy = ringRect.centerY() + radius * (float) Math.sin(midAngle)
                            + (percentPaint.getTextSize() / 3f);
                    String pct = String.format(Locale.getDefault(),
                            "%.1f%%", 100.0 * slice.amount / total);
                    canvas.drawText(pct, cx, cy, percentPaint);
                }

                startAngle += sweep;
            }
        }
    }
}