package com.gogaworm.praktika.ui;

import android.content.Context;
import com.gogaworm.praktika.R;

/**
 * Created on 26.03.2017.
 *
 * @author Ilona
 */
public class StudentColorSelector {
    private int[] COLORS;

    public StudentColorSelector(Context context) {
        COLORS = new int[] {
                context.getResources().getColor(R.color.chartTeacherColor),
                context.getResources().getColor(R.color.chartStudentColor1),
                context.getResources().getColor(R.color.chartStudentColor2),
                context.getResources().getColor(R.color.chartStudentColor3),
                context.getResources().getColor(R.color.chartStudentColor4),
                context.getResources().getColor(R.color.chartStudentColor5)
        };
    }

    public int getColor(int position) {
        return COLORS[position];
    }
}
