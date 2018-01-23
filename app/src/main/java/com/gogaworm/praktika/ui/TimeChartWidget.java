package com.gogaworm.praktika.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import com.gogaworm.praktika.R;
import com.gogaworm.praktika.controller.TimeCalculator;
import com.gogaworm.praktika.data.PersonWorkingTime;
import com.gogaworm.praktika.data.TeacherWorkingDay;
import com.gogaworm.praktika.data.TimeUnit;
import com.gogaworm.praktika.data.WorkingTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 26.03.2017.
 *
 * @author Ilona
 */
public class TimeChartWidget extends View {
    private TeacherWorkingDay teacherWorkingDay;

    private int itemHeight = 16; //load from dimensions

    private final Paint teacherPaint;
    private final Paint studentPaint;
    private final Paint finalPaint;
    private final Paint extraPaint;
    private final Paint gridPaint;

    private StudentColorSelector studentColorSelector;

    private int minHour;
    private int maxHour;
    private List<WorkingTime> studentTime;
    private List<WorkingTime> periods;
    private TimeUnit extraTime;
    private TimeUnit drawnExtraTime = new TimeUnit();

    public TimeChartWidget(Context context) {
        this(context, null);
    }

    public TimeChartWidget(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        studentColorSelector = new StudentColorSelector(context);

        teacherPaint = new Paint();
        teacherPaint.setColor(getResources().getColor(R.color.chartTeacherColor));

        studentPaint = new Paint();
        finalPaint = new Paint();
        finalPaint.setColor(getResources().getColor(R.color.chartFinalColor));

        extraPaint = new Paint();
        extraPaint.setColor(Color.WHITE);
        extraPaint.setAlpha(150);

        gridPaint = new Paint();
        gridPaint.setColor(getResources().getColor(R.color.chartGridColor));
        gridPaint.setTextSize(14);
    }

    public void setup(TeacherWorkingDay teacherWorkingDay) {
        this.teacherWorkingDay = teacherWorkingDay;
        minHour = teacherWorkingDay.teacher.workingTime.start.hour - 1;
        maxHour = teacherWorkingDay.teacher.workingTime.end.hour + 1;

        studentTime = new ArrayList<>();

        for (PersonWorkingTime student : teacherWorkingDay.students) {
            if (student.workingTime.start.hour < minHour) {
                minHour = student.workingTime.start.hour - 1;
            }
            if (student.workingTime.end.hour > maxHour) {
                maxHour = student.workingTime.end.hour + 1;
            }
            studentTime.add(student.workingTime);
        }
        //move to initialization and count only once
        periods = TimeCalculator.getPeriods(teacherWorkingDay.teacher.workingTime, studentTime);
        if (teacherWorkingDay.exceeds) {
            extraTime = TimeCalculator.calculateTeacherWorkingDay(teacherWorkingDay).minus(new TimeUnit(8, 0));
        }

        invalidate();
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (teacherWorkingDay == null) return;

        float border = 8f;

        float hourPart = (getWidth() - border * 2) / (maxHour - minHour);

        //draw teacher time
        WorkingTime teacherWorkingTime = teacherWorkingDay.teacher.workingTime;
        float left = border + getTimePart(hourPart, teacherWorkingTime.start);
        float top = 0;
        float right = border + getTimePart(hourPart, teacherWorkingTime.end);
        float bottom = itemHeight;
        canvas.drawRect(left, top, right, bottom, teacherPaint);

        //draw student time
        List<PersonWorkingTime> students = teacherWorkingDay.students;
        for (int i = 0; i < students.size(); i++) {
            WorkingTime workingTime = students.get(i).workingTime;

            left = border + getTimePart(hourPart, workingTime.start);
            right = border + getTimePart(hourPart, workingTime.end);
            top = (i + 1) * itemHeight;
            bottom = top + itemHeight;
            studentPaint.setColor(studentColorSelector.getColor(i + 1));
            canvas.drawRect(left, top, right, bottom, studentPaint);
        }

        //draw worked time
        top += 2 * itemHeight;
        bottom = top + itemHeight;

        for (WorkingTime workingTime : periods) {
            left = border + getTimePart(hourPart, workingTime.start);
            right = border + getTimePart(hourPart, workingTime.end);
            canvas.drawRect(left, top, right, bottom, finalPaint);
        }

        if (teacherWorkingDay.exceeds) {
            //draw extra time
            drawnExtraTime.hour = extraTime.hour;
            drawnExtraTime.minute = extraTime.minute;

            for (WorkingTime workingTime : periods) {
                if (workingTime.end.minus(workingTime.start).laterThan(drawnExtraTime)) {
                    //will fit into this period
                    left = border + getTimePart(hourPart, workingTime.start);
                    right = border + getTimePart(hourPart, workingTime.start.plus(drawnExtraTime));
                    canvas.drawRect(left, top, right, bottom, extraPaint);
                    break;
                } else {
                    drawnExtraTime = drawnExtraTime.minus(workingTime.end.minus(workingTime.start));
                }
            }
        }

        //draw section lines
        float startX = 8;
        float startY = 0;
        float stopY = itemHeight * (3 + students.size());

        for (int i = minHour; i <= maxHour; i++) {
            canvas.drawLine(startX, startY, startX, stopY, gridPaint);
            canvas.drawText(String.valueOf(i), startX - 7, stopY + 14, gridPaint);
            startX += hourPart;
        }
    }

    private float getTimePart(float hourPart, TimeUnit timeUnit) {
        return hourPart * (timeUnit.hour - minHour) + hourPart * timeUnit.minute /60f;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredHeight = 0;

        if (teacherWorkingDay != null) {
            desiredHeight = (4 + teacherWorkingDay.students.size()) * itemHeight;
        }

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int height;

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        setMeasuredDimension(widthSize, height);
    }
}
