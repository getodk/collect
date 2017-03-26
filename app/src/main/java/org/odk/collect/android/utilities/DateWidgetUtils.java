package org.odk.collect.android.utilities;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * The function fixCalendarViewIfJellyBean fixes the Calendar view bug for Android 4.1.2 devices
 *
 * For more info read the complete solution at this link : http://stackoverflow.com/a/36321828/5479029
 */

public class DateWidgetUtils {

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
                                Method method = object.getClass().
                                        getDeclaredMethod("setTextSize", float.class); // finally set text size
                                method.setAccessible(true);
                                method.invoke(object, (Object) mDateTextSize);

                                break;
                            }
                        }
                    } catch (Exception e) {
                        Log.e("DateTimeWidget", e.getMessage(), e);
                    }
                }

                @Override
                public void onChildViewRemoved(View parent, View child) {
                }
            });
        } catch (Exception e) {
            Log.e("DateTimeWidget", e.getMessage(), e);
        }
    }

}
