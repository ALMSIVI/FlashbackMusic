package cse110.team19.flashbackmusic;

import java.time.LocalDateTime;

/**
 * Created by YueWu on 3/13/2018.
 */

public class DateWrapper {
    int year, month, day, hour, minute;

    public void setDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public void setTime(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public LocalDateTime generateDate() {
        return LocalDateTime.of(year, month, day, hour, minute);
    }
}
