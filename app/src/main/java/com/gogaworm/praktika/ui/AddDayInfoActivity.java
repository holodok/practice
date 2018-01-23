package com.gogaworm.praktika.ui;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import com.gogaworm.praktika.R;
import com.gogaworm.praktika.data.MonthDay;
import com.roomorama.caldroid.CaldroidListener;

import java.util.Calendar;
import java.util.Date;

/**
 * Created on 08.03.2017.
 *
 * @author ikarpova
 */
public class AddDayInfoActivity extends WizardActivity {
    private MonthDay monthDay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        monthDay = new MonthDay();
        monthDay.monthInfo = monthInfo;
    }

    @Override
    protected Fragment getNextStep(int step) {
        switch (step) {
            case 0: {
                final SelectDayFragment selectDayFragment = SelectDayFragment.getInstance(monthInfo);
                selectDayFragment.setCaldroidListener(new CaldroidListener() {
                    @Override
                    public void onSelectDate(Date date, View view) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        monthDay.day = calendar.get(Calendar.DAY_OF_MONTH);
                    }
                });

                return selectDayFragment;
            }
            case 1: {
                getSupportActionBar().setTitle(monthDay.getTitle(this));
                return AddPersonDayTimeFragment.createInstance(monthDay);
            }
            default:
                return null;
        }
    }

    @Override
    protected int getStepCount() {
        return 2;
    }

    @Override
    protected void onLastStep() {}

    @Override
    protected boolean canGoBack(int step) {
        return false;
    }

    void saveDayInfo() {
        getDataSource().saveMonthDay(monthDay);
    }
}
