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
import android.content.Context;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.odk.collect.android.audio.Clip;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.warnings.SpacesInUnderlyingValuesWarning;

import java.util.ArrayList;
import java.util.List;

import static org.odk.collect.android.formentry.media.FormMediaHelpers.getClip;

@SuppressLint("ViewConstructor")
public class GridMultiWidget extends BaseGridWidget {

    public GridMultiWidget(Context context, QuestionDetails questionDetails) {
        super(context, questionDetails, false);
        SpacesInUnderlyingValuesWarning.forQuestionWidget(this).renderWarningIfNecessary(items);
    }

    @Override
    protected void fillInAnswer() {
        List<Selection> answerList = getFormEntryPrompt().getAnswerValue() == null || getFormEntryPrompt().getAnswerValue().getValue() == null
                ? new ArrayList<>()
                : (List<Selection>) getFormEntryPrompt().getAnswerValue().getValue();

        for (int i = 0; i < items.size(); i++) {
            for (Selection answer : answerList) {
                if (items.get(i).getValue().equals(answer.getValue())) {
                    selectItem(i);
                    break;
                }
            }
        }
    }

    @Override
    protected void onItemClick(int index) {
        if (selectedItems.contains(index)) {
            selectedItems.remove(Integer.valueOf(index));
            if (noButtonsMode) {
                itemViews[index].setBackgroundColor(0);
                getAudioHelper().stop();
            }
        } else {
            selectedItems.add(index);
            if (noButtonsMode) {
                itemViews[index].setBackgroundColor(bgOrange);

                SelectChoice item = items.get(index);
                Clip clip = getClip(getFormEntryPrompt(), item, getReferenceManager());

                if (clip != null) {
                    getAudioHelper().play(clip);
                }
            }
        }
        widgetValueChanged();
    }

    @Override
    public IAnswerData getAnswer() {
        List<Selection> answers = new ArrayList<>();
        for (int selectedItem : selectedItems) {
            SelectChoice sc = items.get(selectedItem);
            answers.add(new Selection(sc));
        }
        return answers.isEmpty() ? null : new SelectMultiData(answers);
    }

    @Override
    public void setChoiceSelected(int choiceIndex, boolean isSelected) {
    }
}
