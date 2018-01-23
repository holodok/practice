package com.gogaworm.praktika.data;

import java.util.Locale;

/**
 * Created on 17.03.2017.
 *
 * @author ikarpova
 */
public class WorkingTime {
    public TimeUnit start;
    public TimeUnit end;

    public WorkingTime() {
        start = new TimeUnit();
        end = new TimeUnit();
    }

    public WorkingTime(int startHour, int startMinute, int endHour, int endMinute) {
        start = new TimeUnit(startHour, startMinute);
        end = new TimeUnit(endHour, endMinute);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WorkingTime) {
            WorkingTime toCompare = (WorkingTime) obj;
            return toCompare.start.hour == start.hour && toCompare.start.minute == start.minute
                    && toCompare.end.hour == end.hour && toCompare.end.minute == end.minute;
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return start != null && end != null ? start.toString() + " - " + end.toString() : "";
    }

    public String getRawTime() {
        return String.format(Locale.US, "%02d%02d%02d%02d", start.hour, start.minute, end.hour, end.minute);
    }
}
