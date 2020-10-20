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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.formentry.questions.WidgetViewUtils;
import org.odk.collect.android.fragments.dialogs.BikramSambatDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.CopticDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.CustomDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.EthiopianDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.FixedDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.IslamicDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.MyanmarDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.PersianDatePickerDialog;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.utilities.DateTimeUtils;
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.odk.collect.android.widgets.interfaces.ButtonClickListener;
import org.odk.collect.android.utilities.DialogUtils;

import java.util.Date;

import static org.odk.collect.android.formentry.questions.WidgetViewUtils.createAnswerTextView;
import static org.odk.collect.android.formentry.questions.WidgetViewUtils.createSimpleButton;
import static org.odk.collect.android.fragments.dialogs.CustomDatePickerDialog.DATE_PICKER_DIALOG;
import static org.odk.collect.android.fragments.dialogs.FixedDatePickerDialog.CURRENT_DATE;
import static org.odk.collect.android.fragments.dialogs.FixedDatePickerDialog.DATE_PICKER_DETAILS;
import static org.odk.collect.android.fragments.dialogs.FixedDatePickerDialog.THEME;

/**
 * Displays a DatePicker widget. DateWidget handles leap years and does not allow dates that do not
 * exist.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class DateWidget extends QuestionWidget implements WidgetDataReceiver, ButtonClickListener {
    Button dateButton;
    TextView dateTextView;

    boolean isNullAnswer;

    private LocalDateTime date;

    private DatePickerDetails datePickerDetails;

    public DateWidget(Context context, QuestionDetails prompt) {
        this(context, prompt, false);
    }

    public DateWidget(Context context, QuestionDetails prompt, boolean isPartOfDateTimeWidget) {
        super(context, prompt, !isPartOfDateTimeWidget);
        createWidget(context);
    }

    protected void createWidget(Context context) {
        datePickerDetails = DateTimeUtils.getDatePickerDetails(getFormEntryPrompt().getQuestion().getAppearanceAttr());
        dateButton = createSimpleButton(getContext(), getFormEntryPrompt().isReadOnly(), getContext().getString(R.string.select_date), getAnswerFontSize(), this);
        dateTextView = createAnswerTextView(getContext(), getAnswerFontSize());
        addViews(context);
        if (getFormEntryPrompt().getAnswerValue() == null) {
            clearAnswer();
            setDateToCurrent();
        } else {
            date = new LocalDateTime(getFormEntryPrompt().getAnswerValue().getValue());
            setDateLabel();
        }
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        dateButton.setOnLongClickListener(l);
        dateTextView.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        dateButton.cancelLongPress();
        dateTextView.cancelLongPress();
    }

    @Override
    public void clearAnswer() {
        clearAnswerWithoutValueChangeEvent();
        widgetValueChanged();
    }

    void clearAnswerWithoutValueChangeEvent() {
        isNullAnswer = true;
        dateTextView.setText(R.string.no_date_selected);
        setDateToCurrent();
    }

    @Override
    public IAnswerData getAnswer() {
        return isNullAnswer ? null : new DateData(date.toDate());
    }

    @Override
    public void setData(Object answer) {
        if (answer instanceof LocalDateTime) {
            date = (LocalDateTime) answer;
            setDateLabel();
        }
    }

    @Override
    public void onButtonClick(int buttonId) {
        showDatePickerDialog();
    }

    public boolean isDayHidden() {
        return datePickerDetails.isMonthYearMode() || datePickerDetails.isYearMode();
    }

    public LocalDateTime getDate() {
        return date;
    }

    public boolean isNullAnswer() {
        return isNullAnswer;
    }

    private void addViews(Context context) {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(dateButton);
        linearLayout.addView(dateTextView);
        addAnswerView(linearLayout, WidgetViewUtils.getStandardMargin(context));
    }

    protected void setDateToCurrent() {
        date = LocalDateTime
                .now()
                .withHourOfDay(0)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);
    }

    protected void setDateLabel() {
        isNullAnswer = false;
        dateTextView.setText(DateTimeUtils.getDateTimeLabel((Date) getAnswer().getValue(), datePickerDetails, false, getContext()));
    }

    protected void showDatePickerDialog() {
        switch (datePickerDetails.getDatePickerType()) {
            case ETHIOPIAN:
                CustomDatePickerDialog dialog = EthiopianDatePickerDialog.newInstance(getFormEntryPrompt().getIndex(), date, datePickerDetails);
                dialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), DATE_PICKER_DIALOG);
                break;
            case COPTIC:
                dialog = CopticDatePickerDialog.newInstance(getFormEntryPrompt().getIndex(), date, datePickerDetails);
                dialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), DATE_PICKER_DIALOG);
                break;
            case ISLAMIC:
                dialog = IslamicDatePickerDialog.newInstance(getFormEntryPrompt().getIndex(), date, datePickerDetails);
                dialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), DATE_PICKER_DIALOG);
                break;
            case BIKRAM_SAMBAT:
                dialog = BikramSambatDatePickerDialog.newInstance(getFormEntryPrompt().getIndex(), date, datePickerDetails);
                dialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), DATE_PICKER_DIALOG);
                break;
            case MYANMAR:
                dialog = MyanmarDatePickerDialog.newInstance(getFormEntryPrompt().getIndex(), date, datePickerDetails);
                dialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), DATE_PICKER_DIALOG);
                break;
            case PERSIAN:
                dialog = PersianDatePickerDialog.newInstance(getFormEntryPrompt().getIndex(), date, datePickerDetails);
                dialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), DATE_PICKER_DIALOG);
                break;
            default:
                Bundle bundle = new Bundle();
                bundle.putInt(THEME, getTheme());
                bundle.putSerializable(CURRENT_DATE, date);
                bundle.putSerializable(DATE_PICKER_DETAILS, datePickerDetails);

                DialogUtils.showIfNotShowing(FixedDatePickerDialog.class, bundle, ((FormEntryActivity) getContext()).getSupportFragmentManager());
        }
    }

    public void onDateSet(int year, int month, int dayOfMonth) {
        date = new LocalDateTime()
                .withYear(year)
                .withMonthOfYear(month + 1)
                .withDayOfMonth(dayOfMonth)
                .withHourOfDay(0)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);
        setDateLabel();
    }

    private int getTheme() {
        int theme = 0;
        if (!isBrokenSamsungDevice()) {
            theme = themeUtils.getMaterialDialogTheme();
        }
        if (!datePickerDetails.isCalendarMode() || isBrokenSamsungDevice()) {
            theme = themeUtils.getHoloDialogTheme();
        }

        return theme;
    }

    // https://stackoverflow.com/questions/28618405/datepicker-crashes-on-my-device-when-clicked-with-personal-app
    private boolean isBrokenSamsungDevice() {
        return Build.MANUFACTURER.equalsIgnoreCase("samsung")
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1;
    }
}
