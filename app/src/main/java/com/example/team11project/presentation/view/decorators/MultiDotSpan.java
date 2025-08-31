package com.example.team11project.presentation.view.decorators;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.LineBackgroundSpan;

import androidx.annotation.NonNull;

import java.util.List;

public class MultiDotSpan implements LineBackgroundSpan {

    private final float radius;
    private final List<Integer> colors;

    public MultiDotSpan(float radius, List<Integer> colors) {
        this.radius = radius;
        this.colors = colors;
    }
        @Override
    public void drawBackground(@NonNull Canvas canvas, @NonNull Paint paint, int left, int right, int top, int baseline, int bottom, @NonNull CharSequence text, int start, int end, int lnum) {
                int total = colors.size();
                if (total == 0) return;

                // Izračunaj početnu tačku da sve tačkice budu centrirane
                float totalWidth = (total * 2 * radius) + ((total - 1) * radius); // Širina svih tačkica + razmaci
                float startX = ((float)(left + right) / 2) - (totalWidth / 2);

                float cx = startX + radius;

                for (int color : colors) {
                    int oldColor = paint.getColor();
                    paint.setColor(color);
                    canvas.drawCircle(cx, bottom + radius, radius, paint);
                    paint.setColor(oldColor);
                    cx += 3 * radius; // Pomeri X poziciju za sledeću tačkicu
                }
    }
}

