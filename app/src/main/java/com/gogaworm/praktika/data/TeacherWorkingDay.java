package com.gogaworm.praktika.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 17.03.2017.
 *
 * @author ikarpova
 */
public class TeacherWorkingDay {
    public PersonWorkingTime teacher;
    public List<PersonWorkingTime> students;
    public TimeUnit calculatedTime;
    public boolean exceeds;

    public TeacherWorkingDay() {
        students = new ArrayList<>();
    }

    public TeacherWorkingDay(Person teacher, TimeUnit calculatedTime) {
        this.teacher = new PersonWorkingTime();
        this.teacher.person = teacher;
        this.calculatedTime = calculatedTime;
    }
}
