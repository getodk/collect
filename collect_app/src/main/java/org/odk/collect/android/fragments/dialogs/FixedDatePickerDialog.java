package org.odk.collect.android.fragments.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import org.joda.time.LocalDateTime;
import org.odk.collect.android.R;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.widgets.utilities.DateTimeWidgetUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import timber.log.Timber;

public class FixedDatePickerDialog extends DialogFragment {

    private DatePickerDialog.OnDateSetListener listener;
    private ThemeUtils themeUtils;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        themeUtils = new ThemeUtils(context);

        if (context instanceof DatePickerDialog.OnDateSetListener) {
            listener = (DatePickerDialog.OnDateSetListener) context;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LocalDateTime date = (LocalDateTime) getArguments().getSerializable(DateTimeWidgetUtils.DATE);
        int theme = getArguments().getInt(DateTimeWidgetUtils.DATE_PICKER_THEME);

        DatePickerDialog dialog = new DatePickerDialog(requireActivity(), theme, listener,
                date.getYear(), date.getMonthOfYear() - 1, date.getDayOfMonth());

        if (themeUtils.isHoloDialogTheme(theme)) {
            dialog.setTitle(requireContext().getString(R.string.select_date));
            fixSpinner(requireContext(), date.getYear(), date.getMonthOfYear() - 1, date.getDayOfMonth());
            hidePickersIfNeeded(dialog);

            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            //noinspection deprecation
            dialog.getDatePicker().setCalendarViewShown(false);
        }

        return dialog;
    }

    private void hidePickersIfNeeded(DatePickerDialog dialog) {
        LocalDateTime date = (LocalDateTime) getArguments().getSerializable(DateTimeWidgetUtils.DATE);
        if (((DatePickerDetails) getArguments().getSerializable(DateTimeWidgetUtils.DATE_PICKER_DETAILS)).isYearMode()) {
            dialog.getDatePicker().findViewById(Resources.getSystem().getIdentifier("day", "id", "android"))
                    .setVisibility(View.GONE);

            dialog.getDatePicker().findViewById(Resources.getSystem().getIdentifier("month", "id", "android"))
                    .setVisibility(View.GONE);
            dialog.getDatePicker().updateDate(date.getYear(), 1, 1);
        } else if (((DatePickerDetails) getArguments().getSerializable(DateTimeWidgetUtils.DATE_PICKER_DETAILS)).isMonthYearMode()) {
            dialog.getDatePicker().findViewById(Resources.getSystem().getIdentifier("day", "id", "android"))
                    .setVisibility(View.GONE);
            dialog.getDatePicker().updateDate(date.getYear(), date.getMonthOfYear() - 1, 1);
        }
    }

    /**
     * Workaround for this bug: https://code.google.com/p/android/issues/detail?id=222208
     * In Android 7.0 Nougat, spinner mode for the DatePicker in DatePickerDialog is
     * incorrectly displayed as calendar, even when the theme specifies otherwise.
     * <p>
     * Source: https://gist.github.com/jeffdgr8/6bc5f990bf0c13a7334ce385d482af9f
     */
    private void fixSpinner(Context context, int year, int month, int dayOfMonth) {
        // The spinner vs not distinction probably started in lollipop but applying this
        // for versions < nougat leads to a crash trying to get DatePickerSpinnerDelegate
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            try {
                // Get the theme's android:datePickerMode
                final int MODE_SPINNER = 2;
                Class<?> styleableClass = Class.forName("com.android.internal.R$styleable");
                Field datePickerStyleableField = styleableClass.getField("DatePicker");
                int[] datePickerStyleable = (int[]) datePickerStyleableField.get(null);
                final TypedArray a = context.obtainStyledAttributes(null, datePickerStyleable,
                        android.R.attr.datePickerStyle, 0);
                Field datePickerModeStyleableField = styleableClass.getField("DatePicker_datePickerMode");
                int datePickerModeStyleable = datePickerModeStyleableField.getInt(null);
                final int mode = a.getInt(datePickerModeStyleable, MODE_SPINNER);
                a.recycle();

                if (mode == MODE_SPINNER) {

                    Field datePickerField = findField(DatePickerDialog.class,
                            DatePicker.class, "mDatePicker");
                    if (datePickerField == null) {
                        Timber.w("Reflection failed: couldn't find 'mDatePicker' field on DatePickerDialog.");
                        return;
                    }

                    DatePicker datePicker = (DatePicker) datePickerField.get(this);
                    Class<?> delegateClass = Class.forName("android.widget.DatePicker$DatePickerDelegate");

                    Field delegateField = findField(DatePicker.class, delegateClass, "mDelegate");
                    if (delegateField == null) {
                        Timber.w("Reflection failed: couldn't find 'mDelegate' field on DatePickerDialog.");
                        return;
                    }

                    Object delegate = delegateField.get(datePicker);

                    Class<?> spinnerDelegateClass = Class.forName("android.widget.DatePickerSpinnerDelegate");

                    // In 7.0 Nougat for some reason the datePickerMode is ignored and the
                    // delegate is DatePickerCalendarDelegate
                    if (delegate.getClass() != spinnerDelegateClass) {
                        delegateField.set(datePicker, null); // throw out the DatePickerCalendarDelegate!
                        datePicker.removeAllViews(); // remove the DatePickerCalendarDelegate views

                        Constructor spinnerDelegateConstructor = spinnerDelegateClass
                                .getDeclaredConstructor(DatePicker.class, Context.class,
                                        AttributeSet.class, int.class, int.class);
                        spinnerDelegateConstructor.setAccessible(true);

                        // Instantiate a DatePickerSpinnerDelegate
                        delegate = spinnerDelegateConstructor.newInstance(datePicker, context,
                                null, android.R.attr.datePickerStyle, 0);

                        // set the DatePicker.mDelegate to the spinner delegate
                        delegateField.set(datePicker, delegate);

                        // Set up the DatePicker again, with the DatePickerSpinnerDelegate
                        datePicker.updateDate(year, month, dayOfMonth);
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
