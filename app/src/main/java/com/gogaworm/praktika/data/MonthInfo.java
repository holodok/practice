package com.gogaworm.praktika.data;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import com.gogaworm.praktika.R;

/**
 * Created on 26.02.2017.
 *
 * @author ikarpova
 */
public class MonthInfo implements Parcelable {
    public long id;
    public int year;
    public int month;

    public MonthInfo(long id, int year, int month) {
        this.id = id;
        this.year = year;
        this.month = month;
    }

    protected MonthInfo(Parcel in) {
        id = in.readLong();
        year = in.readInt();
        month = in.readInt();
    }

    public static final Creator<MonthInfo> CREATOR = new Creator<MonthInfo>() {
        @Override
        public MonthInfo createFromParcel(Parcel in) {
            return new MonthInfo(in);
        }

        @Override
        public MonthInfo[] newArray(int size) {
            return new MonthInfo[size];
        }
    };

    public String getTitle(Context context) {
        return String.format("%d %s", year, context.getResources().getStringArray(R.array.months)[month]);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeInt(year);
        dest.writeInt(month);
    }
}
