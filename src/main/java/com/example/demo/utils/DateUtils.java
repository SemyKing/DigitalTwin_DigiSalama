package com.example.demo.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils<T> {

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public Date stringToDate(String dateStr) {
        try {
            return new Date(simpleDateFormat.format(dateStr));
        } catch (Exception e) {
            return null;
        }
    }

}
