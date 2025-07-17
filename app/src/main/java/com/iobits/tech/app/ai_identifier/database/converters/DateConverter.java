package com.iobits.tech.app.ai_identifier.database.converters;

import androidx.annotation.Keep;
import androidx.room.TypeConverter;

import java.util.Date;

@Keep
public class DateConverter {
    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}