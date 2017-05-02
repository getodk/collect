/*
 * Copyright (C) 2009 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.DateTime;
import org.odk.collect.android.R;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Date;

import timber.log.Timber;

/**
 * Displays a TimePicker widget.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class TimeWidget extends QuestionWidget {
    private TimePickerDialog mTimePickerDialog;

    private Button mTimeButton;
    private TextView mTimeTextView;

    private int mHourOfDay;
    private int mMinuteOfHour;

    private boolean mNullAnswer;

    public TimeWidget(Context context, final FormEntryPrompt prompt) {
        super(context, prompt);

        setGravity(Gravity.START);

        createTimeButton();
        createTimeTextView();
        createTimePickerDialog();
        addViews();
    }

    @Override
    public void clearAnswer() {
        mNullAnswer = true;
        mTimeTextView.setText(R.string.no_time_selected);
    }

    @Override
    public IAnswerData getAnswer() {
        clearFocus();
        // use picker time, convert to today's date, store as utc
        DateTime dt = (new DateTime()).withTime(mHourOfDay, mMinuteOfHour, 0, 0);
        return mNullAnswer ? null : new TimeData(dt.toDate());
    }

    @Override
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mTimeButton.setOnLongClickListener(l);
        mTimeTextView.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mTimeButton.cancelLongPress();
        mTimeTextView.cancelLongPress();
    }

    private void createTimeButton() {
        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);

        mTimeButton = new Button(getContext());
        mTimeButton.setId(QuestionWidget.newUniqueId());
        mTimeButton.setText(R.string.select_time);
        mTimeButton.setPadding(20, 20, 20, 20);
        mTimeButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        mTimeButton.setLayoutParams(params);
        mTimeButton.setEnabled(!mPrompt.isReadOnly());

        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNullAnswer) {
                    setTimeToCurrent();
                } else {
                    mTimePickerDialog.updateTime(mHourOfDay, mMinuteOfHour);
                }
                mTimePickerDialog.show();
            }
        });
    }

    private void createTimeTextView() {
        mTimeTextView = new TextView(getContext());
        mTimeTextView.setId(QuestionWidget.newUniqueId());
        mTimeTextView.setPadding(20, 20, 20, 20);
        mTimeTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
    }

    private void addViews() {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(mTimeButton);
        linearLayout.addView(mTimeTextView);
        addAnswerView(linearLayout);
    }

    public void setTimeLabel() {
        mNullAnswer = false;
        mTimeTextView.setText(getAnswer().getDisplayText());
    }

    private void createTimePickerDialog() {
        mTimePickerDialog = new CustomTimePickerDialog(getContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
                        mHourOfDay = hourOfDay;
                        mMinuteOfHour = minuteOfHour;
                        setTimeLabel();
                    }
                }, 0, 0);
        mTimePickerDialog.setCanceledOnTouchOutside(false);

        if (mPrompt.getAnswerValue() == null) {
            clearAnswer();
        } else {
            DateTime dt = new DateTime(((Date) mPrompt.getAnswerValue().getValue()).getTime());
            mHourOfDay = dt.getHourOfDay();
            mMinuteOfHour = dt.getMinuteOfHour();
            setTimeLabel();
            mTimePickerDialog.updateTime(mHourOfDay, mMinuteOfHour);
        }
    }

    public int getHour() {
        return mHourOfDay;
    }

    public int getMinute() {
        return mMinuteOfHour;
    }

    public boolean isNullAnswer() {
        return mNullAnswer;
    }

    public void setTimeToCurrent() {
        DateTime dt = new DateTime();
        mHourOfDay = dt.getHourOfDay();
        mMinuteOfHour = dt.getMinuteOfHour();
        mTimePickerDialog.updateTime(mHourOfDay, mMinuteOfHour);
    }

    private class CustomTimePickerDialog extends TimePickerDialog {
        private String mDialogTitle = getContext().getString(R.string.select_time);

        CustomTimePickerDialog(Context context, OnTimeSetListener callBack, int hour, int minute) {
            super(context, callBack, hour, minute, DateFormat.is24HourFormat(context));
            setTitle(mDialogTitle);
            fixSpinner(context, hour, minute, DateFormat.is24HourFormat(context));
        }

        public void setTitle(CharSequence title) {
            super.setTitle(mDialogTitle);
        }

        /**
         * Workaround for this bug: https://code.google.com/p/android/issues/detail?id=222208
         * In Android 7.0 Nougat, spinner mode for the TimePicker in TimePickerDialog is
         * incorrectly displayed as clock, even when the theme specifies otherwise.
         *
         * Source: https://gist.github.com/jeffdgr8/6bc5f990bf0c13a7334ce385d482af9f
         */
        private void fixSpinner(Context context, int hourOfDay, int minute, boolean is24HourView) {
            // android:timePickerMode spinner and clock began in Lollipop
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    // Get the theme's android:timePickerMode
                    final int MODE_SPINNER = 1;
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
                        TimePicker timePicker = (TimePicker) findField(TimePickerDialog.class,
                                TimePicker.class, "mTimePicker").get(this);
                        Class<?> delegateClass = Class.forName("android.widget.TimePicker$TimePickerDelegate");
                        Field delegateField = findField(TimePicker.class, delegateClass, "mDelegate");
                        Object delegate = delegateField.get(timePicker);

                        Class<?> spinnerDelegateClass;
                        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.LOLLIPOP) {
                            spinnerDelegateClass = Class.forName("android.widget.TimePickerSpinnerDelegate");
                        } else {
                            // TimePickerSpinnerDelegate was initially misnamed in API 21!
                            spinnerDelegateClass = Class.forName("android.widget.TimePickerClockDelegate");
                        }

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
                            timePicker.setOnTimeChangedListener(this);
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
}
