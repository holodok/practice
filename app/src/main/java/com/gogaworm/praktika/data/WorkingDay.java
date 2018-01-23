package com.gogaworm.praktika.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 25.03.2017.
 *
 * @author Ilona
 */
public class WorkingDay {
    public MonthDay monthDay;
    public List<TeacherWorkingDay> teacherWorkingDays;

    public WorkingDay() {
        teacherWorkingDays = new ArrayList<>();
    }
}
