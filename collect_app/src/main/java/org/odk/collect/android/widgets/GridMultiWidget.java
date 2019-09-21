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
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.audio.Clip;
import org.odk.collect.android.utilities.ScreenContext;
import org.odk.collect.android.widgets.warnings.SpacesInUnderlyingValuesWarning;

import java.util.ArrayList;
import java.util.List;

import static org.odk.collect.android.formentry.media.FormMediaHelpers.getClipID;
import static org.odk.collect.android.formentry.media.FormMediaHelpers.getPlayableAudioURI;

@SuppressLint("ViewConstructor")
public class GridMultiWidget extends BaseGridWidget {

    public GridMultiWidget(Context context, FormEntryPrompt prompt, AudioHelper audioHelper) {
        super(context, prompt, false, audioHelper);
        SpacesInUnderlyingValuesWarning.forQuestionWidget(this).renderWarningIfNecessary(items);
    }

    public GridMultiWidget(Context context, FormEntryPrompt prompt) {
        this(context, prompt, new AudioHelper(
                ((ScreenContext) context).getActivity(),
                ((ScreenContext) context).getViewLifecycle()
        ));
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
                String clipID = getClipID(getFormEntryPrompt(), item);
                String audioURI = getPlayableAudioURI(getFormEntryPrompt(), item, getReferenceManager());

                if (audioURI != null) {
                    getAudioHelper().play(new Clip(clipID, audioURI));
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
