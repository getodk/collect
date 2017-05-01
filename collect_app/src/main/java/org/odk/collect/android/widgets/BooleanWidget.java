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
import android.util.TypedValue;
import android.widget.CheckBox;

import org.javarosa.core.model.data.BooleanData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

public class BooleanWidget extends QuestionWidget {

    private CheckBox mBooleanButton;
    private FormEntryPrompt mPrompt;

    public BooleanWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        mPrompt = prompt;

        setupBooleanButton();
        readSavedAnswer();
    }

    @Override
    public void clearAnswer() {
        mBooleanButton.setChecked(false);
    }

    @Override
    public IAnswerData getAnswer() {
        return new BooleanData(mBooleanButton.isChecked());
    }

    @Override
    public void setFocus(Context context) {
        Collect.getInstance().hideKeyboard(this);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mBooleanButton.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mBooleanButton.cancelLongPress();
    }

    private void readSavedAnswer() {
        if (mPrompt.getAnswerValue() != null
                && mPrompt.getAnswerValue().getValue().equals(Boolean.TRUE)) {
            mBooleanButton.setChecked(true);
        }
    }

    private void setupBooleanButton() {
        mBooleanButton = new CheckBox(getContext());
        mBooleanButton.setId(QuestionWidget.newUniqueId());
        mBooleanButton.setText(getContext().getString(R.string.trigger));
        mBooleanButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        mBooleanButton.setEnabled(!mPrompt.isReadOnly());
        addAnswerView(mBooleanButton);
    }
}
