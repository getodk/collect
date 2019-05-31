/*
 * Copyright 2019 Nafundi
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
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TableLayout;

import androidx.annotation.NonNull;

import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.listeners.ThousandsSeparatorTextWatcher;
import org.odk.collect.android.utilities.ViewIds;

import java.text.NumberFormat;
import java.util.Locale;

public abstract class BaseStringWidget extends QuestionWidget {
    protected EditText answerText;
    protected boolean useThousandSeparator;

    public BaseStringWidget(Context context, FormEntryPrompt prompt, boolean useThousandSeparator) {
        super(context, prompt);
        this.useThousandSeparator = useThousandSeparator;

        setUpAnswerText();
        setUpAnswerText();
        setDisplayValueFromModel();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return !event.isAltPressed() && super.onKeyDown(keyCode, event);
    }

    @Override
    public void clearAnswer() {
        answerText.setText(null);
    }

    @Override
    public IAnswerData getAnswer() {
        if (this instanceof IntegerWidget || this instanceof ExIntegerWidget) {
            return getIntegerValue();
        } else if (this instanceof DecimalWidget || this instanceof ExDecimalWidget) {
            return getDecimalValue();
        } else {
            return getStringValue();
        }
    }

    @NonNull
    public String getAnswerText() {
        return answerText.getText().toString();
    }

    /**
     * Registers all subviews except for the EditText to clear on long press. This makes it possible
     * to long-press to paste or perform other text editing functions.
     */
    @Override
    protected void registerToClearAnswerOnLongPress(FormEntryActivity activity) {
        for (int i = 0; i < getChildCount(); i++) {
            if (!(getChildAt(i) instanceof EditText)) {
                activity.registerForContextMenu(getChildAt(i));
            }
        }
    }

    public void setDisplayValueFromModel() {
        if (this instanceof IntegerWidget || this instanceof ExIntegerWidget) {
            setDisplayIntegerValueFromModel();
        } else if (this instanceof DecimalWidget || this instanceof ExDecimalWidget) {
            setDisplayDecimalValueFromModel();
        } else {
            setDisplayStringValueFromModel();
        }
    }

    private void setUpAnswerText() {
        answerText = new EditText(getContext());
        answerText.setId(ViewIds.generateViewId());
        answerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getAnswerFontSize());

        // needed to make long read only text scroll
        answerText.setHorizontallyScrolling(false);
        answerText.setSingleLine(false);

        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);
        answerText.setLayoutParams(params);

        answerText.addTextChangedListener(new TextWatcher() {
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
        if (useThousandSeparator) {
            answerText.addTextChangedListener(new ThousandsSeparatorTextWatcher(answerText));
        }
    }

    protected void setUpIntegerInputFilter() {
        // ints can only hold 2,147,483,648. we allow 999,999,999
        InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(9);
        if (useThousandSeparator) {
            //11 since for a nine digit number , their will be 2 separators.
            fa[0] = new InputFilter.LengthFilter(11);
        }
        answerText.setFilters(fa);
    }

    protected void setUpDecimalInputFilter() {
        // only 15 characters allowed
        InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(15);
        if (useThousandSeparator) {
            fa[0] = new InputFilter.LengthFilter(19);
        }
        answerText.setFilters(fa);
    }

    protected IAnswerData getIntegerValue() {
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

    protected IAnswerData getDecimalValue() {
        String s = answerText.getText().toString();
        if (useThousandSeparator) {
            s = ThousandsSeparatorTextWatcher.getOriginalString(s);
        }

        if (s.isEmpty()) {
            return null;
        } else {
            try {
                return new DecimalData(Double.parseDouble(s));
            } catch (Exception numberFormatException) {
                return null;
            }
        }
    }

    protected IAnswerData getStringValue() {
        String s = getAnswerText();
        return !s.equals("") ? new StringData(s) : null;
    }

    protected Integer getIntegerAnswerValue() {
        IAnswerData dataHolder = getFormEntryPrompt().getAnswerValue();
        Integer d = null;
        if (dataHolder != null) {
            Object dataValue = dataHolder.getValue();
            if (dataValue != null) {
                d = dataValue instanceof Double ? ((Double) dataValue).intValue() : (Integer) dataValue;
            }
        }
        return d;
    }

    protected Double getDoubleAnswerValue() {
        IAnswerData dataHolder = getFormEntryPrompt().getAnswerValue();
        Double d = null;
        if (dataHolder != null) {
            Object dataValue = dataHolder.getValue();
            if (dataValue != null) {
                d = dataValue instanceof Integer ? (double) (Integer) dataValue : (Double) dataValue;
            }
        }
        return d;
    }

    private void setDisplayDecimalValueFromModel() {
        Double d = getDoubleAnswerValue();

        if (d != null) {
            // truncate to 15 digits max in US locale
            // use US locale because DigitsKeyListener can't be localized before API 26
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            nf.setMaximumFractionDigits(15);
            nf.setMaximumIntegerDigits(15);
            nf.setGroupingUsed(false);

            answerText.setText(nf.format(d));
            Selection.setSelection(answerText.getText(), answerText.getText().length());
        }
    }

    private void setDisplayIntegerValueFromModel() {
        Integer i = getIntegerAnswerValue();
        if (i != null) {
            answerText.setText(String.format(Locale.US, "%d", i));
            Selection.setSelection(answerText.getText(), answerText.getText().toString().length());
        }
    }

    private void setDisplayStringValueFromModel() {
        String currentAnswer = getFormEntryPrompt().getAnswerText();

        if (currentAnswer != null) {
            answerText.setText(currentAnswer);
            Selection.setSelection(answerText.getText(), answerText.getText().toString().length());
        }
    }
}
