package com.gsorrentino.micoapp.persistence;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

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


    @TypeConverter
    public static List<String> fromJsonToListString(String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>(){}.getType();
        return gson.fromJson(json, type);
    }

    @TypeConverter
    public static String listStringToJson(List<String> list) {
        return new Gson().toJson(list);
    }
}
