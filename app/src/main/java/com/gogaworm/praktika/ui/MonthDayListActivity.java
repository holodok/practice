package com.gogaworm.praktika.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.gogaworm.praktika.R;
import com.gogaworm.praktika.data.MonthDay;
import com.gogaworm.praktika.data.MonthInfo;
import com.gogaworm.praktika.data.TeacherWorkingDay;
import com.gogaworm.praktika.data.WorkingDay;
import com.gogaworm.praktika.db.MonthInfoDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 05.03.2017.
 *
 * @author ikarpova
 */
public class MonthDayListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<WorkingDay>> {
    private MonthInfo monthInfo;
    private ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthday_list);

        monthInfo = getIntent().getParcelableExtra("monthInfo");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(monthInfo.getTitle(this));

        listView = (ListView) findViewById(android.R.id.list);

        getSupportLoaderManager().initLoader(110, null, this).forceLoad();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent intent = new Intent(this, AddDayInfoActivity.class);
                intent.putExtra("monthInfo", monthInfo);
                startActivityForResult(intent, 100);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            getSupportLoaderManager().getLoader(110).forceLoad();
        }
    }

    @Override
    public Loader<List<WorkingDay>> onCreateLoader(int id, Bundle args) {
        return new WorkingDaysLoader(this, monthInfo);
    }

    @Override
    public void onLoadFinished(Loader<List<WorkingDay>> loader, List<WorkingDay> data) {
        if (((WorkingDaysLoader)loader).exception != null) {
            Toast.makeText(this, ((WorkingDaysLoader)loader).exception.getMessage(), Toast.LENGTH_LONG).show();
        }
        WorkingDay generalInfo = new WorkingDay();
        generalInfo.monthDay = new MonthDay(0, monthInfo, 0);

        for (WorkingDay workingDay : data) {
            for (TeacherWorkingDay teacherWorkingDay : workingDay.teacherWorkingDays) {
                long personId = teacherWorkingDay.teacher.person.id;

                boolean found = false;
                for (TeacherWorkingDay generalDay : generalInfo.teacherWorkingDays) {
                    if (generalDay.teacher.person.id == personId) {
                        generalDay.calculatedTime = generalDay.calculatedTime.plus(teacherWorkingDay.calculatedTime);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    generalInfo.teacherWorkingDays.add(new TeacherWorkingDay(teacherWorkingDay.teacher.person, teacherWorkingDay.calculatedTime));
                }
            }
        }

        data.add(0, generalInfo);
        listView.setAdapter(new MonthDayAdapter(MonthDayListActivity.this, data));
    }

    @Override
    public void onLoaderReset(Loader<List<WorkingDay>> loader) {

    }

    private class MonthDayAdapter extends BaseAdapter {
        private Context context;
        private List<Object> data;
        private int monthStartPosition;

        MonthDayAdapter(Context context, List<WorkingDay> data) {
            this.context = context;
            if (data.size() > 0) {
                WorkingDay workingDay = data.get(0);
                monthStartPosition = 1 + workingDay.teacherWorkingDays.size();
            }
            this.data = new ArrayList<>();
            for (WorkingDay item : data) {
                this.data.add(item.monthDay);
                this.data.addAll(item.teacherWorkingDays);
            }
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Object item = getItem(position);

            final int itemViewType = getItemViewType(position);
            if (convertView == null) {
                switch (itemViewType) {
                    case 0:
                        convertView = LayoutInflater.from(context).inflate(R.layout.list_item_month_day, parent, false);
                        break;
                    case 1:
                        convertView = LayoutInflater.from(context).inflate(R.layout.list_item_month_day_time, parent, false);
                        break;
                }
                convertView.setOnClickListener(null);
            }
            switch (itemViewType) {
                case 0:
                    TextView dateView = (TextView) convertView.findViewById(R.id.date);
                    View editButton = convertView.findViewById(R.id.editButton);

                    final MonthDay monthDay = (MonthDay) item;
                    editButton.setVisibility(monthDay.day == 0 ? View.GONE : View.VISIBLE);
                    if (monthDay.day == 0) {
                        dateView.setText(R.string.label_general_time);
                        dateView.setTextColor(getResources().getColor(R.color.primaryInvertedText));
                        convertView.setBackgroundColor(getResources().getColor(R.color.overalTimeHeaderBackground));
                    } else {
                        convertView.setBackgroundColor(getResources().getColor(R.color.month_panel));
                        dateView.setTextColor(getResources().getColor(R.color.secondaryText));
                        dateView.setText(String.format("%d %s", monthDay.day, context.getResources().getStringArray(R.array.months_for_dates)[monthDay.monthInfo.month]));
                        editButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //todo: should be edit and view should ne another button
                                //notifyDataSetInvalidated();
                                Intent intent = new Intent(MonthDayListActivity.this, TeacherWorkingDayActivity.class);
                                intent.putExtra("monthDay", monthDay);
                                startActivity(intent);
                            }
                        });
                    }
                    break;
                case 1:
                    convertView.setBackgroundColor(getResources().getColor(position < monthStartPosition ? R.color.overralTimeItemBackground : android.R.color.transparent));
                    TextView personView = (TextView) convertView.findViewById(R.id.person);
                    TextView timeView = (TextView) convertView.findViewById(R.id.time);

                    TeacherWorkingDay workingDay = (TeacherWorkingDay) item;
                    personView.setText(workingDay.teacher.person.name);
                    timeView.setText(context.getString(R.string.label_time_format, workingDay.calculatedTime.hour, workingDay.calculatedTime.minute));
                    timeView.setTextColor(workingDay.exceeds ? Color.RED : context.getResources().getColor(R.color.secondaryText));
                    timeView.setTypeface(timeView.getTypeface(), position < monthStartPosition ? Typeface.BOLD : Typeface.NORMAL);
                    break;
            }

            return convertView;
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position) instanceof MonthDay ? 0 : 1;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }
    }

    private static class WorkingDaysLoader extends AsyncTaskLoader<List<WorkingDay>> {
        MonthInfoDataSource dataSource;
        private MonthInfo monthInfo;
        Exception exception;

        WorkingDaysLoader(Context context, MonthInfo monthInfo) {
            super(context);
            dataSource = new MonthInfoDataSource(context);
            this.monthInfo = monthInfo;
        }

        @Override
        public List<WorkingDay> loadInBackground() {
            dataSource.open();
            try {
                return dataSource.getWorkingDays(monthInfo);
            } catch (Exception ex) {
                exception = ex;
                return new ArrayList<>(0);
            } finally {
                dataSource.close();
            }
        }
    }
}
