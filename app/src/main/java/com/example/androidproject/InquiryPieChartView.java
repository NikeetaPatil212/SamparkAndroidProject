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

public class InquiryPieChartView extends View {

    public static class PieEntry {
        public String label;
        public int    total, active, converted;
        public PieEntry(String label, int total, int active, int converted) {
            this.label     = label;
            this.total     = total;
            this.active    = active;
            this.converted = converted;
        }
    }

    // ── Palette — 12 distinct colours ────────────────────────────
    private static final int[] PALETTE = {
            0xFF1565C0, // blue
            0xFF2E7D32, // green
            0xFFE65100, // orange
            0xFFC62828, // red
            0xFF6A1B9A, // purple
            0xFF00838F, // teal
            0xFFF9A825, // amber
            0xFF37474F, // blue-grey
            0xFF558B2F, // light green
            0xFFAD1457, // pink
            0xFF0277BD, // light blue
            0xFF4E342E, // brown
    };

    private List<PieEntry> entries  = new ArrayList<>();
    private final Paint    paintSlice = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint    paintText  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint    paintPct   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint    paintCenter= new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint    paintLegendRect = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint    paintLegendText = new Paint(Paint.ANTI_ALIAS_FLAG);

    public InquiryPieChartView(Context ctx) { super(ctx); init(); }
    public InquiryPieChartView(Context ctx, AttributeSet a) { super(ctx, a); init(); }
    public InquiryPieChartView(Context ctx, AttributeSet a, int d) { super(ctx, a, d); init(); }

    private void init() {
        paintText.setColor(Color.WHITE);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setTypeface(Typeface.DEFAULT_BOLD);

        paintPct.setColor(Color.WHITE);
        paintPct.setTextAlign(Paint.Align.CENTER);

        paintCenter.setColor(Color.WHITE);
        paintCenter.setTextAlign(Paint.Align.CENTER);

        paintLegendRect.setAntiAlias(true);

        paintLegendText.setColor(Color.parseColor("#333333"));
        paintLegendText.setAntiAlias(true);
    }

    public void setData(List<PieEntry> data) {
        this.entries = data != null ? data : new ArrayList<>();
        invalidate();
        requestLayout();
    }

    // ── Measure: pie + legend rows ────────────────────────────────
    @Override
    protected void onMeasure(int wSpec, int hSpec) {
        int w = MeasureSpec.getSize(wSpec);
        // pie diameter = min(w, 260dp), legend = rows * 28dp
        float pieDia = Math.min(w, dpToPx(260));
        int legendRows = entries == null ? 0 : entries.size();
        int legendH = (int)(legendRows * dpToPx(28) + dpToPx(12));
        int total   = (int)(pieDia + legendH + dpToPx(16));
        setMeasuredDimension(w, total);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (entries == null || entries.isEmpty()) return;

        float w = getWidth();
        float pieDia = Math.min(w * 0.75f, dpToPx(240));
        float pieR   = pieDia / 2f;
        float cx     = w / 2f;
        float cy     = dpToPx(8) + pieR;

        // Donut hole radius
        float holeR = pieR * 0.45f;

        RectF oval = new RectF(cx - pieR, cy - pieR, cx + pieR, cy + pieR);

        // Sum total
        int sum = 0;
        for (PieEntry e : entries) sum += e.total;
        if (sum == 0) return;

        // Draw slices
        float startAngle = -90f;
        paintText.setTextSize(spToPx(9));
        paintPct.setTextSize(spToPx(8));

        for (int i = 0; i < entries.size(); i++) {
            PieEntry e = entries.get(i);
            if (e.total == 0) continue;

            float sweep = 360f * e.total / sum;
            paintSlice.setColor(PALETTE[i % PALETTE.length]);
            canvas.drawArc(oval, startAngle, sweep, true, paintSlice);

            // % label inside slice (only if slice >= 8%)
            float pct = e.total * 100f / sum;
            if (pct >= 6f) {
                double midAngle = Math.toRadians(startAngle + sweep / 2f);
                float labelR = pieR * 0.72f;
                float lx = cx + (float)(labelR * Math.cos(midAngle));
                float ly = cy + (float)(labelR * Math.sin(midAngle));
                canvas.drawText(String.format("%.0f%%", pct), lx, ly - spToPx(4), paintText);
                canvas.drawText(String.valueOf(e.total), lx, ly + spToPx(7), paintPct);
            }

            startAngle += sweep;
        }

        // Donut hole
        paintCenter.setColor(Color.WHITE);
        canvas.drawCircle(cx, cy, holeR, paintCenter);

        // Centre text: total
        Paint centreLabel = new Paint(Paint.ANTI_ALIAS_FLAG);
        centreLabel.setTextAlign(Paint.Align.CENTER);
        centreLabel.setTypeface(Typeface.DEFAULT_BOLD);
        centreLabel.setColor(Color.parseColor("#2E7D32"));
        centreLabel.setTextSize(spToPx(18));
        canvas.drawText(String.valueOf(sum), cx, cy + spToPx(6), centreLabel);

        Paint centreSub = new Paint(Paint.ANTI_ALIAS_FLAG);
        centreSub.setTextAlign(Paint.Align.CENTER);
        centreSub.setColor(Color.parseColor("#888888"));
        centreSub.setTextSize(spToPx(10));
        canvas.drawText("Total", cx, cy + spToPx(18), centreSub);

        // ── Legend ────────────────────────────────────────────────
        float legendTop = cy + pieR + dpToPx(12);
        float rowH      = dpToPx(26);
        float boxSize   = dpToPx(12);
        float textSize  = spToPx(12);
        paintLegendText.setTextSize(textSize);

        for (int i = 0; i < entries.size(); i++) {
            PieEntry e = entries.get(i);
            float rowY = legendTop + i * rowH;

            // Colour box
            paintLegendRect.setColor(PALETTE[i % PALETTE.length]);
            RectF box = new RectF(dpToPx(16), rowY,
                    dpToPx(16) + boxSize, rowY + boxSize);
            canvas.drawRoundRect(box, dpToPx(2), dpToPx(2), paintLegendRect);

            // Course name
            String label = e.label != null ? e.label : "";
            canvas.drawText(label,
                    dpToPx(16) + boxSize + dpToPx(8),
                    rowY + boxSize - dpToPx(1),
                    paintLegendText);

            // Count on right
            Paint countPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            countPaint.setTextAlign(Paint.Align.RIGHT);
            countPaint.setTypeface(Typeface.DEFAULT_BOLD);
            countPaint.setTextSize(textSize);
            countPaint.setColor(PALETTE[i % PALETTE.length]);
            canvas.drawText(String.valueOf(e.total),
                    getWidth() - dpToPx(16),
                    rowY + boxSize - dpToPx(1),
                    countPaint);
        }
    }

    private float dpToPx(float dp) {
        return dp * getContext().getResources().getDisplayMetrics().density;
    }
    private float spToPx(float sp) {
        return sp * getContext().getResources().getDisplayMetrics().scaledDensity;
    }
}
