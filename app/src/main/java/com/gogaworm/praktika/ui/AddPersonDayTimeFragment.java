package com.gogaworm.praktika.ui;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.*;
import android.widget.*;
import com.gogaworm.praktika.R;
import com.gogaworm.praktika.data.*;

import java.util.*;

/**
 * Created on 08.03.2017.
 *
 * 1 shift: 08:00-16:00, 2 shift 12:15-20:15
 * @author ikarpova
 */
public class AddPersonDayTimeFragment extends WizardFragment {
    private Spinner selectPerson;
    private PersonAdapter spinnerAdapter;
    private MaskedEditText workingTimeView;
    private ListView listView;
    private PersonTimeAdapter personTimeAdapter;

    private View addTimePanel;
    private View selectTeacherPanel;
    private Spinner selectTeacherSpinner;
    private Spinner selectGeneralTeacherSpinner;
    private MonthDay monthDay;

    public static AddPersonDayTimeFragment createInstance(MonthDay monthDay) {
        AddPersonDayTimeFragment fragment = new AddPersonDayTimeFragment();
        Bundle args = new Bundle();
        args.putParcelable("monthDay", monthDay);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.fragment_add_person_day, container, false);

        View listHeader = inflater.inflate(R.layout.panel_add_person_time, null);
        listView = (ListView) parentView.findViewById(android.R.id.list);
        listView.addHeaderView(listHeader);

