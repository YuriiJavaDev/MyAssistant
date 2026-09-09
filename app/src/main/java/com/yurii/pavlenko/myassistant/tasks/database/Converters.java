package com.yurii.pavlenko.myassistant.tasks.database;

import androidx.room.TypeConverter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Converters {

    @TypeConverter
    public static LocalDate fromTimestampToDate(Long value) {
        return value == null ? null : LocalDate.ofEpochDay(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(LocalDate date) {
        return date == null ? null : date.toEpochDay();
    }

    @TypeConverter
    public static LocalDateTime fromTimestampToDateTime(Long value) {
        return value == null ? null : LocalDateTime.ofEpochSecond(value, 0, ZoneOffset.UTC);
    }

    @TypeConverter
    public static Long dateTimeToTimestamp(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.toEpochSecond(ZoneOffset.UTC);
    }
}