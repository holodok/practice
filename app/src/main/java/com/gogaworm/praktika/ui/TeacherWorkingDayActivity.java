package com.gogaworm.praktika.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.gogaworm.praktika.R;
import com.gogaworm.praktika.data.*;
import com.gogaworm.praktika.db.MonthInfoDataSource;

import java.util.*;

public class TeacherWorkingDayActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<WorkingDay> {
    private MonthDay monthDay;
    private int personId;

    private Map<Person, TeacherWorkingDay> teacherWorkingDayMap = new HashMap<>();
    private Spinner teacherSpinner;
    private WorkingTimeAdapter workingTimeAdapter;
    private TimeChartWidget chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_working_day);

        ListView listView = (ListView) findViewById(android.R.id.list);
        workingTimeAdapter = new WorkingTimeAdapter(this);
        listView.setAdapter(workingTimeAdapter);
        teacherSpinner = (Spinner) findViewById(R.id.teacherSpinner);

        teacherSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                Person person = (Person) adapterView.getItemAtPosition(position);
                TeacherWorkingDay teacherWorkingDay = teacherWorkingDayMap.get(person);
                workingTimeAdapter.setup(teacherWorkingDay);
                chart.setup(teacherWorkingDay);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        chart = (TimeChartWidget) findViewById(R.id.chart);

        monthDay = getIntent().getParcelableExtra("monthDay");
        //personId = getIntent().getParcelableExtra("personId");
        getSupportActionBar().setTitle(monthDay.getTitle(this));

        getSupportLoaderManager().initLoader(120, null, this).forceLoad();
    }

    @Override
    public Loader<WorkingDay> onCreateLoader(int id, Bundle args) {
        return new WorkingDayLoader(this, monthDay);
    }

    @Override
    public void onLoadFinished(Loader<WorkingDay> loader, WorkingDay data) {
        teacherWorkingDayMap.clear();
        for (TeacherWorkingDay teacherWorkingDay : data.teacherWorkingDays) {
            teacherWorkingDayMap.put(teacherWorkingDay.teacher.person, teacherWorkingDay);
        }

        //setup spinner
        List<Person> teachers = new ArrayList<>(teacherWorkingDayMap.keySet());
        Collections.sort(teachers, new Comparator<Person>() {
            @Override
            public int compare(Person person, Person person2) {
                return person.name.compareTo(person2.name);
            }
        });
        teacherSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, teachers));
    }

    @Override
    public void onLoaderReset(Loader<WorkingDay> loader) {}

    private static class WorkingDayLoader extends AsyncTaskLoader<WorkingDay> {
        MonthInfoDataSource dataSource;
        private MonthDay monthDay;

        WorkingDayLoader(Context context, MonthDay monthDay) {
            super(context);
            dataSource = new MonthInfoDataSource(context);
            this.monthDay = monthDay;
        }

        @Override
        public WorkingDay loadInBackground() {
            dataSource.open();
            try {
                return dataSource.getWorkingDay(monthDay);
            } finally {
                dataSource.close();
            }
        }
    }

    private class WorkingTimeAdapter extends BaseAdapter {
        List<Object> data;
        private Context context;

        WorkingTimeAdapter(Context context) {
            this.context = context;
            data = new ArrayList<>();
        }

        void setup(TeacherWorkingDay teacherWorkingDay) {
            data.clear();
            data.add(teacherWorkingDay.teacher);
            data.addAll(teacherWorkingDay.students);
            data.add(teacherWorkingDay);
            notifyDataSetChanged();
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
            if (convertView == null) {
                convertView = new PersonTimeWidget(context);
            }
            Object item = getItem(position);
            if (item instanceof PersonWorkingTime) {
                ((PersonTimeWidget) convertView).setup((PersonWorkingTime) item, position);
            } else {
                TeacherWorkingDay teacherWorkingDay = (TeacherWorkingDay) item;
                ((PersonTimeWidget) convertView).setupGeneral(teacherWorkingDay.calculatedTime, teacherWorkingDay.exceeds);
            }
            return convertView;
        }
    }
}
