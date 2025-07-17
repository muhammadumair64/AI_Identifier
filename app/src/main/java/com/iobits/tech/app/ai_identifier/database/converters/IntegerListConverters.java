package com.iobits.tech.app.ai_identifier.database.converters;

import androidx.annotation.Keep;
import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Keep
public class IntegerListConverters {
    private static String SEPARATOR = ",";

    @TypeConverter
    public static String fromIntegerListToString(List<Integer> value) {
        return value == null ? null : concateIntegerListToString(value);
    }

    @TypeConverter
    public static List<Integer> stringToIntegerList(String date) {

        if (date != null) {
            List<String> stringList = Arrays.asList(date.split(SEPARATOR));
            List<Integer> intList = new ArrayList<>();
            for (String string : stringList) {
                try {
                    intList.add(Integer.parseInt(string));
                } catch (Exception e) {
                }

            }
            return intList;

        }
        return null;
    }

    private static String concateIntegerListToString(List<Integer> intList) {

        StringBuilder csvBuilder = new StringBuilder();
        for (Integer integer : intList) {
            csvBuilder.append(integer);
            csvBuilder.append(SEPARATOR);
        }
        String csv = csvBuilder.toString();
        if(csv.length()==0){
            return "";
        }
        csv = csv.substring(0, csv.length() - SEPARATOR.length());
        return csv;
    }

}