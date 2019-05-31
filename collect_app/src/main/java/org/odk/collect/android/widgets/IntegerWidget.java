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
import androidx.annotation.NonNull;
import android.text.InputType;
import android.text.method.DigitsKeyListener;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.listeners.ThousandsSeparatorTextWatcher;

/**
 * Widget that restricts values to integers.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class IntegerWidget extends StringWidget {

    boolean useThousandSeparator;

    public IntegerWidget(Context context, FormEntryPrompt prompt, boolean readOnlyOverride, boolean useThousandSeparator) {
        super(context, prompt, readOnlyOverride);

        this.useThousandSeparator = useThousandSeparator;

        answerText.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);
        answerText.setKeyListener(new DigitsKeyListener(true, false));
        if (useThousandSeparator) {
            answerText.addTextChangedListener(new ThousandsSeparatorTextWatcher(answerText));
        }

        setUpIntegerInputFilter(useThousandSeparator);
    }

    @NonNull
    @Override
    public String getAnswerText() {
        return useThousandSeparator
                ? ThousandsSeparatorTextWatcher.getOriginalString(super.getAnswerText())
                : super.getAnswerText();
    }

    @Override
    public IAnswerData getAnswer() {
        String s = answerText.getText().toString();
        if (useThousandSeparator) {
            s = ThousandsSeparatorTextWatcher.getOriginalString(s);
        }

        if (s.isEmpty()) {
            return null;
        } else {
            try {
                return new IntegerData(Integer.parseInt(s));
            } catch (Exception numberFormatException) {
                return null;
            }
        }
    }

}
