package ru.werklogic.werklogic.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by bmw on 16.05.2015.
 */
public class DateUtils {
    private static final DateFormat withYear = new SimpleDateFormat("yyyy.MM.dd HH:mm");
    private static final DateFormat withMonth = new SimpleDateFormat("d MMM HH:mm");
    private static final DateFormat withHour = new SimpleDateFormat("HH:mm");

    public static String getShortDatePresentation(Date d) {
        return getShortDatePresentation(new Date(), d);
    }

    private static String getShortDatePresentation(Date baseDate, Date d) {
        Calendar baseCalendar = Calendar.getInstance();
        baseCalendar.setTime(baseDate);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);

        int y1 = baseCalendar.get(Calendar.YEAR);
        int y2 = calendar.get(Calendar.YEAR);
        int m1 = baseCalendar.get(Calendar.MONTH);
        int m2 = calendar.get(Calendar.MONTH);
        int d1 = baseCalendar.get(Calendar.DAY_OF_MONTH);
        int d2 = calendar.get(Calendar.DAY_OF_MONTH);

        if (y1 == y2) {
            if (m1 == m2 && d1 == d2) {
                return withHour.format(d);
            } else {
                return withMonth.format(d);
            }
        } else {
            return withYear.format(d);
        }
    }
}
