package com.gogaworm.praktika.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.gogaworm.praktika.controller.TimeCalculator;
import com.gogaworm.praktika.data.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 01.03.2017.
 *
 * @author ikarpova
 */
public class MonthInfoDataSource {
    // Database fields
    private SQLiteDatabase database;
    private DataReaderDbHelper dbHelper;

    public MonthInfoDataSource(Context context) {
        dbHelper = new DataReaderDbHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long loadMonthInfo(MonthInfo monthInfo) {
        Cursor cursor = database.query("MONTH",
                new String[] {"ID"}, "YEAR=? AND MONTH=?", new String[] {String.valueOf(monthInfo.year), String.valueOf(monthInfo.month)}, null, null, null);

        long id = 0;
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            id = cursor.getLong(0);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return id;
    }

    public void saveMonthInfo(MonthInfo monthInfo) {
        long id = loadMonthInfo(monthInfo);
        if (id > 0) {
            monthInfo.id = id;
            return; //already saved
        }

        ContentValues values = new ContentValues();
        values.put("YEAR", monthInfo.year);
        values.put("MONTH", monthInfo.month);
        monthInfo.id = database.insert("MONTH", null, values);
    }

    public void deleteMonthInfo(MonthInfo monthInfo) {
        database.delete("MONTH", "YEAR=? AND MONTH=?", new String[] {String.valueOf(monthInfo.year), String.valueOf(monthInfo.month)});
    }

    public List<MonthInfo> getAllMonthInfo() {
        List<MonthInfo> monthInfos = new ArrayList<MonthInfo>();

        Cursor cursor = database.query("MONTH",
                new String[] {"ID", "YEAR", "MONTH"}, null, null, null, null, "YEAR DESC, MONTH DESC");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            monthInfos.add(new MonthInfo(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2)));
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return monthInfos;
    }

    public List<Student> getStudentsForMonth(MonthInfo monthInfo) {
        List<Person> persons = getPersons(monthInfo, 0);
        List<Student> students = new ArrayList<>(persons.size());
        for (Person person : persons) {
            Student student = new Student(person);
            students.add(student);
            Person teacher = getAnyTeacherForStudent(person.id, monthInfo.id, "select PERSON_NAME.ID, PERSON_NAME.TYPE, PERSON_NAME.NAME, STUDENT_TEACHERS.TEACHER_ID " +
                    "from PERSON_NAME, STUDENT_TEACHERS, MONTH_DAY " +
                    "where STUDENT_TEACHERS.STUDENT_ID=? and PERSON_NAME.ID=STUDENT_TEACHERS.TEACHER_ID and MONTH_DAY.ID=STUDENT_TEACHERS.MONTH_DAY_ID and MONTH_DAY.MONTH_ID=? " +
                    "order by MONTH_DAY.DAY DESC");
            if (teacher != null) {
                student.teachers.add(teacher);
                Person genericTeacher = getAnyTeacherForStudent(person.id, monthInfo.id, "select PERSON_NAME.ID, PERSON_NAME.TYPE, PERSON_NAME.NAME, STUDENT_TEACHERS.TEACHER_ID " +
                        "from PERSON_NAME, STUDENT_TEACHERS, MONTH_DAY " +
                        "where STUDENT_TEACHERS.STUDENT_ID=? and PERSON_NAME.ID=STUDENT_TEACHERS.GENERAL_TEACHER_ID and MONTH_DAY.ID=STUDENT_TEACHERS.MONTH_DAY_ID and MONTH_DAY.MONTH_ID=? " +
                        "order by MONTH_DAY.DAY DESC");
                student.teachers.add(genericTeacher != null ? genericTeacher : teacher);
            }
        }
        return students;
    }

