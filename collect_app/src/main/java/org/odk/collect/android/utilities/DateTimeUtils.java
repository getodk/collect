package org.odk.collect.android.utilities;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

public class DateTimeUtils {

    /**
     * The function fixCalendarViewIfJellyBean fixes the Calendar view bug for Android 4.1.2 devices
     *
     * For more info read the complete solution at this link : http://stackoverflow.com/a/36321828/5479029
     */
    public static void fixCalendarViewIfJellyBean(CalendarView calendarView) {
        try {
            Object object = calendarView;
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals("mDelegate")) { // the CalendarViewLegacyDelegate instance is stored in this variable
                    field.setAccessible(true);
                    object = field.get(object);
                    break;
                }
            }

            Field field = object.getClass().getDeclaredField("mDateTextSize"); // text size integer value
            field.setAccessible(true);
            final int mDateTextSize = (Integer) field.get(object);

            field = object.getClass().getDeclaredField("mListView"); // main ListView
            field.setAccessible(true);
            Object innerObject = field.get(object);

            Method method = innerObject.getClass().getMethod(
                    "setOnHierarchyChangeListener", ViewGroup.OnHierarchyChangeListener.class); // we need to set the OnHierarchyChangeListener
            method.setAccessible(true);
            method.invoke(innerObject, (Object) new ViewGroup.OnHierarchyChangeListener() {
                @Override
                public void onChildViewAdded(View parent, View child) { // apply text size every time when a new child view is added
                    try {
                        Object object = child;
                        Field[] fields = object.getClass().getDeclaredFields();
                        for (Field field : fields) {
                            if (field.getName().equals("mMonthNumDrawPaint")) { // the paint is stored inside the view
                                field.setAccessible(true);
                                object = field.get(object);
                                Method method = object.getClass()
                                        .getDeclaredMethod("setTextSize", float.class); // finally set text size
                                method.setAccessible(true);
                                method.invoke(object, (Object) mDateTextSize);

                                break;
                            }
                        }
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }

                @Override
                public void onChildViewRemoved(View parent, View child) {
                }
            });
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public static String getDateTimeBasedOnUserLocale(Date date, String appearance, boolean containsTime) {
        DateFormat dateFormatter;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            String format = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), getDateTimePattern(containsTime, appearance));
            dateFormatter = new SimpleDateFormat(format, Locale.getDefault());
        } else {
            dateFormatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());
        }
        return dateFormatter.format(date);
    }

    private static String getDateTimePattern(boolean containsTime, String appearance) {
        String datePattern;
        if (containsTime) {
            datePattern = "yyyyMMMdd HHmm";
        } else {
            datePattern = "yyyyMMMdd";
        }
        if ("year".equals(appearance)) {
            datePattern = "yyyy";
        } else if ("month-year".equals(appearance)) {
            datePattern = "yyyyMMM";
        }
        return datePattern;
    }
}
