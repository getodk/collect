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

package org.odk.collect.android.fragments.dialogs;

import android.util.Log;

import net.danlew.android.joda.JodaTimeAndroid;

import org.javarosa.core.model.FormIndex;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.chrono.EthiopicChronology;
import org.joda.time.chrono.ISOChronology;
import org.odk.collect.android.R;
import org.odk.collect.android.exception.BsException;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.utilities.DateTimeUtils;

import java.util.Arrays;

import static java.lang.String.format;

/**
 * @author Nishon Tandukar (nishon.tan@gmail.com)
 * @author Grzegorz Orczykowski (gorczykowski@soldevelo.com)
 * @author Aurelio Di Pasquale (aurelio.dipasquale@unibas.ch)
 */
public class NepaliDatePickerDialog extends CustomDatePickerDialog {

    private static final int MIN_SUPPORTED_YEAR = 1893; //1900 in Gregorian calendar
    private static final int MAX_SUPPORTED_YEAR = 2093; //2100 in Gregorian calendar

    private String[] monthsArray;


    public static NepaliDatePickerDialog newInstance(FormIndex formIndex, LocalDateTime date, DatePickerDetails datePickerDetails) {
        NepaliDatePickerDialog dialog = new NepaliDatePickerDialog();
        dialog.setArguments(getArgs(formIndex, date, datePickerDetails));
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        monthsArray = getResources().getStringArray(R.array.nepali_months);
        setUpValues();
    }

    private void setUpValues() {
        try {
            setUpDatePicker();
        } catch (BsException e) {
            e.printStackTrace();
        }
    }

    private void setUpDatePicker() throws BsException {

        LocalDateTime nepalidate = DateTimeUtils.getNepaliDateTime(getDate());


        setUpDayPicker(nepalidate);
        setUpMonthPicker(nepalidate, monthsArray);
        setUpYearPicker(nepalidate, MIN_SUPPORTED_YEAR, MAX_SUPPORTED_YEAR);
    }

    @Override
    protected void updateDays() {

    }

    @Override
    protected LocalDateTime getOriginalDate() {
        return null;
    }


}