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

import android.text.method.TextKeyListener;
import android.text.method.TextKeyListener.Capitalize;
import android.view.Gravity;
import android.widget.EditText;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.utilities.SoftKeyboardUtils;

import timber.log.Timber;

/**
 * The most basic widget that allows for entry of any text.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class StringWidget extends BaseStringWidget {
    private static final String ROWS = "rows";
    boolean readOnly;

    protected StringWidget(Context context, FormEntryPrompt prompt, boolean readOnlyOverride) {
        this(context, prompt, readOnlyOverride, false);
    }

    protected StringWidget(Context context, FormEntryPrompt prompt, boolean readOnlyOverride, boolean useThousandSeparator) {
        super(context, prompt, useThousandSeparator);

        readOnly = prompt.isReadOnly() || readOnlyOverride;

        handleRowsNumber();

        // capitalize the first letter of the sentence
        answerText.setKeyListener(new TextKeyListener(Capitalize.SENTENCES, false));

        if (readOnly) {
            answerText.setBackground(null);
            answerText.setEnabled(false);
            answerText.setTextColor(themeUtils.getPrimaryTextColor());
            answerText.setFocusable(false);
        }

        addAnswerView(answerText);
    }

    @Override
    public void setFocus(Context context) {
        if (!readOnly) {
            SoftKeyboardUtils.showSoftKeyboard(answerText);
            /*
             * If you do a multi-question screen after a "add another group" dialog, this won't
             * automatically pop up. It's an Android issue.
             *
             * That is, if I have an edit text in an activity, and pop a dialog, and in that
             * dialog's button's OnClick() I call edittext.requestFocus() and
             * showSoftInput(edittext, 0), showSoftinput() returns false. However, if the edittext
             * is focused before the dialog pops up, everything works fine. great.
             */
        } else {
            SoftKeyboardUtils.hideSoftKeyboard(answerText);
        }
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        answerText.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        answerText.cancelLongPress();
    }

    private void handleRowsNumber() {
        /*
         * If a 'rows' attribute is on the input tag, set the minimum number of lines
         * to display in the field to that value.
         *
         * I.e.,
         * <input ref="foo" rows="5">
         *   ...
         * </input>
         *
         * will set the height of the EditText box to 5 rows high.
         */
        String height = getFormEntryPrompt().getQuestion().getAdditionalAttribute(null, ROWS);
        if (height != null && height.length() != 0) {
            try {
                answerText.setMinLines(Integer.parseInt(height));
                answerText.setGravity(Gravity.TOP); // to write test starting at the top of the edit area
            } catch (Exception e) {
                Timber.e("Unable to process the rows setting for the answerText field: %s", e.toString());
            }
        }
    }

    // Just for tests
    public EditText getAnswerTextField() {
        return answerText;
    }
}
