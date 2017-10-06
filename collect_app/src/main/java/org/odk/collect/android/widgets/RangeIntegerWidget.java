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

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.form.api.FormEntryPrompt;

@SuppressLint("ViewConstructor")
public class RangeIntegerWidget extends RangeWidget {

    public RangeIntegerWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
    }

    @Override
    public IAnswerData getAnswer() {
        return actualValue == null ? null : new IntegerData(actualValue.intValue());
    }

    @Override
    protected void setUpActualValueLabel() {
        String value = actualValue != null ? String.valueOf(actualValue.intValue()) : "";

        if (currentValue != null) {
            currentValue.setText(value);
        }
    }

    @Override
    protected void setUpDisplayedValuesForNumberPicker() {
        int index = 0;
        displayedValuesForNumberPicker = new String[elementCount + 1];

        if (rangeEnd.compareTo(rangeStart) > -1) {
            for (int i = rangeEnd.intValue(); i >= rangeStart.intValue(); i -= rangeStep.abs().intValue()) {
                displayedValuesForNumberPicker[index] = String.valueOf(i);
                index++;
            }
        } else {
            for (int i = rangeEnd.intValue(); i <= rangeStart.intValue(); i += rangeStep.abs().intValue()) {
                displayedValuesForNumberPicker[index] = String.valueOf(i);
                index++;
            }
        }
    }
}