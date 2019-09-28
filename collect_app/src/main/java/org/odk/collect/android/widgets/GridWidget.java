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
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.odk.collect.android.audio.Clip;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.AdvanceToNextListener;

import static org.odk.collect.android.formentry.media.FormMediaHelpers.getClip;

@SuppressLint("ViewConstructor")
public class GridWidget extends BaseGridWidget {

    private final AdvanceToNextListener listener;

    public GridWidget(Context context, QuestionDetails questionDetails, final boolean quickAdvance) {
        super(context, questionDetails, quickAdvance);
        listener = context instanceof AdvanceToNextListener ? (AdvanceToNextListener) context : null;
    }

    @Override
    protected void fillInAnswer() {
        String answer = getFormEntryPrompt().getAnswerValue() != null
                ? ((Selection) getFormEntryPrompt().getAnswerValue().getValue()).getValue()
                : null;

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getValue().equals(answer)) {
                selectItem(i);
            }
        }
    }

    @Override
    protected void onItemClick(int index) {
        if (!selectedItems.contains(index)) {
            if (selectedItems.isEmpty()) {
                selectItem(index);
            } else {
                unselectItem(selectedItems.get(0));
                selectItem(index);
            }
        } else {
            getAudioHelper().stop();
        }

        if (quickAdvance && listener != null) {
            listener.advance();
        }

        if (noButtonsMode) {
            SelectChoice item = items.get(index);
            Clip clip = getClip(getFormEntryPrompt(), item, getReferenceManager());

            if (clip != null) {
                getAudioHelper().play(clip);
            }
        }

        widgetValueChanged();
    }

    @Override
    public IAnswerData getAnswer() {
        return selectedItems.isEmpty()
                ? null
                : new SelectOneData(new Selection(items.get(selectedItems.get(0))));
    }

    @Override
    public void setChoiceSelected(int choiceIndex, boolean isSelected) {
    }
}
