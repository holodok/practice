package com.gogaworm.praktika.data;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import com.gogaworm.praktika.db.MonthInfoDataSource;

import java.util.List;

/**
 * Created on 01.03.2017.
 *
 * @author ikarpova
 */
public class MonthInfoLoader extends AsyncTaskLoader<List<MonthInfo>> {
    private final MonthInfoDataSource dataSource;

    public MonthInfoLoader(Context context) {
        super(context);
        dataSource = new MonthInfoDataSource(context);
    }

    @Override
    public List<MonthInfo> loadInBackground() {
        dataSource.open();
        try {
            return dataSource.getAllMonthInfo();
        } finally {
            dataSource.close();
        }
    }


}
