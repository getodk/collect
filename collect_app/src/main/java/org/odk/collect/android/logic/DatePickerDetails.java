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

package org.odk.collect.android.logic;

import java.io.Serializable;

public class DatePickerDetails implements Serializable {
    public enum DatePickerType {
        GREGORIAN, ETHIOPIAN, COPTIC, ISLAMIC
    }

    public enum DatePickerMode {
        CALENDAR, SPINNERS, MONTH_YEAR, YEAR
    }

    private final DatePickerType datePickerType;
    private final DatePickerMode datePickerMode;

    public DatePickerDetails(DatePickerType datePickerType, DatePickerMode datePickerMode) {
        this.datePickerType = datePickerType;
        this.datePickerMode = datePickerMode;
    }

    public boolean isGregorianType() {
        return datePickerType.equals(DatePickerType.GREGORIAN);
    }

    public boolean isEthiopianType() {
        return datePickerType.equals(DatePickerType.ETHIOPIAN);
    }

    public boolean isCopticType() {
        return datePickerType.equals(DatePickerType.COPTIC);
    }

    public boolean isCalendarMode() {
        return datePickerMode.equals(DatePickerMode.CALENDAR);
    }

    public boolean isSpinnerMode() {
        return datePickerMode.equals(DatePickerMode.SPINNERS);
    }

    public boolean isMonthYearMode() {
        return datePickerMode.equals(DatePickerMode.MONTH_YEAR);
    }

    public boolean isYearMode() {
        return datePickerMode.equals(DatePickerMode.YEAR);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DatePickerDetails)) {
            return false;
        }
        DatePickerDetails datePickerDetails = (DatePickerDetails) obj;
        return this.datePickerType.equals(datePickerDetails.datePickerType) && this.datePickerMode.equals(datePickerDetails.datePickerMode);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + datePickerType.hashCode();
        result = 31 * result + datePickerMode.hashCode();
        return result;
    }
}
