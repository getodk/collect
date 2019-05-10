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
import org.odk.collect.android.widgets.warnings.SpacesInUnderlyingValuesWarning;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("ViewConstructor")
public class GridMultiWidget extends BaseGridWidget {

    // need to remember the last click position for audio treatment
    int lastClickPosition;

    @SuppressWarnings("unchecked")
    public GridMultiWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt, false);

        setUpView(prompt);
        fillInAnswer();
        initializeGridView();
        SpacesInUnderlyingValuesWarning.forQuestionWidget(this).renderWarningIfNecessary(items);
    }

    void onElementClick(int position) {
        if (selected[position]) {
            selected[position] = false;
            imageViews[position].setBackgroundColor(0);
            if (audioHandlers[position] != null) {
                stopAudio();
            }
        } else {
            selected[position] = true;
            if (audioHandlers[lastClickPosition] != null) {
                stopAudio();
            }
            imageViews[position].setBackgroundColor(bgOrange);
            if (audioHandlers[position] != null) {
                audioHandlers[position].playAudio(getContext());
            }
            lastClickPosition = position;
        }
        widgetValueChanged();
    }

    private void fillInAnswer() {
        IAnswerData answer = getFormEntryPrompt().getAnswerValue();
        List<Selection> ve;
        if ((answer == null) || (answer.getValue() == null)) {
            ve = new ArrayList<>();
        } else {
            ve = (List<Selection>) answer.getValue();
        }

        for (int i = 0; i < choices.length; ++i) {
            String value = items.get(i).getValue();
            boolean found = false;
            for (Selection s : ve) {
                if (value.equals(s.getValue())) {
                    found = true;
                    break;
                }
            }

            selected[i] = found;
            if (selected[i]) {
                imageViews[i].setBackgroundColor(bgOrange);
            }
        }
    }

    @Override
    public IAnswerData getAnswer() {
        List<Selection> vc = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            if (selected[i]) {
                SelectChoice sc = items.get(i);
                vc.add(new Selection(sc));
            }
        }

        if (vc.isEmpty()) {
            return null;

        } else {
            return new SelectMultiData(vc);
        }
    }

    @Override
    public void setChoiceSelected(int choiceIndex, boolean isSelected) {
        selected[choiceIndex] = isSelected;
    }
}
