package com.gogaworm.praktika.data;

/**
 * Created on 17.03.2017.
 *
 * @author ikarpova
 */
public class TimeUnit {
    public int hour;
    public int minute;

    public TimeUnit() {
    }

    public TimeUnit(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public boolean earlierThan(TimeUnit timeUnit) {
        return hour < timeUnit.hour || hour == timeUnit.hour && minute < timeUnit.minute;
    }

    public boolean laterThan(TimeUnit timeUnit) {
        return hour > timeUnit.hour || hour == timeUnit.hour && minute > timeUnit.minute;
    }

    public TimeUnit minus(TimeUnit timeUnit) {
        return operation(timeUnit, false);
    }

    public TimeUnit plus(TimeUnit timeUnit) {
        return operation(timeUnit, true);
    }

    private TimeUnit operation(TimeUnit timeUnit, boolean plus) {
        int totalMinutes = (hour * 60 + minute) + (plus ? 1 : -1) * (timeUnit.hour * 60 + timeUnit.minute);
        int hour = totalMinutes / 60;
        int minutes = totalMinutes - (hour * 60);
        return new TimeUnit(hour, minutes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimeUnit) {
            TimeUnit toCompare = (TimeUnit) obj;
            return hour == toCompare.hour && minute == toCompare.minute;
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return String.format("%02d:%02d", hour, minute);
    }
}
