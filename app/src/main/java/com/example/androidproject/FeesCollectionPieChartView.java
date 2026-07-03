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

public class FeesCollectionPieChartView extends View {

    public static class PieEntry {
        public String label;   // "18 Mar"
        public double amount;

        public PieEntry(String label, double amount) {
            this.label  = label;
            this.amount = amount;
        }
    }

    private List<PieEntry> entries = new ArrayList<>();

    // Palette — blue family + greens + others to distinguish days
    private static final int[] COLORS = {
            0xFF1565C0, 0xFF2E7D32, 0xFFE65100, 0xFF6A1B9A,
            0xFF00838F, 0xFFC62828, 0xFF558B2F, 0xFF1565C0,
            0xFFAD1457, 0xFF4527A0, 0xFF00695C, 0xFF6D4C41,
            0xFF37474F, 0xFFFF6F00, 0xFF0277BD, 0xFF2E7D32
    };

    private final Paint paintSlice  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintCenter = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintText   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintLabel  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintDot    = new Paint(Paint.ANTI_ALIAS_FLAG);

    public FeesCollectionPieChartView(Context context) { super(context); init(); }
    public FeesCollectionPieChartView(Context context, AttributeSet a) { super(context, a); init(); }
    public FeesCollectionPieChartView(Context context, AttributeSet a, int d) { super(context, a, d); init(); }

    private void init() {
        paintCenter.setColor(Color.WHITE);
        paintCenter.setAntiAlias(true);

        paintText.setColor(Color.parseColor("#1A1A1A"));
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setAntiAlias(true);
        paintText.setTypeface(Typeface.DEFAULT_BOLD);

        paintLabel.setColor(Color.parseColor("#555555"));
        paintLabel.setAntiAlias(true);
        paintLabel.setTextSize(sp(11f));

        paintDot.setAntiAlias(true);
    }

    public void setData(List<PieEntry> data) {
        this.entries = data != null ? data : new ArrayList<>();
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);

        // Pie height + legend rows below
        int legendRows = entries.isEmpty() ? 0 : (int) Math.ceil(entries.size() / 2.0);
        int legendH    = legendRows * (int) dp(24) + (int) dp(16);
        int pieSize    = Math.min(width, (int) dp(240));
        int totalH     = pieSize + legendH;

        setMeasuredDimension(width, totalH);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (entries == null || entries.isEmpty()) return;

        float w = getWidth();

        // ── Pie dimensions ─────────────────────────────────────────
        float pieSize   = Math.min(w, dp(240));
        float pieCX     = w / 2f;
        float pieCY     = pieSize / 2f;
        float outerR    = pieSize / 2f - dp(8);
        float innerR    = outerR * 0.52f;  // donut hole

        // Total
        double total = 0;
        for (PieEntry e : entries) total += e.amount;
        if (total == 0) return;

        // ── Draw slices ────────────────────────────────────────────
        RectF oval = new RectF(pieCX - outerR, pieCY - outerR,
                pieCX + outerR, pieCY + outerR);

        float startAngle = -90f;
        for (int i = 0; i < entries.size(); i++) {
            PieEntry e   = entries.get(i);
            float sweep  = (float)(e.amount / total * 360f);
            int   color  = COLORS[i % COLORS.length];

            paintSlice.setColor(color);
            canvas.drawArc(oval, startAngle, sweep, true, paintSlice);
            startAngle += sweep;
        }

        // ── Donut hole ─────────────────────────────────────────────
        canvas.drawCircle(pieCX, pieCY, innerR, paintCenter);

        // ── Center text: total amount ──────────────────────────────
        paintText.setTextSize(sp(16f));
        paintText.setColor(Color.parseColor("#1565C0"));
        canvas.drawText("₹" + formatAmount((long) total), pieCX, pieCY - sp(4), paintText);

        paintText.setTextSize(sp(10f));
        paintText.setColor(Color.parseColor("#888888"));
        canvas.drawText("Total Collected", pieCX, pieCY + sp(14), paintText);

        // ── Legend below pie ───────────────────────────────────────
        float legendTop  = pieSize + dp(8);
        float dotSize    = dp(10);
        float rowH       = dp(24);
        float colW       = w / 2f;

        for (int i = 0; i < entries.size(); i++) {
            PieEntry e  = entries.get(i);
            int col     = i % 2;
            int row     = i / 2;

            float x = col * colW + dp(14);
            float y = legendTop + row * rowH;

            // Colored dot
            paintDot.setColor(COLORS[i % COLORS.length]);
            canvas.drawCircle(x + dotSize / 2f, y + rowH / 2f, dotSize / 2f, paintDot);

            // Label: "18 Mar  ₹1.7L"
            String label = shortDate(e.label) + "  ₹" + formatAmount((long) e.amount);
            float pct    = (float)(e.amount / total * 100f);
            String full  = label + String.format("  %.0f%%", pct);

            paintLabel.setTextSize(sp(10f));
            paintLabel.setColor(Color.parseColor("#333333"));
            canvas.drawText(full, x + dotSize + dp(6),
                    y + rowH / 2f + sp(4), paintLabel);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────

    // "2026-03-18" → "18 Mar"
    private String shortDate(String iso) {
        try {
            String[] parts  = iso.split("-");
            String[] months = {"","Jan","Feb","Mar","Apr","May","Jun",
                    "Jul","Aug","Sep","Oct","Nov","Dec"};
            int m = Integer.parseInt(parts[1]);
            return parts[2] + " " + months[m];
        } catch (Exception e) {
            return iso;
        }
    }

    // 170674 → "1.7L", 11000 → "11K", 500 → "500"
    private String formatAmount(long val) {
        if (val >= 100000) return String.format("%.1fL", val / 100000.0);
        if (val >= 1000)   return String.format("%.0fK", val / 1000.0);
        return String.valueOf(val);
    }

    private float dp(float v) { return v * getContext().getResources().getDisplayMetrics().density; }
    private float sp(float v) { return v * getContext().getResources().getDisplayMetrics().scaledDensity; }
}