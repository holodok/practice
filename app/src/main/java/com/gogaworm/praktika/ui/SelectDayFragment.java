package com.gogaworm.praktika.ui;

import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import com.gogaworm.praktika.R;
import com.gogaworm.praktika.data.MonthDay;
import com.gogaworm.praktika.data.MonthInfo;
import com.gogaworm.praktika.db.MonthInfoDataSource;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created on 11.03.2017.
 *
 * @author Ilona
 */
public class SelectDayFragment extends CaldroidFragment implements OnSaveListener {
    private ColorDrawable selectedColor;
    private ColorDrawable alreadyFilledColor;
    private List<MonthDay> filledDays;


    public static SelectDayFragment getInstance(MonthInfo monthInfo) {
        final SelectDayFragment caldroidFragment = new SelectDayFragment();
        Bundle args = new Bundle();
        final Calendar calendar = Calendar.getInstance();
        calendar.set(monthInfo.year, monthInfo.month, 1);
        args.putInt(CaldroidFragment.MONTH, calendar.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, calendar.get(Calendar.YEAR));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String minDate = sdf.format(calendar.getTime());
        args.putString(CaldroidFragment.MIN_DATE, minDate);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        args.putString(CaldroidFragment.MAX_DATE, sdf.format(calendar.getTime()));
        args.putBoolean(CaldroidFragment.SHOW_NAVIGATION_ARROWS, false);
        args.putInt(CaldroidFragment.START_DAY_OF_WEEK, Calendar.MONDAY);
        args.putParcelable("monthInfo", monthInfo);
        caldroidFragment.setArguments(args);
        return caldroidFragment;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // get values
        super.onCreate(savedInstanceState);

        final MonthInfo monthInfo = getArguments().getParcelable("monthInfo");

        selectedColor = new ColorDrawable(getResources().getColor(R.color.colorAccent));
        alreadyFilledColor = new ColorDrawable(getResources().getColor(R.color.colorAccent100));


        new AsyncTask<Void, Void, List<MonthDay>>() {
            MonthInfoDataSource dataSource;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dataSource = new MonthInfoDataSource(getActivity());
            }

            @Override
            protected List<MonthDay> doInBackground(Void... voids) {
                dataSource.open();
                try {
                    return dataSource.loadMonthDays(monthInfo);
                } finally {
                    dataSource.close();
                }
            }

            @Override
            protected void onPostExecute(List<MonthDay> monthDays) {
                super.onPostExecute(monthDays);
                filledDays = monthDays;
                showFilledDates();

                if (monthDays.size() > 0) {
                    Calendar calendar = Calendar.getInstance();
                    MonthDay monthDay = monthDays.get(monthDays.size() - 1);
                    int day = calendar.getMaximum(Calendar.DAY_OF_MONTH) > monthDay.day ? (monthDay.day + 1) : 1;
                    calendar.set(Calendar.DAY_OF_MONTH, day);
                    getCaldroidListener().onSelectDate(calendar.getTime(), null);
                } else {
                    refreshView();
                }
            }
        }.execute();
    }

    @Override
    public void setCaldroidListener(CaldroidListener caldroidListener) {
        super.setCaldroidListener(new SelectionAwareListener(caldroidListener));
    }

    @Override
    public boolean canGoNext() {
        return true;
    }

    @Override
    public void save() {
        AddDayInfoActivity wizardActivity = (AddDayInfoActivity) getActivity();
        wizardActivity.saveDayInfo();
    }

    private void showFilledDates() {
        final Calendar calendar = Calendar.getInstance();
        for (MonthDay monthDay : filledDays) {
            calendar.set(monthDay.monthInfo.year, monthDay.monthInfo.month, 1);
            calendar.set(Calendar.DAY_OF_MONTH, monthDay.day);
            setBackgroundDrawableForDate(alreadyFilledColor, calendar.getTime());
        }
    }

    private class SelectionAwareListener extends CaldroidListener {
        private CaldroidListener delegate;
        Date selectedDate;

        SelectionAwareListener(CaldroidListener delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onSelectDate(Date date, View view) {
            if (selectedDate != null) {
                clearBackgroundDrawableForDate(selectedDate);
                showFilledDates();
            }
            selectedDate = date;
            delegate.onSelectDate(date, view);
            setBackgroundDrawableForDate(selectedColor, date);
            refreshView();
        }
    }
}