    private Person getAnyTeacherForStudent(long studentId, long monthId, String query) {
        Cursor cursor = database.rawQuery(query, new String[] {String.valueOf(studentId), String.valueOf(monthId)});
        try {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                return new Person(cursor.getLong(0), cursor.getInt(1), cursor.getString(2));
            }
        } finally {
            cursor.close();
        }
        return null;
    }


    public List<Person> getTeachersForMonth(MonthInfo monthInfo) {
        return getPersons(monthInfo, 1);
    }

    public List<Person> getPersons(MonthInfo monthInfo, int type) {
        List<Person> students = new ArrayList<>();
        Cursor cursor = database.query("PERSON_NAME", new String[] {"ID", "TYPE", "NAME"}, "TYPE=? and MONTH_ID=?", new String[]{String.valueOf(type), String.valueOf(monthInfo.id)}, "NAME", null, "NAME ASC");
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                students.add(new Person(cursor.getLong(0), cursor.getInt(1), cursor.getString(2)));
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return students;
    }

    public List<String> getPersonNames(MonthInfo monthInfo, int type) {
        Cursor cursor = database.query("PERSON_NAME", new String[] {"NAME"}, "TYPE=? and MONTH_ID=?", new String[]{String.valueOf(type), String.valueOf(monthInfo.id)}, null, null, "NAME ASC");
        return getPersonNames(cursor);
    }

    public List<String> getPersonNames(int type) {
        Cursor cursor = database.query("PERSON_NAME", new String[] {"NAME"}, "TYPE=?", new String[]{String.valueOf(type)}, "NAME", null, "NAME ASC");
        return getPersonNames(cursor);
    }

    private List<String> getPersonNames(Cursor cursor) {
        List<String> names = new ArrayList<>();
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                names.add(cursor.getString(0));
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return names;
    }

    public void savePersonsForMonth(MonthInfo monthInfo, List<String> personNames, int type) {
        List<Person> oldPersons = getPersons(monthInfo, type);
        for (int i = 0; i < oldPersons.size(); i++) {
            for (int j = 0; j < personNames.size(); j++) {
                if (personNames.get(j).equals(oldPersons.get(i).name)) {
                    oldPersons.remove(i--);
                    personNames.remove(j);
                    break;
                }
            }
        }
        //remove old links
        for (Person person : oldPersons) {
            database.delete("PERSON_NAME", "ID=?", new String[]{String.valueOf(person.id)});
        }

        //save new persons
        for (String name : personNames) {
            ContentValues values = new ContentValues();
            values.put("NAME", name);
            values.put("TYPE", type);
            values.put("MONTH_ID", monthInfo.id);
            database.insert("PERSON_NAME", null, values);
        }
    }

    public void saveMonthDay(MonthDay monthDay) {
        Cursor cursor = database.query("MONTH_DAY",
                new String[] {"ID"}, "MONTH_ID=? AND DAY=?", new String[] {String.valueOf(monthDay.monthInfo.id), String.valueOf(monthDay.day)}, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            monthDay.id = cursor.getLong(0);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();

        if (monthDay.id == 0) {
            ContentValues values = new ContentValues();
            values.put("MONTH_ID", monthDay.monthInfo.id);
            values.put("DAY", monthDay.day);
            monthDay.id = database.insert("MONTH_DAY", null, values);
        }
    }

    public List<PersonWorkingTime> loadPersonWorkingTimesByDay(long monthDayId) {
        List<PersonWorkingTime> results = new ArrayList<>();
        Cursor cursor = database.rawQuery("select WORKING_TIME.*, PERSON_NAME.NAME, PERSON_NAME.TYPE, PERSON_NAME.MONTH_ID from WORKING_TIME, PERSON_NAME " +
                        "where WORKING_TIME.MONTH_DAY_ID=? " +
                        "and WORKING_TIME.PERSON_ID=PERSON_NAME.ID order by PERSON_NAME.NAME ASC",
                new String[] {String.valueOf(monthDayId)});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            PersonWorkingTime item = new PersonWorkingTime();
            item.id = cursor.getLong(cursor.getColumnIndex("ID"));
            item.person = new Person(cursor.getLong(cursor.getColumnIndex("PERSON_ID")),
                    cursor.getInt(cursor.getColumnIndex("TYPE")),
                    cursor.getString(cursor.getColumnIndex("NAME")));
            if (cursor.getInt(cursor.getColumnIndex("TYPE")) == 0) {
                item.person = new Student(cursor.getLong(cursor.getColumnIndex("PERSON_ID")),
                        cursor.getInt(cursor.getColumnIndex("TYPE")),
                        cursor.getString(cursor.getColumnIndex("NAME")));
                ((Student) item.person).teachers = getTeachersForStudent(item.person.id, monthDayId);
            } else {
                item.person = new Person(cursor.getLong(cursor.getColumnIndex("PERSON_ID")),
                        cursor.getInt(cursor.getColumnIndex("TYPE")),
                        cursor.getString(cursor.getColumnIndex("NAME")));
            }
            item.workingTime = new WorkingTime(
                    cursor.getInt(cursor.getColumnIndex("FROM_HOUR")),
                    cursor.getInt(cursor.getColumnIndex("FROM_MINUTE")),
                    cursor.getInt(cursor.getColumnIndex("TO_HOUR")),
                    cursor.getInt(cursor.getColumnIndex("TO_MINUTE"))
            );
            results.add(item);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return results;
    }

    private List<Person> getTeachersForStudent(long studentId, long monthDayId) {
        List<Person> results = new ArrayList<>(2);
        results.add(getTeacherForStudent(studentId, monthDayId, "select PERSON_NAME.ID, PERSON_NAME.TYPE, PERSON_NAME.NAME, STUDENT_TEACHERS.TEACHER_ID " +
                "from PERSON_NAME, STUDENT_TEACHERS where STUDENT_TEACHERS.STUDENT_ID=? and STUDENT_TEACHERS.MONTH_DAY_ID=? and PERSON_NAME.ID=STUDENT_TEACHERS.TEACHER_ID"));
        results.add(getTeacherForStudent(studentId, monthDayId, "select PERSON_NAME.ID, PERSON_NAME.TYPE, PERSON_NAME.NAME, STUDENT_TEACHERS.TEACHER_ID " +
                "from PERSON_NAME, STUDENT_TEACHERS where STUDENT_TEACHERS.STUDENT_ID=? and STUDENT_TEACHERS.MONTH_DAY_ID=? and PERSON_NAME.ID=STUDENT_TEACHERS.GENERAL_TEACHER_ID"));
        return results;
    }

    private Person getTeacherForStudent(long studentId, long monthDayId, String query) {
        Cursor cursor = database.rawQuery(query, new String[] {String.valueOf(studentId), String.valueOf(monthDayId)});
        try {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                return new Person(cursor.getLong(0), cursor.getInt(1), cursor.getString(2));
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public void savePersonWorkingTime(MonthDay monthDay, List<PersonWorkingTime> personWorkingTimes) {
        // remove old values
        List<Long> oldValues = new ArrayList<>();
        Cursor cursor = database.query("WORKING_TIME", new String[] {"ID", "PERSON_ID"}, "MONTH_DAY_ID=?" , new String[] {String.valueOf(monthDay.id)}, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            long itemId = cursor.getLong(0);
            long personId = cursor.getLong(1);
            oldValues.add(itemId);

            for (PersonWorkingTime item : personWorkingTimes) {
                if (item.person.id == personId) {
                    oldValues.remove(itemId);
                    item.id = cursor.getLong(0);
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
        for (Long id : oldValues) {
            database.delete("WORKING_TIME", "ID=?", new String[] {String.valueOf(id)});
        }

        for (PersonWorkingTime personWorkingTime : personWorkingTimes) {
            ContentValues values = new ContentValues();
            values.put("MONTH_DAY_ID", monthDay.id);
            values.put("PERSON_ID", personWorkingTime.person.id);
            values.put("FROM_HOUR", personWorkingTime.workingTime.start.hour);
            values.put("FROM_MINUTE", personWorkingTime.workingTime.start.minute);
            values.put("TO_HOUR", personWorkingTime.workingTime.end.hour);
            values.put("TO_MINUTE", personWorkingTime.workingTime.end.minute);
            if (personWorkingTime.id > 0) {
                database.update("WORKING_TIME", values, "ID=?", new String[]{String.valueOf(personWorkingTime.id)});
            } else {
                database.insert("WORKING_TIME", null, values);
            }
        }

        //save teachers-students links
        database.delete("STUDENT_TEACHERS", "MONTH_DAY_ID=?", new String[] {String.valueOf(monthDay.id)});

        for (PersonWorkingTime personWorkingTime : personWorkingTimes) {
            if (personWorkingTime.person instanceof Student) {
                List<Person> teachers = ((Student) personWorkingTime.person).teachers;
                ContentValues values = new ContentValues();
                values.put("MONTH_DAY_ID", monthDay.id);
                values.put("STUDENT_ID", personWorkingTime.person.id);
                values.put("TEACHER_ID", teachers.get(0).id);
                values.put("GENERAL_TEACHER_ID", teachers.get(1).id);
                database.insert("STUDENT_TEACHERS", null, values);
            }
        }
    }

    public List<WorkingDay> getWorkingDays(MonthInfo monthInfo) {
        List<WorkingDay> workingDays = new ArrayList<>();
        List<MonthDay> monthDays = loadMonthDays(monthInfo);
        for (MonthDay monthDay : monthDays) {
            workingDays.add(createWorkingDay(monthDay));
        }
        return workingDays;
    }

    private List<Long> getStudentsByTeacher(long teacherId, long monthDayId) {
        Cursor cursor = database.query("STUDENT_TEACHERS", new String[] {"STUDENT_ID"}, "MONTH_DAY_ID=? and (TEACHER_ID=? or GENERAL_TEACHER_ID=?)",
                new String[] {String.valueOf(monthDayId), String.valueOf(teacherId), String.valueOf(teacherId)}, null, null, null);

        List<Long> studentIds = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            studentIds.add(cursor.getLong(0));
            cursor.moveToNext();
        }
        cursor.close();
        return studentIds;
    }

    private PersonWorkingTime findWorkingTimeByPerson(long personId, List<PersonWorkingTime> personWorkingTimes) {
        for (PersonWorkingTime personWorkingTime : personWorkingTimes) {
            if (personWorkingTime.person.id == personId) {
                return personWorkingTime;
            }
        }
        return null;
    }

    public List<MonthDay> loadMonthDays(MonthInfo monthInfo) {
        Cursor cursor = database.query("MONTH_DAY", new String[] {"ID", "DAY"}, "MONTH_ID=?", new String[] {String.valueOf(monthInfo.id)}, null, null, "DAY ASC");
        List<MonthDay> days = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            days.add(new MonthDay(cursor.getLong(0), monthInfo, cursor.getInt(1)));
            cursor.moveToNext();
        }
        cursor.close();
        return days;
    }

    public WorkingDay getWorkingDay(MonthDay monthDay) {
        return createWorkingDay(monthDay);
    }

    private WorkingDay createWorkingDay(MonthDay monthDay) {
        WorkingDay workingDay = new WorkingDay();
        workingDay.monthDay = monthDay;
        List<PersonWorkingTime> personWorkingTimes = loadPersonWorkingTimesByDay(monthDay.id);

        for (PersonWorkingTime personWorkingTime : personWorkingTimes) {
            if (personWorkingTime.person.type == 0) continue; //skip student

            TeacherWorkingDay teacherWorkingDay = new TeacherWorkingDay();
            workingDay.teacherWorkingDays.add(teacherWorkingDay);
            teacherWorkingDay.teacher = personWorkingTime;

            //get all students for the teacher
            List<Long> studentIds = getStudentsByTeacher(personWorkingTime.person.id, monthDay.id);
            for (Long id : studentIds) {
                teacherWorkingDay.students.add(findWorkingTimeByPerson(id, personWorkingTimes));
            }
            teacherWorkingDay.calculatedTime = TimeCalculator.calculateTeacherWorkingDay(teacherWorkingDay);
            TimeUnit maxTime = new TimeUnit(8, 0);
            teacherWorkingDay.exceeds = teacherWorkingDay.calculatedTime.laterThan(maxTime);
            if (teacherWorkingDay.exceeds) {
                teacherWorkingDay.calculatedTime = maxTime;
            }

        }
        return workingDay;
    }

    public boolean hasPersonInfo(MonthInfo monthInfo, String personName) {
        Cursor cursor = database.rawQuery("select WORKING_TIME.ID from WORKING_TIME, PERSON_NAME where PERSON_NAME.MONTH_ID=? and PERSON_NAME.NAME=? and WORKING_TIME.PERSON_ID=PERSON_NAME.ID",
                new String[]{String.valueOf(monthInfo.id), personName});
        cursor.moveToFirst();
        try {
            return !cursor.isAfterLast();
        } finally {
            cursor.close();
        }
    }
}
