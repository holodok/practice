package com.gogaworm.praktika.ui;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import com.gogaworm.praktika.R;
import com.gogaworm.praktika.data.MonthInfo;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created on 27.02.2017.
 *
 * @author ikarpova
 */
public class SelectPersonsFragment extends WizardFragment {
    private int type;
    private ListView listView;
    private AutoCompleteTextView personName;
    private NamesAdapter adapter;

    static SelectPersonsFragment getStudentsFragment() {
        return getFragmentByType(0);
    }

    static SelectPersonsFragment getTeachersFragment() {
        return getFragmentByType(1);
    }

    private static SelectPersonsFragment getFragmentByType(int type) {
        SelectPersonsFragment selectMonthFragment = new SelectPersonsFragment();
        Bundle args = new Bundle();
        args.putInt("type", type);
        selectMonthFragment.setArguments(args);
        return selectMonthFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.fragment_select_person, container, false);
        listView = (ListView) parentView.findViewById(android.R.id.list);
        registerForContextMenu(listView);

        personName = (AutoCompleteTextView) parentView.findViewById(R.id.personName);
        personName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        parentView.findViewById(R.id.addPersonButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = personName.getText().toString().trim();
                if (!name.equals("")) {
                    adapter.addIfNew(name);
                }
                personName.setText("");
            }
        });

        type = getArguments().getInt("type");

        personName.setHint(type == 0 ? R.string.hint_student_name : R.string.hint_teacher_name);
        ((TextView) parentView.findViewById(R.id.personLabel)).setText(type == 0 ? R.string.label_select_students : R.string.label_select_teachers);

        loadData();
        return parentView;
    }

    private void loadData() {
        //load values for auto-complete
        new AsyncTask<Void, Void, List<String>> (){
            @Override
            protected List<String> doInBackground(Void... params) {
                return loadAllPersons();
            }

            @Override
            protected void onPostExecute(List<String> values) {
                super.onPostExecute(values);
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    personName.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, values));
                }
            }
        }.execute();

        new AsyncTask<Void, Void, List<String>> (){
            @Override
            protected List<String> doInBackground(Void... params) {
                return loadPersons(monthInfo);
            }

            @Override
            protected void onPostExecute(List<String> values) {
                super.onPostExecute(values);
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    adapter = new NamesAdapter(getContext(), values);
                    listView.setAdapter(adapter);
                }
            }
        }.execute();
    }

    private List<String> loadPersons(MonthInfo monthInfo) {
        if (dataSource != null) {
            return dataSource.getPersonNames(monthInfo, type);
        }
        return new ArrayList<>(0);
    }

    private List<String> loadAllPersons() {
        if (dataSource != null) {
            return dataSource.getPersonNames(type);
        }
        return new ArrayList<>(0);
    }

    @Override
    public boolean canGoNext() {
        boolean canGoNext = adapter.getNames().size() > 0;
        if (!canGoNext) {
            Toast.makeText(getContext(), type == 0 ? R.string.error_no_students : R.string.error_no_teachers, Toast.LENGTH_SHORT).show();
        }
        return canGoNext;
    }

    @Override
    public void save() {
        dataSource.savePersonsForMonth(monthInfo, adapter.getNames(), type);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.menu_context_main, menu);
        menu.getItem(1).setVisible(false);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.action_delete:
                new AsyncTask<Integer, Void, Boolean>() {
                    int position;

                    @Override
                    protected Boolean doInBackground(Integer... integers) {
                        position = integers[0];
                        return dataSource.hasPersonInfo(monthInfo, adapter.getItem(position));
                    }

                    @Override
                    protected void onPostExecute(Boolean result) {
                        super.onPostExecute(result);
                        if (result) {
                            Toast.makeText(getContext(), R.string.error_person_has_data, Toast.LENGTH_SHORT).show();
                        } else {
                            adapter.remove(position);
                        }
                    }
                }.execute(info.position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private class NamesAdapter extends BaseAdapter {
        private Context context;
        private List<String> values;

        NamesAdapter(Context context, List<String> values) {
            this.context = context;
            this.values = values;
        }

        List<String> getNames() {
            return values;
        }

        void addIfNew(String name) {
            if (!values.contains(name)) {
                values.add(name);
                Collections.sort(values, Collator.getInstance());
            }
            notifyDataSetChanged();
        }

        void remove(int position) {
            values.remove(position);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return values.size();
        }

        @Override
        public String getItem(int position) {
            return values.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            textView.setText(getItem(position));
            return convertView;
        }
    }
}
