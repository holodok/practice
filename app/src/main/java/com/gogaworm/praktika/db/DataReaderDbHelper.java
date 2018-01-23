package com.gogaworm.praktika.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created on 01.03.2017.
 *
 * @author ikarpova
 */
public class DataReaderDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Praktika.db";

    public DataReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE MONTH (ID INTEGER PRIMARY KEY, YEAR INTEGER, MONTH INTEGER);");
        db.execSQL("CREATE TABLE MONTH_DAY (ID INTEGER PRIMARY KEY, MONTH_ID INTEGER, DAY INTEGER); ");
        db.execSQL("CREATE TABLE PERSON_NAME (ID INTEGER PRIMARY KEY, NAME TEXT, TYPE INTEGER, MONTH_ID INTEGER); ");
        db.execSQL("CREATE TABLE WORKING_TIME (ID INTEGER PRIMARY KEY, MONTH_DAY_ID INTEGER, PERSON_ID INTEGER, FROM_HOUR INTEGER, FROM_MINUTE INTEGER, TO_HOUR INTEGER, TO_MINUTE INTEGER); ");
        db.execSQL("CREATE TABLE STUDENT_TEACHERS (ID INTEGER PRIMARY KEY, MONTH_DAY_ID INTEGER, STUDENT_ID INTEGER, TEACHER_ID INTEGER, GENERAL_TEACHER_ID INTEGER); ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS WORKING_TIME;");
        db.execSQL("DROP TABLE IF EXISTS STUDENT_TEACHERS;");
        db.execSQL("DROP TABLE IF EXISTS MONTH_DAY;");
        db.execSQL("DROP TABLE IF EXISTS PERSON_NAME;");
        db.execSQL("DROP TABLE IF EXISTS MONTH;");
        onCreate(db);
    }
}