        selectPerson = (Spinner) parentView.findViewById(R.id.selectPerson);
        workingTimeView = (MaskedEditText) parentView.findViewById(R.id.workingTime);
        workingTimeView.setMask("99:99 - 99:99");
        workingTimeView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                if (focused) {
                    //
                }
            }
        });
        View addPersonButton = parentView.findViewById(R.id.addPersonButton);
        addPersonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPersonTime();
            }
        });

        registerForContextMenu(listView);
        addTimePanel = parentView.findViewById(R.id.addTimePanel);
        selectTeacherPanel = parentView.findViewById(R.id.selectTeacherPanel);
        selectTeacherSpinner = (Spinner) parentView.findViewById(R.id.selectTeacher);
        selectGeneralTeacherSpinner = (Spinner) parentView.findViewById(R.id.selectGeneralTeacher);
        parentView.findViewById(R.id.firstShiftButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                workingTimeView.setText("08001600");
            }
        });
        parentView.findViewById(R.id.secondShiftButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                workingTimeView.setText("12152015");
            }
        });
        parentView.findViewById(R.id.middleShiftButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                workingTimeView.setText("10001830");
            }
        });

        selectPerson.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Person person = (Person) parent.getItemAtPosition(position);
                selectTeacherPanel.setVisibility(person.type == 0 ? View.VISIBLE : View.GONE);
                if (person instanceof Student) {
                    Student student = (Student) person;
                    if (student.teachers != null && student.teachers.size() == 2) {
                        selectTeacherSpinner.setSelection(((ArrayAdapter<Person>) selectTeacherSpinner.getAdapter()).getPosition(student.teachers.get(0)));
                        selectGeneralTeacherSpinner.setSelection(((ArrayAdapter<Person>) selectGeneralTeacherSpinner.getAdapter()).getPosition(student.teachers.get(1)));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        monthDay = getArguments().getParcelable("monthDay");

        loadData();

        return parentView;
    }

    private void loadData() {
        new AsyncTask<Void, Void, List<Person>>() {
            List<Person> teachers;
            List<PersonWorkingTime> personWorkingTimes;

            @Override
            protected List<Person> doInBackground(Void... params) {
                List<Person> personList = new ArrayList<>();
                personList.addAll(dataSource.getStudentsForMonth(monthInfo));
                teachers = dataSource.getTeachersForMonth(monthInfo);
                personList.addAll(teachers);

                personWorkingTimes = dataSource.loadPersonWorkingTimesByDay(monthDay.id);

                return personList;
            }

            @Override
            protected void onPostExecute(List<Person> result) {
                super.onPostExecute(result);
                selectTeacherSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, teachers));
                selectGeneralTeacherSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, teachers));
                setupSpinner(result);

                personTimeAdapter = new PersonTimeAdapter(getContext(), personWorkingTimes);
                listView.setAdapter(personTimeAdapter);
                for (PersonWorkingTime personWorkingTime : personWorkingTimes) {
                    spinnerAdapter.remove(personWorkingTime.person);
                }
                if (spinnerAdapter.getCount() == 0) {
                    addTimePanel.setVisibility(View.GONE);
                }
            }
        }.execute();
    }

    @Override
    public boolean canGoNext() {
        return true;
    }

    @Override
    public void save() {
        // save working time
        dataSource.savePersonWorkingTime(monthDay, personTimeAdapter.values);
    }

    private void setupSpinner(List<Person> persons) {
        spinnerAdapter = new PersonAdapter(getContext(), persons);
        selectPerson.setAdapter(spinnerAdapter);
    }

    private void addPersonTime() {
        WorkingTime workingTime = validateWorkingTime();
        if (workingTime == null) return;

        PersonWorkingTime personWorkingTime = new PersonWorkingTime();
        Person person = (Person) selectPerson.getSelectedItem();
        personWorkingTime.person = person.type == 0 ? new Student(person) : person;
        personWorkingTime.workingTime = workingTime;
        personTimeAdapter.add(personWorkingTime);

        //add teachers
        if (personWorkingTime.person.type == 0) {
            Student student = (Student) personWorkingTime.person;
            student.teachers.add((Person) selectTeacherSpinner.getSelectedItem());
            student.teachers.add((Person) selectGeneralTeacherSpinner.getSelectedItem());
        }

        spinnerAdapter.remove(personWorkingTime.person);
        workingTimeView.setText("");
        // check if all time input, hide add form
        if (spinnerAdapter.getCount() == 0) {
            addTimePanel.setVisibility(View.GONE);
        }
    }

    private WorkingTime validateWorkingTime() {
        String value = workingTimeView.getText(true).toString();
        if (value.length() != 8) {
            workingTimeView.setError(getString(R.string.error_bad_time));
            return null;
        }
        int fromHour = Integer.valueOf(value.substring(0, 2));
        int fromMinute = Integer.valueOf(value.substring(2, 4));
        int toHour = Integer.valueOf(value.substring(4, 6));
        int toMinute = Integer.valueOf(value.substring(6, 8));

        if (fromHour > 23 || toHour > 23 || fromHour > toHour) {
            workingTimeView.setError(getString(R.string.error_bad_time));
            return null;
        }
        if (fromMinute > 59 || toMinute > 59 || fromHour == toHour && fromMinute > toMinute) {
            workingTimeView.setError(getString(R.string.error_bad_time));
            return null;
        }

        return new WorkingTime(fromHour, fromMinute, toHour, toMinute);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.menu_context_main, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.action_delete:
            case R.id.action_edit:
                PersonWorkingTime personWorkingTime = (PersonWorkingTime) listView.getAdapter().getItem(info.position);
                personTimeAdapter.remove(personWorkingTime);
                int position = spinnerAdapter.add(personWorkingTime.person);
                addTimePanel.setVisibility(View.VISIBLE);
                if (item.getItemId() == R.id.action_edit) {
                    selectPerson.setSelection(position);
                    workingTimeView.setText(personWorkingTime.workingTime.getRawTime());
                    if (personWorkingTime.person instanceof Student) {
                        Student student = (Student) personWorkingTime.person;
                        if (student.teachers != null && student.teachers.size() == 2) {
                            selectTeacherSpinner.setSelection(((ArrayAdapter<Person>) selectTeacherSpinner.getAdapter()).getPosition(student.teachers.get(0)));
                            selectGeneralTeacherSpinner.setSelection(((ArrayAdapter<Person>) selectGeneralTeacherSpinner.getAdapter()).getPosition(student.teachers.get(1)));
                        }
                    }
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private class PersonAdapter extends BaseAdapter {
        private Context context;
        List<Person> values = new ArrayList<>();

        PersonAdapter(Context context, List<Person> persons) {
            this.context = context;
            values = persons;
        }

        int add(Person person) {
            values.add(person);
            Collections.sort(values, new Comparator<Person>() {
                @Override
                public int compare(Person person, Person person2) {
                    return person.name.compareTo(person2.name);
                }
            });
            notifyDataSetChanged();
            return values.indexOf(person);
        }

        void remove(Person person) {
            values.remove(person);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return values.size();
        }

        @Override
        public Person getItem(int position) {
            return values.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, parent, false);
            }
            TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            textView.setText(getItem(position).name);
            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
            }
            TextView nameView = (TextView) convertView.findViewById(android.R.id.text1);
            TextView positionView = (TextView) convertView.findViewById(android.R.id.text2);

            Person item = getItem(position);
            nameView.setText(item.name);
            positionView.setText(item.type == 0 ? R.string.label_student : R.string.label_teacher);
            return convertView;
        }
    }

    private class PersonTimeAdapter extends BaseAdapter {
        Context context;
        List<PersonWorkingTime> values = new ArrayList<>();

        PersonTimeAdapter(Context context, List<PersonWorkingTime> values) {
            this.context = context;
            this.values = values;
        }

        public void add(PersonWorkingTime personWorkingTime) {
            values.add(personWorkingTime);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return values.size();
        }

        @Override
        public PersonWorkingTime getItem(int position) {
            return values.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.list_item_person_working_time, parent, false);
            }
            TextView personName = (TextView) convertView.findViewById(R.id.personName);
            TextView personLabel = (TextView) convertView.findViewById(R.id.personLabel);
            TextView workingTime = (TextView) convertView.findViewById(R.id.workingTime);
            TextView teachersView = (TextView) convertView.findViewById(R.id.teachers);

            PersonWorkingTime item = getItem(position);
            personName.setText(item.person.name);
            personLabel.setText(item.person.type == 0 ? R.string.label_student : R.string.label_teacher);
            workingTime.setText(item.workingTime.toString());
            // show teachers for students "teacher1, teacher2"
            teachersView.setVisibility(item.person.type == 0 ? View.VISIBLE : View.GONE);
            if (item.person.type == 0) {
                List<Person> teachers = ((Student) item.person).teachers;
                if (teachers != null && teachers.size() == 2) {
                    teachersView.setText(String.format("%s, %s", teachers.get(0).name, teachers.get(1).name));
                }
            }
            return convertView;
        }

        void remove(PersonWorkingTime personWorkingTime) {
            values.remove(personWorkingTime);
            notifyDataSetChanged();
        }
    }
}
