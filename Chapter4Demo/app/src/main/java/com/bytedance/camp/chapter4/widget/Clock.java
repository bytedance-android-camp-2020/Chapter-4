package com.bytedance.camp.chapter4.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Clock extends View {

    private static final int FULL_CIRCLE_DEGREE = 360;
    private static final int UNIT_DEGREE = 6;

    private static final float UNIT_LINE_WIDTH = 8; // 刻度线的宽度
    private static final int HIGHLIGHT_UNIT_ALPHA = 0xFF;
    private static final int NORMAL_UNIT_ALPHA = 0x80;

    private static final float HOUR_NEEDLE_LENGTH_RATIO = 0.4f; // 时针长度相对表盘半径的比例
    private static final float MINUTE_NEEDLE_LENGTH_RATIO = 0.6f; // 分针长度相对表盘半径的比例
    private static final float SECOND_NEEDLE_LENGTH_RATIO = 0.8f; // 秒针长度相对表盘半径的比例
    private static final float HOUR_NEEDLE_WIDTH = 12; // 时针的宽度
    private static final float MINUTE_NEEDLE_WIDTH = 8; // 分针的宽度
    private static final float SECOND_NEEDLE_WIDTH = 4; // 秒针的宽度

    private Calendar calendar = Calendar.getInstance();

    private float radius = 0;
    private float centerX = 0;
    private float centerY = 0;
    private List<RectF> unitLinePositions = new ArrayList<>();
    private Paint unitPaint = new Paint();
    private Paint needlePaint = new Paint();
    private Paint numberPaint = new Paint();
    private Paint.FontMetrics fontMetrics = new Paint.FontMetrics();

    public Clock(Context context) {
        super(context);
        init();
    }

    public Clock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Clock(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        unitPaint.setAntiAlias(true);
        unitPaint.setColor(Color.WHITE);
        unitPaint.setStrokeWidth(UNIT_LINE_WIDTH);
        unitPaint.setStrokeCap(Paint.Cap.ROUND);
        unitPaint.setStyle(Paint.Style.STROKE);

        needlePaint.setAntiAlias(true);
        needlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        needlePaint.setStrokeCap(Paint.Cap.ROUND);
        needlePaint.setColor(Color.WHITE);

        numberPaint.setAntiAlias(true);
        numberPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        numberPaint.setTextSize(50);
        numberPaint.setColor(Color.WHITE);
        numberPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        configWhenLayoutChanged();
    }

    private void configWhenLayoutChanged() {
        float newRadius = Math.min(getWidth(), getHeight()) / 2f;
        if (newRadius == radius) {
            return;
        }
        radius = newRadius;
        centerX = getWidth() / 2f;
        centerY = getHeight() / 2f;

        // 当视图的宽高确定后就可以提前计算表盘的刻度线的起止坐标了
        for (int degree = 0; degree < FULL_CIRCLE_DEGREE; degree += UNIT_DEGREE) {
            double radians = Math.toRadians(degree);
            float startX = (float) (centerX + (radius * (1 - 0.05f)) * Math.cos(radians));
            float startY = (float) (centerX + (radius * (1 - 0.05f)) * Math.sin(radians));
            float stopX = (float) (centerX + radius * Math.cos(radians));
            float stopY = (float) (centerY + radius * Math.sin(radians));
            unitLinePositions.add(new RectF(startX, startY, stopX, stopY));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawUnit(canvas);
        drawTimeNeedles(canvas);
        drawTimeNumbers(canvas);
    }

    // 绘制表盘上的刻度
    private void drawUnit(Canvas canvas) {
        for (int i = 0; i < unitLinePositions.size(); i++) {
            if (i % 5 == 0) {
                unitPaint.setAlpha(HIGHLIGHT_UNIT_ALPHA);
            } else {
                unitPaint.setAlpha(NORMAL_UNIT_ALPHA);
            }
            RectF linePosition = unitLinePositions.get(i);
            canvas.drawLine(linePosition.left, linePosition.top, linePosition.right, linePosition.bottom, unitPaint);
        }
    }

    private void drawTimeNeedles(Canvas canvas) {
        Time time = getCurrentTime();
        float hourUnitDegree = UNIT_DEGREE * 5f;
        float hourDegree = (time.getHours() + time.getMinutes() / 60f) * hourUnitDegree - 90;
        float minuteDegree = time.getMinutes() * UNIT_DEGREE - 90;
        float secondsDegree = time.getSeconds() * UNIT_DEGREE - 90;
        needlePaint.setStrokeWidth(HOUR_NEEDLE_WIDTH);
        canvas.drawLine(
                centerX,
                centerY,
                (float) (centerX + radius * Math.cos(Math.toRadians(hourDegree)) * HOUR_NEEDLE_LENGTH_RATIO),
                (float) (centerY + radius * Math.sin(Math.toRadians(hourDegree)) * HOUR_NEEDLE_LENGTH_RATIO),
                needlePaint
        );
        needlePaint.setStrokeWidth(MINUTE_NEEDLE_WIDTH);
        canvas.drawLine(
                centerX,
                centerY,
                (float) (centerX + radius * Math.cos(Math.toRadians(minuteDegree)) * MINUTE_NEEDLE_LENGTH_RATIO),
                (float) (centerY + radius * Math.sin(Math.toRadians(minuteDegree)) * MINUTE_NEEDLE_LENGTH_RATIO),
                needlePaint
        );
        needlePaint.setStrokeWidth(SECOND_NEEDLE_WIDTH);
        canvas.drawLine(
                centerX,
                centerY,
                (float) (centerX + radius * Math.cos(Math.toRadians(secondsDegree)) * SECOND_NEEDLE_LENGTH_RATIO),
                (float) (centerY + radius * Math.sin(Math.toRadians(secondsDegree)) * SECOND_NEEDLE_LENGTH_RATIO),
                needlePaint
        );
    }

    private void drawTimeNumbers(Canvas canvas) {
        numberPaint.getFontMetrics(fontMetrics);
        float textHeight = fontMetrics.descent - fontMetrics.ascent;
        // 粗糙绘制
        for (int i = 0; i < 12; i++) {
            float hourDegree = i * 30 - 60;
            String number = String.valueOf(i + 1);
            float textWidth = numberPaint.measureText(number);
            canvas.drawText(
                    number,
                    (float) (centerX + (radius * 0.8f + textWidth / 2f) * Math.cos(Math.toRadians(hourDegree))),
                    (float) (centerY + (radius * 0.8 + textHeight / 2f) * Math.sin(Math.toRadians(hourDegree))),
                    numberPaint
            );
        }
    }

    // 获取当前的时间：时、分、秒
    private Time getCurrentTime() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        return new Time(
                calendar.get(Calendar.HOUR),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
    }
}
