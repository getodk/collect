package org.odk.collect.android.utilities;

import org.javarosa.core.model.data.TimeData;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.chrono.GregorianChronology;

public final class DateTimeUtils {

    private DateTimeUtils() {

    }

    public static LocalDateTime getCurrentDateTime() {
        return new LocalDateTime()
                .withDate(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), DateTime.now().getDayOfMonth())
                .withTime(DateTime.now().getHourOfDay(), DateTime.now().getMinuteOfHour(), 0, 0);
    }

    public static LocalDateTime getSelectedDate(LocalDateTime selectedDate, LocalDateTime currentTime) {
        return new LocalDateTime()
                .withDate(selectedDate.getYear(), selectedDate.getMonthOfYear(), selectedDate.getDayOfMonth())
                .withTime(currentTime.getHourOfDay(), currentTime.getMinuteOfHour(), 0, 0);
    }

    public static LocalDateTime getDateAsGregorian(LocalDateTime date) {
        return skipDaylightSavingGapIfExists(date)
                .toDateTime()
                .withChronology(GregorianChronology.getInstance())
                .toLocalDateTime();
    }

    public static LocalDateTime getSelectedTime(LocalDateTime selectedTime, LocalDateTime currentDate) {
        return new LocalDateTime()
                .withDate(currentDate.getYear(), currentDate.getMonthOfYear(), currentDate.getDayOfMonth())
                .withTime(selectedTime.getHourOfDay(), selectedTime.getMinuteOfHour(), 0, 0);
    }

    public static TimeData getTimeData(DateTime dateTime) {
        return new TimeData(dateTime.toDate());
    }

    public static LocalDateTime getDateWithSkippedDaylightSavingGapIfExists(LocalDateTime date) {
        return skipDaylightSavingGapIfExists(date)
                .toDateTime()
                .toLocalDateTime();
    }

    public static LocalDateTime skipDaylightSavingGapIfExists(LocalDateTime date) {
        final DateTimeZone dtz = DateTimeZone.getDefault();

        if (dtz != null) {
            while (dtz.isLocalDateTimeGap(date)) {
                date = date.plusMinutes(1);
            }
        }
        return date;
    }
}
