/*
 * Copyright (C) 2011 University of Washington
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
import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.widgets.interfaces.ButtonWidget;
import org.odk.collect.android.widgets.interfaces.MultiChoiceWidget;
import org.odk.collect.android.widgets.warnings.SpacesInUnderlyingValuesWarning;

import java.util.ArrayList;
import java.util.List;

/**
 * SpinnerMultiWidget, like SelectMultiWidget handles multiple selection fields using checkboxes,
 * but the user clicks a button to see the checkboxes. The goal is to be more compact. If images,
 * audio, or video are specified in the select answers they are ignored. WARNING: There is a bug in
 * android versions previous to 2.0 that affects this widget. You can find the report here:
 * http://code.google.com/p/android/issues/detail?id=922 This bug causes text to be white in alert
 * boxes, which makes the select options invisible in this widget. For this reason, this widget
 * should not be used on phones with android versions lower than 2.0.
 *
 * @author Jeff Beorse (jeff@beorse.net)
 */
@SuppressLint("ViewConstructor")
public class SpinnerMultiWidget extends ItemsWidget implements ButtonWidget, MultiChoiceWidget {

    // The possible select answers
    String[] answerItems;
    CharSequence[] styledAnswerItems;

    // The button to push to display the answers to choose from
    Button button;

    // Defines which answers are selected
    boolean[] selections;

    // The alert box that contains the answer selection view
    AlertDialog.Builder alertBuilder;

    // Displays the current selections below the button
    TextView selectionText;

    @SuppressWarnings("unchecked")
    public SpinnerMultiWidget(final Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        selections = new boolean[items.size()];
        answerItems = new String[items.size()];
        styledAnswerItems = new CharSequence[items.size()];
        alertBuilder = new AlertDialog.Builder(context);
        button = getSimpleButton(context.getString(R.string.select_answer));

        // Build View
        for (int i = 0; i < items.size(); i++) {
            answerItems[i] = prompt.getSelectChoiceText(items.get(i));
            styledAnswerItems[i] = org.odk.collect.android.utilities.TextUtils.textToHtml(answerItems[i]);
        }

        selectionText = getAnswerTextView();
        selectionText.setVisibility(View.GONE);

        // Fill in previous answers
        List<Selection> ve = prompt.getAnswerValue() != null
                ? (List<Selection>) prompt.getAnswerValue().getValue()
                : new ArrayList<>();

        if (ve != null) {
            List<String> selectedValues = new ArrayList<>();

            for (int i = 0; i < selections.length; i++) {
                String value = items.get(i).getValue();
                for (Selection s : ve) {
                    if (value.equals(s.getValue())) {
                        selections[i] = true;
                        selectedValues.add(answerItems[i]);
                        break;
                    }
                }
            }
            showSelectedValues(selectedValues);
        }

        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(button);
        answerLayout.addView(selectionText);
        addAnswerView(answerLayout);

        SpacesInUnderlyingValuesWarning.forQuestionWidget(this).renderWarningIfNecessary(items);
    }

    @Override
    public IAnswerData getAnswer() {
        List<Selection> vc = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            if (selections[i]) {
                vc.add(new Selection(items.get(i)));
            }
        }
        return vc.isEmpty() ? null : new SelectMultiData(vc);
    }

    @Override
    public void clearAnswer() {
        selectionText.setText(R.string.selected);
        selectionText.setVisibility(View.GONE);
        for (int i = 0; i < selections.length; i++) {
            selections[i] = false;
        }

        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        button.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        button.cancelLongPress();
    }

    @Override
    public int getChoiceCount() {
        return selections.length;
    }

    @Override
    public void setChoiceSelected(int choiceIndex, boolean isSelected) {
        selections[choiceIndex] = isSelected;
    }

    @Override
    public void onButtonClick(int buttonId) {
        alertBuilder.setTitle(getFormEntryPrompt().getQuestionText()).setPositiveButton(R.string.ok,
                (dialog, id) -> {
                    List<String> selectedValues = new ArrayList<>();

                    for (int i = 0; i < selections.length; i++) {
                        if (selections[i]) {
                            selectedValues.add(answerItems[i]);
                        }
                    }
                    showSelectedValues(selectedValues);
                });

        alertBuilder.setMultiChoiceItems(styledAnswerItems, selections,
                (dialog, which, isChecked) -> {
                    selections[which] = isChecked;
                    widgetValueChanged();
                });
        alertBuilder.create().show();
    }

    private void showSelectedValues(List<String> selectedValues) {
        if (selectedValues.isEmpty()) {
            clearAnswer();
        } else {
            CharSequence answerText = org.odk.collect.android.utilities.TextUtils.textToHtml(String.format(getContext().getString(R.string.selected_answer),
                    TextUtils.join(", ", selectedValues)));
            selectionText.setText(answerText);
            selectionText.setVisibility(View.VISIBLE);
        }
    }
}