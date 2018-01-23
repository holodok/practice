package com.gogaworm.praktika.data;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import com.gogaworm.praktika.R;

/**
 * Created on 11.03.2017.
 *
 * @author Ilona
 */
public class MonthDay implements Parcelable {
    public long id;
    public int day;
    public MonthInfo monthInfo;

    public MonthDay() {
    }

    public MonthDay(long id, MonthInfo monthInfo, int day) {
        this.id = id;
        this.monthInfo = monthInfo;
        this.day = day;
    }

    protected MonthDay(Parcel in) {
        id = in.readLong();
        day = in.readInt();
        monthInfo = in.readParcelable(MonthInfo.class.getClassLoader());
    }

    public String getTitle(Context context) {
        return String.format("%d %s", day, context.getResources().getStringArray(R.array.months_for_dates)[monthInfo.month]);
    }

    public static final Creator<MonthDay> CREATOR = new Creator<MonthDay>() {
        @Override
        public MonthDay createFromParcel(Parcel in) {
            return new MonthDay(in);
        }

        @Override
        public MonthDay[] newArray(int size) {
            return new MonthDay[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeInt(day);
        parcel.writeParcelable(monthInfo, i);
    }
}
