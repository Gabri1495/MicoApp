package com.gsorrentino.micoapp.persistence;

import androidx.room.TypeConverter;

import java.util.Calendar;
import java.util.GregorianCalendar;

@SuppressWarnings("WeakerAccess")
class Converters {
    @TypeConverter
    public static Calendar fromTimestamp(Long value) {
        if (value == null)
            return null;
        else {
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(value);
            return calendar;
        }
    }

    @TypeConverter
    public static Long calendarToTimestamp(Calendar calendar) {
        return calendar == null ? null : calendar.getTime().getTime();
    }
}
