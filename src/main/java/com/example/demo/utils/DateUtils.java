package com.example.demo.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {

    private final static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static DateTimeFormatter getFormat() {
        return format;
    }


    public static Date stringToDateVehicle(String dateStr) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            return simpleDateFormat.parse(dateStr);
        } catch (ParseException e) {
            System.err.println("INVALID DATE VALUE (VEHICLE): " + dateStr);
            return null;
        }
    }

    public static Date stringToDateDistance(String dateStr) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

        try {
            return simpleDateFormat.parse(dateStr);
        } catch (ParseException e) {
            System.err.println("INVALID DATE VALUE (DISTANCE): " + dateStr);
            return null;
        }
    }

    public static Date stringToDateRefuel(String dateStr) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

        try {
            return simpleDateFormat.parse(dateStr);
        } catch (ParseException e) {
            System.err.println("INVALID DATE VALUE (REFUEL): " + dateStr);
            return null;
        }
    }

    public static Date stringToDateTrip(String dateStr) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            return simpleDateFormat.parse(dateStr);
        } catch (ParseException e) {
            System.err.println("INVALID DATE VALUE (TRIP): " + dateStr);
            return null;
        }
    }

    public static Date stringToDateVehicleEvent(String dateStr) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

        try {
            return simpleDateFormat.parse(dateStr);
        } catch (ParseException e) {
            System.err.println("INVALID DATE VALUE (VEHICLE EVENT): " + dateStr);
            return null;
        }
    }

    public static Date stringToDateUser(String dateStr) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

        try {
            return simpleDateFormat.parse(dateStr);
        } catch (ParseException e) {
            System.err.println("INVALID DATE VALUE (USER): " + dateStr);
            return null;
        }
    }

    public static Date stringToDateOrganisation(String dateStr) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

        try {
            return simpleDateFormat.parse(dateStr);
        } catch (ParseException e) {
            System.err.println("INVALID DATE VALUE (ORGANISATION): " + dateStr);
            return null;
        }
    }

    public static Date stringToDateEquipment(String dateStr) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

        try {
            return simpleDateFormat.parse(dateStr);
        } catch (ParseException e) {
            System.err.println("INVALID DATE VALUE (EQUIPMENT): " + dateStr);
            return null;
        }
    }

    public static Date stringToDateFile(String dateStr) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

        try {
            return simpleDateFormat.parse(dateStr);
        } catch (ParseException e) {
            System.err.println("INVALID DATE VALUE (FILE): " + dateStr);
            return null;
        }
    }

    public static Date stringToDateFleet(String dateStr) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

        try {
            return simpleDateFormat.parse(dateStr);
        } catch (ParseException e) {
            System.err.println("INVALID DATE VALUE (FLEET): " + dateStr);
            return null;
        }
    }
}
