package org.odk.collect.android.fragments.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.Window;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import org.joda.time.LocalDateTime;
import org.odk.collect.android.R;
import org.odk.collect.android.widgets.viewmodels.DateTimeViewModel;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import timber.log.Timber;

public class CustomTimePickerDialog extends DialogFragment {
    public static final String CURRENT_TIME = "CURRENT_TIME";
    public static final String TIME_PICKER_THEME = "TIME_PICKER_THEME";

    private DateTimeViewModel dateTimeViewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dateTimeViewModel = new ViewModelProvider(requireActivity()).get(DateTimeViewModel.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LocalDateTime date = (LocalDateTime) getArguments().getSerializable(CURRENT_TIME);

        TimePickerDialog dialog = new TimePickerDialog(requireContext(), getArguments().getInt(TIME_PICKER_THEME),
                dateTimeViewModel.getOnTimeSetListener(), date.getHourOfDay(), date.getMinuteOfHour(), DateFormat.is24HourFormat(requireContext()));

        dialog.setTitle(requireContext().getString(R.string.select_time));
        fixSpinner(requireContext(), date.getHourOfDay(), date.getMinuteOfHour(), DateFormat.is24HourFormat(requireContext()));

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        setCancelable(false);
        return dialog;
    }

    /**
     * Workaround for this bug: https://code.google.com/p/android/issues/detail?id=222208
     * In Android 7.0 Nougat, spinner mode for the TimePicker in TimePickerDialog is
     * incorrectly displayed as clock, even when the theme specifies otherwise.
     * <p>
     * Source: https://gist.github.com/jeffdgr8/6bc5f990bf0c13a7334ce385d482af9f
     */
    @SuppressWarnings("deprecation")
    private void fixSpinner(Context context, int hourOfDay, int minute, boolean is24HourView) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            try {
                // Get the theme's android:timePickerMode
                final int MODE_SPINNER = 2;
                Class<?> styleableClass = Class.forName("com.android.internal.R$styleable");
                Field timePickerStyleableField = styleableClass.getField("TimePicker");
                int[] timePickerStyleable = (int[]) timePickerStyleableField.get(null);
                final TypedArray a = context.obtainStyledAttributes(null, timePickerStyleable,
                        android.R.attr.timePickerStyle, 0);
                Field timePickerModeStyleableField = styleableClass.getField("TimePicker_timePickerMode");
                int timePickerModeStyleable = timePickerModeStyleableField.getInt(null);
                final int mode = a.getInt(timePickerModeStyleable, MODE_SPINNER);
                a.recycle();

                if (mode == MODE_SPINNER) {
                    Field field = findField(TimePickerDialog.class, TimePicker.class, "mTimePicker");
                    if (field == null) {
                        Timber.e("Reflection failed: Couldn't find field 'mTimePicker'");
                        return;
                    }

                    TimePicker timePicker = (TimePicker) field.get(this);
                    Class<?> delegateClass = Class.forName("android.widget.TimePicker$TimePickerDelegate");
                    Field delegateField = findField(TimePicker.class, delegateClass, "mDelegate");

                    if (delegateField == null) {
                        Timber.e("Reflection failed: Couldn't find field 'mDelegate'");
                        return;
                    }
                    Object delegate = delegateField.get(timePicker);

                    Class<?> spinnerDelegateClass;
                    spinnerDelegateClass = Class.forName("android.widget.TimePickerSpinnerDelegate");
                    // In 7.0 Nougat for some reason the timePickerMode is ignored and the
                    // delegate is TimePickerClockDelegate
                    if (delegate.getClass() != spinnerDelegateClass) {
                        delegateField.set(timePicker, null); // throw out the TimePickerClockDelegate!
                        timePicker.removeAllViews(); // remove the TimePickerClockDelegate views
                        Constructor spinnerDelegateConstructor = spinnerDelegateClass
                                .getConstructor(TimePicker.class, Context.class,
                                        AttributeSet.class, int.class, int.class);
                        spinnerDelegateConstructor.setAccessible(true);

                        // Instantiate a TimePickerSpinnerDelegate
                        delegate = spinnerDelegateConstructor.newInstance(timePicker, context,
                                null, android.R.attr.timePickerStyle, 0);

                        // set the TimePicker.mDelegate to the spinner delegate
                        delegateField.set(timePicker, delegate);

                        // Set up the TimePicker again, with the TimePickerSpinnerDelegate
                        timePicker.setIs24HourView(is24HourView);
                        timePicker.setCurrentHour(hourOfDay);
                        timePicker.setCurrentMinute(minute);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Field findField(Class objectClass, Class fieldClass, String expectedName) {
        try {
            Field field = objectClass.getDeclaredField(expectedName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            Timber.i(e); // ignore
        }

        // search for it if it wasn't found under the expected ivar name
        for (Field searchField : objectClass.getDeclaredFields()) {
            if (searchField.getType() == fieldClass) {
                searchField.setAccessible(true);
                return searchField;
            }
        }
        return null;
    }
}