package org.odk.collect.android.utilities;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.chrono.EthiopicChronology;
import org.odk.collect.android.R;
import org.odk.collect.android.exception.NepaliDateException;
import org.odk.collect.android.logic.DatePickerDetails;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static java.lang.String.format;

public class DateTimeUtils {

    public static String getDateTimeLabel(Date date, DatePickerDetails datePickerDetails, boolean containsTime, Context context) {
        if (datePickerDetails.isGregorianType()) {
            return getGregorianDateTimeLabel(date, datePickerDetails, containsTime, null);
        } else if (datePickerDetails.isNepaliType()) {
            return getNepaliDateTimeLabel(date, datePickerDetails, containsTime, context);
        } else {
            return getEthiopianDateTimeLabel(date, datePickerDetails, containsTime, context);
        }
    }

    private static String getGregorianDateTimeLabel(Date date, DatePickerDetails datePickerDetails, boolean containsTime, Locale locale) {
        DateFormat dateFormatter;
        locale = locale == null ? Locale.getDefault() : locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            String format = android.text.format.DateFormat.getBestDateTimePattern(locale, getDateTimePattern(containsTime, datePickerDetails));
            dateFormatter = new SimpleDateFormat(format, locale);
        } else {
            dateFormatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, locale);
        }
        return dateFormatter.format(date);
    }


    private static String getNepaliDateTimeLabel(Date date, DatePickerDetails datePickerDetails, boolean containsTime, Context context) {

        String gregorianDateText = getGregorianDateTimeLabel(date, datePickerDetails, containsTime, Locale.US);

        DateTime nepaliDateTime = new DateTime(date).toDateTime();
        String day = datePickerDetails.isSpinnerMode() ? nepaliDateTime.getDayOfMonth() + " " : " ";
        String month = datePickerDetails.isSpinnerMode() || datePickerDetails.isMonthYearMode() ? context.getResources().getStringArray(R.array.nepali_months)[nepaliDateTime.getMonthOfYear() - 1] + " " : "";
        String nepaliDateText;

        if (containsTime) {
            SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
            nepaliDateText = day + month + nepaliDateTime.getYear() + ", " + df.format(nepaliDateTime.toDate());
        } else {
            nepaliDateText = day + month + nepaliDateTime.getYear();
        }

        return nepaliDateText;
    }


    private static String getEthiopianDateTimeLabel(Date date, DatePickerDetails datePickerDetails, boolean containsTime, Context context) {


        String gregorianDateText = getGregorianDateTimeLabel(date, datePickerDetails, containsTime, Locale.US);


        DateTime ethiopianDate = new DateTime(date).withChronology(EthiopicChronology.getInstance());
        String day = datePickerDetails.isSpinnerMode() ? ethiopianDate.getDayOfMonth() + " " : "";
        String month = datePickerDetails.isSpinnerMode() || datePickerDetails.isMonthYearMode() ? context.getResources().getStringArray(R.array.ethiopian_months)[ethiopianDate.getMonthOfYear() - 1] + " " : "";

        String ethiopianDateText;
        if (containsTime) {
            SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
            ethiopianDateText = day + month + ethiopianDate.getYear() + ", " + df.format(ethiopianDate.toDate());
        } else {
            ethiopianDateText = day + month + ethiopianDate.getYear();
        }
        return String.format(context.getString(R.string.ethiopian_date), ethiopianDateText, gregorianDateText);
    }

    private static String getDateTimePattern(boolean containsTime, DatePickerDetails datePickerDetails) {
        String datePattern;
        if (containsTime) {
            datePattern = "yyyyMMMdd HHmm";
        } else {
            datePattern = "yyyyMMMdd";
        }
        if (datePickerDetails.isMonthYearMode()) {
            datePattern = "yyyyMMM";
        } else if (datePickerDetails.isYearMode()) {
            datePattern = "yyyy";
        }
        return datePattern;
    }

    public static LocalDateTime skipDaylightSavingGapIfExists(LocalDateTime date) {
        final DateTimeZone dtz = DateTimeZone.getDefault();

        while (dtz.isLocalDateTimeGap(date)) {
            date = date.plusMinutes(1);
        }
        return date;
    }

    public static DatePickerDetails getDatePickerDetails(String appearance) {


        DatePickerDetails.DatePickerType datePickerType = DatePickerDetails.DatePickerType.GREGORIAN;
        DatePickerDetails.DatePickerMode datePickerMode = DatePickerDetails.DatePickerMode.CALENDAR;
        if (appearance != null) {
            appearance = appearance.toLowerCase(Locale.US);
            if (appearance.contains("ethiopian")) {
                datePickerType = DatePickerDetails.DatePickerType.ETHIOPIAN;
                datePickerMode = DatePickerDetails.DatePickerMode.SPINNERS;
            } else if (appearance.contains("nepali")) {
                datePickerType = DatePickerDetails.DatePickerType.NEPALI;
                datePickerMode = DatePickerDetails.DatePickerMode.SPINNERS;
            } else if (appearance.contains("no-calendar")) {
                datePickerMode = DatePickerDetails.DatePickerMode.SPINNERS;
            }

            if (appearance.contains("month-year")) {
                datePickerMode = DatePickerDetails.DatePickerMode.MONTH_YEAR;
            } else if (appearance.contains("year")) {
                datePickerMode = DatePickerDetails.DatePickerMode.YEAR;
            }
        }

        return new DatePickerDetails(datePickerType, datePickerMode);
    }

    public static LocalDateTime getNepaliDateTime(LocalDateTime localDateTime) throws NepaliDateException {


        // We have defined our own Epoch for Bikram Sambat:
        //   1-1-2007 BS / 13-4-1950 AD
        final long MS_PER_DAY = 86400000L;
        final long BS_EPOCH_TS = -622359900000L; // 1950-4-13 AD
        final long BS_YEAR_ZERO = 2007L;

        int year = 2007;
        int days;
        days = (int) Math.floor((localDateTime.toDateTime().getMillis() - BS_EPOCH_TS) / MS_PER_DAY) + 1;

        while (days > 0) {
            for (int m = 1; m <= 12; ++m) {
                int dM = NepaliDaysInMonth(year, m);
                if (days <= dM) {

                    String dateTime = year + "-" + m + "-" + days;
                    return new LocalDateTime(dateTime);
                }
                days -= dM;
            }
            ++year;
        }

        throw new NepaliDateException("Date outside supported range: " + localDateTime.getYear() + " AD");

    }

    /**
     * Magic numbers:
     * 2000 <- the first year encoded in ENCODED_MONTH_LENGTHS
     * month #5 <- this is the only month which has a day variation of more than 1
     * & 3 <- this is a 2 bit mask, i.e. 0...011
     */
    private static int NepaliDaysInMonth(int year, int month) throws NepaliDateException {

        final long[] ENCODED_MONTH_LENGTHS = {
                8673005L, 5315258L, 5314298L, 9459438L, 8673005L, 5315258L, 5314298L, 9459438L, 8473322L, 5315258L, 5314298L, 9459438L, 5327594L, 5315258L, 5314298L, 9459438L, 5327594L, 5315258L, 5314286L, 8673006L, 5315306L, 5315258L, 5265134L, 8673006L, 5315306L, 5315258L, 9459438L, 8673005L, 5315258L, 5314490L, 9459438L, 8673005L, 5315258L, 5314298L, 9459438L, 8473325L, 5315258L, 5314298L, 9459438L, 5327594L, 5315258L, 5314298L, 9459438L, 5327594L, 5315258L, 5314286L, 9459438L, 5315306L, 5315258L, 5265134L, 8673006L, 5315306L, 5315258L, 5265134L, 8673006L, 5315258L, 5314490L, 9459438L, 8673005L, 5315258L, 5314298L, 9459438L, 8669933L, 5315258L, 5314298L, 9459438L, 8473322L, 5315258L, 5314298L, 9459438L, 5327594L, 5315258L, 5314286L, 9459438L, 5315306L, 5315258L, 5265134L, 8673006L, 5315306L, 5315258L, 5265134L, 5527290L, 5527277L, 5527226L, 5527226L, 5528046L, 5527277L, 5528250L, 5528057L, 5527277L, 5527277L,
        };

        try {
            return 29 + (int) ((ENCODED_MONTH_LENGTHS[year - 2000] >>>
                    (((month - 1) << 1))) & 3);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new NepaliDateException(format("Unsupported year/month combination: %s/%s", year, month));
        }
    }
}
