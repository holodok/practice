package com.gogaworm.praktika.controller;

import com.gogaworm.praktika.data.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created on 17.03.2017.
 *
 * @author ikarpova
 */
public class TimeCalculator {

    public static TimeUnit calculateTeacherWorkingDay(TeacherWorkingDay teacherWorkingDay) {
        List<WorkingTime> studentsTime = new ArrayList<>(teacherWorkingDay.students.size());
        for (PersonWorkingTime personWorkingTime : teacherWorkingDay.students) {
            studentsTime.add(personWorkingTime.workingTime);
        }
        return calculateDayTime(teacherWorkingDay.teacher.workingTime, studentsTime);
    }

    public static TimeUnit calculateDayTime(WorkingTime teacherTime, List<WorkingTime> studentsTime) {
        List<WorkingTime> periods = getPeriods(teacherTime, studentsTime);

        //calculate final time
        TimeUnit timeUnit = new TimeUnit();
        for (WorkingTime period : periods) {
            timeUnit = timeUnit.plus(period.end.minus(period.start));
        }
        return timeUnit;
    }

    public static List<WorkingTime> getPeriods(WorkingTime teacherTime, List<WorkingTime> studentsTime) {
        List<WorkingTime> periods = new ArrayList<>(studentsTime.size());
        for (WorkingTime studentTime : studentsTime) {
            periods.add(calculateTimeForStudent(teacherTime, studentTime));
        }

        if (periods.size() > 1) {
            //sort periods
            Collections.sort(periods, new Comparator<WorkingTime>() {
                @Override
                public int compare(WorkingTime o1, WorkingTime o2) {
                    return o1.start.earlierThan(o2.start) ? -1 : o1.start.laterThan(o2.start) ? 1 : 0;
                }
            });

            //merge
            for (int i = 1; i < periods.size(); i++) {
                WorkingTime first = periods.get(i - 1);
                WorkingTime second = periods.get(i);

                if (first.end.laterThan(second.start)) {
                    if (first.end.earlierThan(second.end)) {
                        first.end = second.end;
                    }
                    periods.remove(i--);
                }
            }
        }

        return periods;
    }

    private static WorkingTime calculateTimeForStudent(WorkingTime teacherTime, WorkingTime studentTime) {
        WorkingTime workingTime = new WorkingTime();
        if (studentTime.start.earlierThan(teacherTime.start)) { //student came early
            if (studentTime.end.laterThan(teacherTime.start)) {
                if (studentTime.end.earlierThan(teacherTime.end)) {
                    workingTime.start.hour = teacherTime.start.hour;
                    workingTime.start.minute = teacherTime.start.minute;
                    workingTime.end.hour = studentTime.end.hour;
                    workingTime.end.minute = studentTime.end.minute;
                } else {
                    workingTime.start.hour = teacherTime.start.hour;
                    workingTime.start.minute = teacherTime.start.minute;
                    workingTime.end.hour = teacherTime.end.hour;
                    workingTime.end.minute = teacherTime.end.minute;
                }
            } else {
                workingTime.start.hour = 0;
                workingTime.start.minute = 0;
                workingTime.end.hour = 0;
                workingTime.end.minute = 0;
            }
        } else if (studentTime.start.laterThan(teacherTime.start)) {
            if (studentTime.start.earlierThan(teacherTime.end)) {
                if (studentTime.end.earlierThan(teacherTime.end)) {
                    workingTime.start.hour = studentTime.start.hour;
                    workingTime.start.minute = studentTime.start.minute;
                    workingTime.end.hour = studentTime.end.hour;
                    workingTime.end.minute = studentTime.end.minute;
                } else {
                    workingTime.start.hour = studentTime.start.hour;
                    workingTime.start.minute = studentTime.start.minute;
                    workingTime.end.hour = teacherTime.end.hour;
                    workingTime.end.minute = teacherTime.end.minute;
                }
            } else {
                workingTime.start.hour = 0;
                workingTime.start.minute = 0;
                workingTime.end.hour = 0;
                workingTime.end.minute = 0;
            }
        } else { //hour equals
            workingTime.start.hour = teacherTime.start.hour;
            workingTime.start.minute = studentTime.start.minute >= teacherTime.start.minute ? studentTime.start.minute : teacherTime.start.minute;
            if (teacherTime.end.laterThan(studentTime.end)) {
                workingTime.end.hour = studentTime.end.hour;
                workingTime.end.minute = studentTime.end.minute;
            } else {
                workingTime.end.hour = teacherTime.end.hour;
                workingTime.end.minute = teacherTime.end.minute;
            }
        }
        return workingTime;
    }
}
