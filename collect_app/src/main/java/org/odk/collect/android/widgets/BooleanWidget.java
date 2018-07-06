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
import android.support.v7.widget.AppCompatCheckBox;
import android.util.TypedValue;

import org.javarosa.core.model.data.BooleanData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.utilities.ViewIds;

public class BooleanWidget extends QuestionWidget {

    private AppCompatCheckBox booleanButton;
    private final FormEntryPrompt prompt;

    public BooleanWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        this.prompt = prompt;

        setupBooleanButton();
        readSavedAnswer();
    }

    @Override
    public void clearAnswer() {
        booleanButton.setChecked(false);
    }

    @Override
    public IAnswerData getAnswer() {
        return new BooleanData(booleanButton.isChecked());
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        booleanButton.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        booleanButton.cancelLongPress();
    }

    private void readSavedAnswer() {
        Boolean value = (Boolean) prompt.getAnswerValue().getValue();
        if (value != null) {
            booleanButton.setChecked(value);
        }
    }

    private void setupBooleanButton() {
        booleanButton = new AppCompatCheckBox(getContext());
        booleanButton.setId(ViewIds.generateViewId());
        booleanButton.setText(getContext().getString(R.string.trigger));
        booleanButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getAnswerFontSize());
        booleanButton.setEnabled(!prompt.isReadOnly());
        addAnswerView(booleanButton);
    }

    boolean isChecked() {
        return booleanButton.isChecked();
    }

    void isChecked(boolean isChecked) {
        booleanButton.setChecked(isChecked);
    }
}
