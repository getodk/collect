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
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.SpinnerAdapter;
import org.odk.collect.android.listeners.AdvanceToNextListener;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.utilities.ViewIds;
import org.odk.collect.android.views.ScrolledToTopSpinner;
import org.odk.collect.android.widgets.interfaces.MultiChoiceWidget;

/**
 * SpinnerWidget handles select-one fields. Instead of a list of buttons it uses a spinner, wherein
 * the user clicks a button and the choices pop up in a dialogue box. The goal is to be more
 * compact. If images, audio, or video are specified in the select answers they are ignored.
 *
 * @author Jeff Beorse (jeff@beorse.net)
 */
@SuppressLint("ViewConstructor")
public class SpinnerWidget extends ItemsWidget implements MultiChoiceWidget {
    private final ScrolledToTopSpinner spinner;
    private final SpinnerAdapter spinnerAdapter;

    // used to ascertain whether the user selected an item on spinner (not programmatically)
    private boolean firstSetSelectionCall = true;

    @Nullable
    private AdvanceToNextListener listener;

    public SpinnerWidget(Context context, FormEntryPrompt prompt, boolean autoAdvance) {
        super(context, prompt);

        if (context instanceof AdvanceToNextListener) {
            listener = (AdvanceToNextListener) context;
        }

        View view = inflate(context, R.layout.spinner_layout, null);

        spinner = view.findViewById(R.id.spinner);
        spinnerAdapter = new SpinnerAdapter(getContext(), getChoices(prompt));
        spinner.setAdapter(spinnerAdapter);
        spinner.setPrompt(prompt.getQuestionText());
        spinner.setEnabled(!prompt.isReadOnly());
        spinner.setFocusable(!prompt.isReadOnly());
        spinner.setId(ViewIds.generateViewId());
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!firstSetSelectionCall) {
                    if (position != items.size() && autoAdvance && listener != null) {
                        listener.advance();
                    }
                    widgetValueChanged();
                }
                spinnerAdapter.updateSelectedItemPosition(spinner.getSelectedItemPosition());
                firstSetSelectionCall = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        fillInPreviousAnswer(prompt);
        addAnswerView(view);
    }

    @Override
    public IAnswerData getAnswer() {
        int selectedItemPosition = spinner.getSelectedItemPosition();
        return selectedItemPosition < 0 || selectedItemPosition >= items.size()
                ? null
                : new SelectOneData(new Selection(items.get(selectedItemPosition)));
    }

    @Override
    public void clearAnswer() {
        // It seems that spinners cannot return a null answer. This resets the answer to its original value, but it is not null.
        spinner.setSelection(items.size());
        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        spinner.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        spinner.cancelLongPress();
    }

    @Override
    public int getChoiceCount() {
        return items.size();
    }

    @Override
    public void setChoiceSelected(int choiceIndex, boolean isSelected) {
        if (isSelected) {
            spinner.setSelection(choiceIndex);
        } else if (spinner.getSelectedItemPosition() == choiceIndex) {
            clearAnswer();
        }
    }

    private void fillInPreviousAnswer(FormEntryPrompt prompt) {
        String selectedItemValue = null;
        if (prompt.getAnswerValue() != null) {
            selectedItemValue = ((Selection) prompt.getAnswerValue().getValue()).getValue();
        }

        boolean answerExists = false;
        if (selectedItemValue != null) {
            for (int i = 0; i < items.size(); ++i) {
                String match = items.get(i).getValue();
                if (match.equals(selectedItemValue)) {
                    spinner.setSelection(i);
                    answerExists = true;
                }
            }
        }
        if (!answerExists) {
            spinner.setSelection(items.size());
        }
    }

    private CharSequence[] getChoices(FormEntryPrompt prompt) {
        CharSequence[] choices = new CharSequence[items.size() + 1];
        for (int i = 0; i < items.size(); i++) {
            choices[i] = FormEntryPromptUtils.getItemText(prompt, items.get(i));
        }
        choices[items.size()] = getContext().getString(R.string.select_one);
        return choices;
    }
}
