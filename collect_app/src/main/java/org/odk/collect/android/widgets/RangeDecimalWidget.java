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

import android.annotation.SuppressLint;
import android.content.Context;

import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;

import java.math.BigDecimal;

@SuppressLint("ViewConstructor")
public class RangeDecimalWidget extends RangeWidget {

    public RangeDecimalWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
    }

    @Override
    public IAnswerData getAnswer() {
        return actualValue != null
                ? new DecimalData(actualValue.doubleValue())
                : null;
    }

    @Override
    protected void setUpActualValueLabel() {
        String value = actualValue != null
                ? String.valueOf(actualValue.doubleValue())
                : "";

        if (currentValue != null) {
            currentValue.setText(value);
        }
    }

    @Override
    protected void setUpDisplayedValuesForNumberPicker() {
        displayedValuesForNumberPicker = new String[elementCount + 1];

        if (isRangeIncreasing()) {
            fillDisplayedValuesWithIncreasingValues();

        } else {
            fillDisplayedValuesWithDecreasingValues();
        }
    }

    private boolean isRangeIncreasing() {
        return rangeEnd.compareTo(rangeStart) > -1;
    }

    private void fillDisplayedValuesWithIncreasingValues() {
        int index = 0;
        for (BigDecimal i = rangeEnd; i.compareTo(rangeStart) > -1; i = i.subtract(rangeStep.abs())) {
            displayedValuesForNumberPicker[index] = String.valueOf(i);
            index++;
        }
    }

    private void fillDisplayedValuesWithDecreasingValues() {
        int index = 0;
        for (BigDecimal i = rangeEnd; i.compareTo(rangeStart) < 1; i = i.add(rangeStep.abs())) {
            displayedValuesForNumberPicker[index] = String.valueOf(i);
            index++;
        }
    }
}
