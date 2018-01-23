package com.gogaworm.praktika.ui;

import android.content.Context;
import android.support.v4.app.Fragment;
import com.gogaworm.praktika.data.MonthInfo;
import com.gogaworm.praktika.db.MonthInfoDataSource;

/**
 * Created on 06.03.2017.
 *
 * @author ikarpova
 */
public abstract class WizardFragment extends Fragment implements OnSaveListener {
    protected MonthInfoDataSource dataSource;
    protected MonthInfo monthInfo;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MonthSettingsDbProvider) {
            MonthSettingsDbProvider dbProvider = (MonthSettingsDbProvider) context;
            dataSource = dbProvider.getDataSource();
            monthInfo = dbProvider.getMonthInfo();
        }
    }

    @Override
    public void onDetach() {
        dataSource = null;
        super.onDetach();
    }

    @Override
    public boolean canGoNext() {
        return true;
    }
}
