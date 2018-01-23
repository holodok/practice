package com.gogaworm.praktika.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import com.gogaworm.praktika.R;
import com.gogaworm.praktika.data.MonthInfo;
import com.gogaworm.praktika.db.MonthInfoDataSource;

/**
 * Created on 08.03.2017.
 *
 * @author ikarpova
 */
public abstract class WizardActivity extends AppCompatActivity implements MonthSettingsDbProvider {
    private int step;
    protected MonthInfo monthInfo;
    private Fragment currentFragment;
    private MonthInfoDataSource dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_wizard);

        findViewById(R.id.nextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNextButtonClicked();
            }
        });

        monthInfo = getIntent().getParcelableExtra("monthInfo");
        dataSource = new MonthInfoDataSource(this);
        dataSource.open();
        showNextStep();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onNextButtonClicked() {
        if (currentFragment instanceof OnSaveListener) {
            final OnSaveListener listener = (OnSaveListener) currentFragment;
            if (listener.canGoNext()) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        listener.save();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        showNextStep();
                    }
                }.execute();
            }
        } else {
            showNextStep();
        }
    }

    private void showNextStep() {
        if (getStepCount() == step) {
            onLastStep();
            finish();
        } else {
            currentFragment = getNextStep(step++);
            getSupportFragmentManager().beginTransaction().replace(R.id.contentPanel, currentFragment).commit();
        }
    }

    protected abstract Fragment getNextStep(int step);
    protected abstract int getStepCount();
    protected abstract void onLastStep();

    @Override
    protected void onDestroy() {
        dataSource.close();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        step -= 2;
        if (canGoBack(step)) {
            showNextStep();
        } else {
            super.onBackPressed();
        }
    }

    protected abstract boolean canGoBack(int step);


    @Override
    public MonthInfoDataSource getDataSource() {
        return dataSource;
    }

    @Override
    public MonthInfo getMonthInfo() {
        return monthInfo;
    }
}
