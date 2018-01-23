package com.gogaworm.praktika.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.gogaworm.praktika.R;

public class NewMonthActivity extends WizardActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.title_new_month);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected Fragment getNextStep(int step) {
        switch (step) {
            case 0:
                return new SelectMonthFragment();
            case 1:
                getSupportActionBar().setTitle(monthInfo.getTitle(this));
                return SelectPersonsFragment.getStudentsFragment();
            case 2:
                return SelectPersonsFragment.getTeachersFragment();
        }
        return null;
    }

    @Override
    protected int getStepCount() {
        return 3;
    }

    @Override
    protected void onLastStep() {
        Intent intent = new Intent(this, MonthDayListActivity.class);
        intent.putExtra("monthInfo", monthInfo);
        startActivity(intent);
    }

    @Override
    protected boolean canGoBack(int step) {
        return step > 0;
    }

}
