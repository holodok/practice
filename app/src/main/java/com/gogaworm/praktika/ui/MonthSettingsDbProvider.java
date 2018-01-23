package com.gogaworm.praktika.ui;

import com.gogaworm.praktika.data.MonthInfo;
import com.gogaworm.praktika.db.MonthInfoDataSource;

/**
 * Created on 06.03.2017.
 *
 * @author ikarpova
 */
public interface MonthSettingsDbProvider {
    MonthInfoDataSource getDataSource();
    MonthInfo getMonthInfo();
}
