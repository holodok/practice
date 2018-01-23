package com.gogaworm.praktika.ui;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.gogaworm.praktika.R;
import com.gogaworm.praktika.data.MonthInfo;

/**
 * Created on 27.02.2017.
 *
 * @author ikarpova
 */
public class SelectMonthFragment extends WizardFragment {
    private TextView yearView;
    private ListView listView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.fragment_select_month, container, false);
        yearView = (TextView) parentView.findViewById(R.id.year);
        listView = (ListView) parentView.findViewById(android.R.id.list);
        final String[] months = getResources().getStringArray(R.array.months);
        listView.setAdapter(new MonthAdapter(getActivity(), months));

        parentView.findViewById(R.id.plusButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeYear(true);
            }
        });
        parentView.findViewById(R.id.minusButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeYear(false);
            }
        });

        yearView.setText(String.valueOf(monthInfo.year));
        listView.setSelection(monthInfo.month - 1);

        loadIdIfAny(monthInfo);
        return parentView;
    }

    private void changeYear(boolean up) {
        monthInfo.year += up ? 1 : -1;
        yearView.setText(String.valueOf(monthInfo.year));
    }

    @Override
    public void save() {
        dataSource.saveMonthInfo(monthInfo);
    }

    private void loadIdIfAny(final MonthInfo monthInfo) {
        new AsyncTask<MonthInfo, Void, Long>() {
            @Override
            protected Long doInBackground(MonthInfo... params) {
                if (dataSource != null) {
                    return dataSource.loadMonthInfo(monthInfo);
                } return 0L;
            }

            @Override
            protected void onPostExecute(Long id) {
                super.onPostExecute(id);
                monthInfo.id = id;
                ((BaseAdapter) listView.getAdapter()).notifyDataSetInvalidated();
            }
        }.execute(monthInfo);
    }

    private class MonthAdapter extends ArrayAdapter<String> {
        MonthAdapter(Context context, String[] months) {
            super(context, android.R.layout.simple_list_item_1, android.R.id.text1, months);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            int colorId = position == monthInfo.month ? (monthInfo.id == 0 ? R.color.colorAccent : R.color.colorAccent100) : android.R.color.transparent;
            view.setBackgroundColor(getResources().getColor(colorId));
            ((TextView) view.findViewById(android.R.id.text1)).setTextColor(getResources().getColor(position == monthInfo.month ? R.color.primaryInvertedText : R.color.primaryText));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    monthInfo.month = position;
                    loadIdIfAny(monthInfo);
                }
            });
            return view;
        }
    }
}
