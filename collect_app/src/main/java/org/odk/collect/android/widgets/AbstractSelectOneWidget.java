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
import androidx.recyclerview.widget.RecyclerView;
import android.widget.RadioButton;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.adapters.SelectOneListAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.listeners.AdvanceToNextListener;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.widgets.interfaces.MultiChoiceWidget;

import timber.log.Timber;

/**
 * SelectOneWidgets handles select-one fields using radio buttons.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
@SuppressLint("ViewConstructor")
public abstract class AbstractSelectOneWidget extends SelectTextWidget implements MultiChoiceWidget {

    @Nullable
    private AdvanceToNextListener listener;

    private String selectedValue;
    private final boolean autoAdvance;
    SelectOneListAdapter adapter;

    public AbstractSelectOneWidget(Context context, FormEntryPrompt prompt, boolean autoAdvance) {
        super(context, prompt);

        if (prompt.getAnswerValue() != null) {
            if (this instanceof ItemsetWidget) {
                selectedValue = prompt.getAnswerValue().getDisplayText();
            } else { // Regular SelectOneWidget
                selectedValue = ((Selection) prompt.getAnswerValue().getValue()).getValue();
            }
        }

        this.autoAdvance = autoAdvance;

        if (context instanceof AdvanceToNextListener) {
            listener = (AdvanceToNextListener) context;
        }
    }

    @Override
    public void clearAnswer() {
        if (adapter != null) {
            adapter.clearAnswer();
        }

        widgetValueChanged();
    }

    @Override
    public IAnswerData getAnswer() {
        SelectChoice selectChoice =  adapter.getSelectedItem();

        return selectChoice == null
                ? null
                : this instanceof ItemsetWidget
                    ? new StringData(selectChoice.getValue())
                    : new SelectOneData(new Selection(selectChoice));
    }

    protected void createLayout() {
        adapter = new SelectOneListAdapter(items, selectedValue, this, numColumns);

        if (items != null) {
            RecyclerView recyclerView = setUpRecyclerView();
            recyclerView.setAdapter(adapter);
            answerLayout.addView(recyclerView);
            adjustRecyclerViewSize(adapter, recyclerView);
            addAnswerView(answerLayout);
        }
    }

    /**
     * It's needed only for external choices. Everything works well and
     * out of the box when we use internal choices instead
     */
    public void clearNextLevelsOfCascadingSelect() {
        FormController formController = Collect.getInstance().getFormController();
        if (formController == null) {
            return;
        }

        if (formController.currentCaptionPromptIsQuestion()) {
            try {
                FormIndex startFormIndex = formController.getQuestionPrompt().getIndex();
                formController.stepToNextScreenEvent();
                while (formController.currentCaptionPromptIsQuestion()
                        && formController.getQuestionPrompt().getFormElement().getAdditionalAttribute(null, "query") != null) {
                    formController.saveAnswer(formController.getQuestionPrompt().getIndex(), null);
                    formController.stepToNextScreenEvent();
                }
                formController.jumpToIndex(startFormIndex);
            } catch (JavaRosaException e) {
                Timber.d(e);
            }
        }
    }

    public void onClick() {
        if (autoAdvance && listener != null) {
            listener.advance();
        }

        widgetValueChanged();
    }

    public boolean isAutoAdvance() {
        return autoAdvance;
    }

    @Override
    public int getChoiceCount() {
        return adapter.getItemCount();
    }

    @Override
    public void setChoiceSelected(int choiceIndex, boolean isSelected) {
        RadioButton button = new RadioButton(getContext());
        button.setTag(choiceIndex);
        button.setChecked(isSelected);

        adapter.onCheckedChanged(button, isSelected);
    }
}
