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
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.databinding.WidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
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
import org.odk.collect.android.utilities.DialogUtils;

import java.util.Date;

import static org.odk.collect.android.fragments.dialogs.CustomDatePickerDialog.DATE_PICKER_DIALOG;
import static org.odk.collect.android.fragments.dialogs.FixedDatePickerDialog.CURRENT_DATE;
import static org.odk.collect.android.fragments.dialogs.FixedDatePickerDialog.DATE_PICKER_DETAILS;
import static org.odk.collect.android.fragments.dialogs.FixedDatePickerDialog.DATE_PICKER_THEME;

@SuppressLint("ViewConstructor")
public class DateWidget extends QuestionWidget implements WidgetDataReceiver {
    WidgetAnswerBinding binding;

    private LocalDateTime date;
    private DatePickerDetails datePickerDetails;
    private boolean isNullAnswer;

    public DateWidget(Context context, QuestionDetails prompt) {
        this(context, prompt, false);
    }

    public DateWidget(Context context, QuestionDetails prompt, boolean isPartOfDateTimeWidget) {
        super(context, prompt, !isPartOfDateTimeWidget);
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = WidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        View answerView = binding.getRoot();

        datePickerDetails = DateTimeUtils.getDatePickerDetails(prompt.getQuestion().getAppearanceAttr());

        if (prompt.isReadOnly()) {
            binding.widgetButton.setVisibility(GONE);
        } else {
            binding.widgetButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
            binding.widgetButton.setText(getContext().getString(R.string.select_date));
            binding.widgetButton.setOnClickListener(v -> showDatePickerDialog());
        }

        binding.widgetAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        if (getFormEntryPrompt().getAnswerValue() == null) {
            clearAnswer();
            setDateToCurrent();
        } else {
            date = new LocalDateTime(getFormEntryPrompt().getAnswerValue().getValue());
            setDateLabel();
        }

        return answerView;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.widgetButton.setOnLongClickListener(l);
        binding.widgetAnswerText.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        binding.widgetButton.cancelLongPress();
        binding.widgetAnswerText.cancelLongPress();
    }

    @Override
    public void clearAnswer() {
        isNullAnswer = true;
        binding.widgetAnswerText.setText(R.string.no_date_selected);
        setDateToCurrent();
        widgetValueChanged();
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

    private void setDateToCurrent() {
        date = LocalDateTime
                .now()
                .withHourOfDay(0)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);
    }

    private void setDateLabel() {
        isNullAnswer = false;
        binding.widgetAnswerText.setText(DateTimeUtils.getDateTimeLabel((Date) getAnswer().getValue(), datePickerDetails, false, getContext()));
    }

    private void showDatePickerDialog() {
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
                bundle.putInt(DATE_PICKER_THEME, getTheme());
                bundle.putSerializable(CURRENT_DATE, date);
                bundle.putSerializable(DATE_PICKER_DETAILS, datePickerDetails);

                DialogUtils.showIfNotShowing(FixedDatePickerDialog.class, bundle, ((FormEntryActivity) getContext()).getSupportFragmentManager());
        }
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
