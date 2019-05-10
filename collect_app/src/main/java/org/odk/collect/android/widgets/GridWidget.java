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
import androidx.annotation.Nullable;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.listeners.AdvanceToNextListener;

@SuppressLint("ViewConstructor")
public class GridWidget extends BaseGridWidget {

    @Nullable
    private AdvanceToNextListener listener;

    public GridWidget(Context context, FormEntryPrompt prompt, final boolean quickAdvance) {
        super(context, prompt, quickAdvance);

        if (context instanceof AdvanceToNextListener) {
            listener = (AdvanceToNextListener) context;
        }

        setUpView(prompt);
        fillInAnswer();
        initializeGridView();
    }

    void onElementClick(int position) {
        // Imitate the behavior of a radio button. Clear all buttons
        // and then check the one clicked by the user. Update the
        // background color accordingly
        for (int i = 0; i < selected.length; i++) {
            // if we have an audio handler, be sure audio is stopped.
            if (selected[i] && (audioHandlers[i] != null)) {
                stopAudio();
            }
            selected[i] = false;
            imageViews[i].setBackgroundColor(0);
        }
        selected[position] = true;
        imageViews[position].setBackgroundColor(bgOrange);

        if (quickAdvance && listener != null) {
            listener.advance();
        } else if (audioHandlers[position] != null) {
            audioHandlers[position].playAudio(getContext());
        }

        widgetValueChanged();
    }

    private void fillInAnswer() {
        String s = null;
        if (getFormEntryPrompt().getAnswerValue() != null) {
            s = ((Selection) getFormEntryPrompt().getAnswerValue().getValue()).getValue();
        }

        for (int i = 0; i < items.size(); ++i) {
            String match = items.get(i).getValue();

            selected[i] = match.equals(s);
            if (selected[i]) {
                imageViews[i].setBackgroundColor(bgOrange);
            }
        }
    }

    @Override
    public IAnswerData getAnswer() {
        for (int i = 0; i < choices.length; ++i) {
            if (selected[i]) {
                SelectChoice sc = items.get(i);
                return new SelectOneData(new Selection(sc));
            }
        }
        return null;
    }

    @Override
    public void setChoiceSelected(int choiceIndex, boolean isSelected) {
        for (int i = 0; i < selected.length; i++) {
            selected[i] = false;
        }

        selected[choiceIndex] = isSelected;
    }
}
