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
import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.utilities.QuestionFontSizeUtils;

import timber.log.Timber;

/**
 * The most basic widget that allows for entry of any text.
 */
@SuppressLint("ViewConstructor")
public class StringWidget extends QuestionWidget {
    public final TextInputLayout textInputLayout;
    public final TextInputEditText answerText;

    protected StringWidget(Context context, QuestionDetails questionDetails) {
        super(context, questionDetails);

        textInputLayout = getAnswerEditText(questionDetails.isReadOnly() || this instanceof ExStringWidget, getFormEntryPrompt());
        answerText = (TextInputEditText) textInputLayout.getEditText();
        setUpLayout(context);
    }

    protected void setUpLayout(Context context) {
        setDisplayValueFromModel();
        addAnswerView(textInputLayout);
    }

    @Override
    public void clearAnswer() {
        answerText.setText(null);
        widgetValueChanged();
    }

    @Override
    public IAnswerData getAnswer() {
        String answer = getAnswerText();
        return !answer.isEmpty() ? new StringData(answer) : null;
    }

    @NonNull
    public String getAnswerText() {
        return answerText.getText().toString();
    }

    @Override
    public void setFocus(Context context) {
        if (!questionDetails.isReadOnly()) {
            softKeyboardController.showSoftKeyboard(answerText);
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
            softKeyboardController.hideSoftKeyboard(answerText);
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

    /**
     * Registers all subviews except for the answer_container (which contains the EditText) to clear on long press.
     * This makes it possible to long-press to paste or perform other text editing functions.
     */
    @Override
    protected void registerToClearAnswerOnLongPress(Activity activity, ViewGroup viewGroup) {
        ViewGroup view = findViewById(R.id.question_widget_container);
        for (int i = 0; i < view.getChildCount(); i++) {
            View childView = view.getChildAt(i);
            if (childView.getId() != R.id.answer_container) {
                childView.setTag(childView.getId());
                childView.setId(getId());
                activity.registerForContextMenu(childView);
            }
        }
    }

    public void setDisplayValueFromModel() {
        String currentAnswer = getFormEntryPrompt().getAnswerText();

        if (currentAnswer != null) {
            answerText.setText(currentAnswer);
            Selection.setSelection(answerText.getText(), answerText.getText().toString().length());
        }
    }

    private TextInputLayout getAnswerEditText(boolean readOnly, FormEntryPrompt prompt) {
        TextInputLayout textInputLayout = (TextInputLayout) LayoutInflater
                .from(getContext())
                .inflate(R.layout.widget_answer_text_field, null, false);

        TextInputEditText answerEditText = textInputLayout.findViewById(R.id.edit_text);
        answerEditText.setId(View.generateViewId());
        answerEditText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, QuestionFontSizeUtils.getFontSize(settings, QuestionFontSizeUtils.FontSize.HEADLINE_6));
        answerEditText.setKeyListener(new TextKeyListener(TextKeyListener.Capitalize.SENTENCES, false));

        // needed to make long read only text scroll
        answerEditText.setHorizontallyScrolling(false);
        answerEditText.setSingleLine(false);

        if (readOnly) {
            textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_NONE);
            answerEditText.setBackground(null);
            answerEditText.setEnabled(false);
            answerEditText.setTextColor(themeUtils.getColorOnSurface());
            answerEditText.setFocusable(false);
        }

        answerEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                widgetValueChanged();
            }
        });

        QuestionDef questionDef = prompt.getQuestion();
        if (questionDef != null) {
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
            String height = questionDef.getAdditionalAttribute(null, "rows");
            if (height != null && height.length() != 0) {
                try {
                    int rows = Integer.parseInt(height);
                    answerEditText.setMinLines(rows);
                    answerEditText.setGravity(Gravity.TOP); // to write test starting at the top of the edit area
                } catch (Exception e) {
                    Timber.e(new Error("Unable to process the rows setting for the answerText field: " + e));
                }
            }
        }

        return textInputLayout;
    }

    @Override
    public void hideError() {
        textInputLayout.setError(null);
    }

    @Override
    public void displayError(String errorMessage) {
        textInputLayout.setError(errorMessage);
    }
}
