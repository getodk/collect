/*
 * Copyright 2017 Nafundi
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
import android.widget.LinearLayout;

import org.javarosa.core.model.SelectChoice;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.views.ChoicesRecyclerView;

import static org.odk.collect.android.analytics.AnalyticsEvents.AUDIO_QUESTION;
import static org.odk.collect.android.formentry.media.FormMediaUtils.getPlayableAudioURI;

public abstract class SelectWidget extends ItemsWidget {
    LinearLayout answerLayout;
    protected int numColumns = 1;

    public SelectWidget(Context context, QuestionDetails questionDetails) {
        super(context, questionDetails);
        answerLayout = new LinearLayout(context);
        answerLayout.setOrientation(LinearLayout.VERTICAL);

        logAnalytics(questionDetails);
    }

    @Override
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    public void setOnLongClickListener(OnLongClickListener l) {
    }

    protected ChoicesRecyclerView setUpRecyclerView(AbstractSelectListAdapter adapter) {
        boolean isFlex = WidgetAppearanceUtils.isFlexAppearance(getFormEntryPrompt());
        numColumns = WidgetAppearanceUtils.getNumberOfColumns(getFormEntryPrompt(), getContext());

        return new ChoicesRecyclerView(getContext(), adapter, isFlex, numColumns);
    }

    private void logAnalytics(QuestionDetails questionDetails) {
        if (items != null) {
            for (SelectChoice choice : items) {
                String audioURI = getPlayableAudioURI(questionDetails.getPrompt(), choice, getReferenceManager());

                if (audioURI != null) {
                    analytics.logEvent(AUDIO_QUESTION, "AudioChoice", questionDetails.getFormAnalyticsID());
                    break;
                }
            }
        }
    }
}
