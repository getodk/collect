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
import android.support.annotation.NonNull;
import android.text.Selection;
import android.text.method.TextKeyListener;
import android.text.method.TextKeyListener.Capitalize;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TableLayout;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.utilities.SoftKeyboardUtils;
import org.odk.collect.android.utilities.ViewIds;

import timber.log.Timber;

/**
 * The most basic widget that allows for entry of any text.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class StringWidget extends QuestionWidget {
    private static final String ROWS = "rows";
    boolean readOnly;
    private final EditText answerText;

    protected StringWidget(Context context, FormEntryPrompt prompt, boolean readOnlyOverride) {
        super(context, prompt);

        answerText = new EditText(context);
        answerText.setId(ViewIds.generateViewId());
        readOnly = prompt.isReadOnly() || readOnlyOverride;

        answerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getAnswerFontSize());

        TableLayout.LayoutParams params = new TableLayout.LayoutParams();

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
        String height = prompt.getQuestion().getAdditionalAttribute(null, ROWS);
        if (height != null && height.length() != 0) {
            try {
                int rows = Integer.parseInt(height);
                answerText.setMinLines(rows);
                answerText.setGravity(
                        Gravity.TOP); // to write test starting at the top of the edit area
            } catch (Exception e) {
                Timber.e("Unable to process the rows setting for the answerText field: %s", e.toString());
            }
        }

        params.setMargins(7, 5, 7, 5);
        answerText.setLayoutParams(params);

        // capitalize the first letter of the sentence
        answerText.setKeyListener(new TextKeyListener(Capitalize.SENTENCES, false));

        // needed to make long read only text scroll
        answerText.setHorizontallyScrolling(false);
        answerText.setSingleLine(false);

        String s = prompt.getAnswerText();
        if (s != null) {
            answerText.setText(s);
            Selection.setSelection(answerText.getText(), answerText.getText().toString().length());
        }

        if (readOnly) {
            answerText.setBackground(null);
            answerText.setEnabled(false);
            answerText.setTextColor(themeUtils.getPrimaryTextColor());
            answerText.setFocusable(false);
        }

        addAnswerView(answerText);
    }

    @Override
    public void clearAnswer() {
        answerText.setText(null);
    }

    public EditText getAnswerTextField() {
        return answerText;
    }

    @Override
    public IAnswerData getAnswer() {
        clearFocus();

        String s = getAnswerText();
        return !s.equals("") ? new StringData(s) : null;
    }

    @NonNull
    public String getAnswerText() {
        return answerText.getText().toString();
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return !event.isAltPressed() && super.onKeyDown(keyCode, event);
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

}
