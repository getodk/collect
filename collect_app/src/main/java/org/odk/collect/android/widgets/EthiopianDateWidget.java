/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.widgets;

import android.content.Context;
import android.view.View;

import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.DateTime;
import org.joda.time.chrono.EthiopicChronology;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.fragments.dialogs.EthiopianDatePickerDialog;
import org.odk.collect.android.utilities.DateTimeUtils;

import java.util.Date;

import static org.odk.collect.android.fragments.dialogs.CustomDatePickerDialog.DATE_PICKER_DIALOG;

/**
 * @author Grzegorz Orczykowski (gorczykowski@soldevelo.com)
 */
public class EthiopianDateWidget extends AbstractDateWidget {

    public EthiopianDateWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
    }

    @Override
    protected void createWidget() {
        super.createWidget();
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }

    @Override
    protected void setDateLabel() {
        nullAnswer = false;
        String ethiopianDate = getEthiopianDateLabel(new DateTime(((Date) getAnswer().getValue()).getTime()), getContext());
        String gregorianDate = DateTimeUtils.getDateTimeBasedOnUserLocale((Date) getAnswer().getValue(), getPrompt().getQuestion().getAppearanceAttr(), false);

        dateTextView.setText(String.format(getContext().getString(R.string.ethiopian_date), ethiopianDate, gregorianDate));
    }

    protected void showDatePickerDialog() {
        EthiopianDatePickerDialog ethiopianDatePickerDialog = EthiopianDatePickerDialog.newInstance(getPrompt().getIndex(), dateTime, calendarMode);
        ethiopianDatePickerDialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), DATE_PICKER_DIALOG);
    }

    private String getEthiopianDateLabel(DateTime dateTime, Context context) {
        DateTime ethiopianDate = dateTime.withChronology(EthiopicChronology.getInstance());
        String day = calendarMode.equals(CalendarMode.FULL_DATE) ? String.valueOf(ethiopianDate.getDayOfMonth()) + " " : "";
        String month = !calendarMode.equals(CalendarMode.YEAR) ? context.getResources().getStringArray(R.array.ethiopian_months)[ethiopianDate.getMonthOfYear() - 1] + " " : "";

        return day + month + ethiopianDate.getYear();
    }
}
