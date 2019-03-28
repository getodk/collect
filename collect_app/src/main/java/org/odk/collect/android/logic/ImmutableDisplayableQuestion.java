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

package org.odk.collect.android.logic;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.form.api.FormEntryPrompt;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents all the user-visible aspects of a question at a particular point in time. This
 * information is generally accessed through {@link FormEntryPrompt}s which change as the form is
 * being filled and {@link org.javarosa.core.model.condition.Triggerable}s are evaluated.
 * {@link ImmutableDisplayableQuestion} objects are used to make snapshots of what the user sees.
 *
 * Inspired by https://github.com/dimagi/commcare-android/blob/fd63a5b2471a1e33ac6bf1ce77ac82daf558b08a/app/src/org/commcare/activities/components/FormRelevancyUpdating.java#L48
 */
public class ImmutableDisplayableQuestion {
    /**
     * Where in the form this question is located.
     */
    private final FormIndex index;

    /**
     * The main text of the question. This will include a prefixed asterisk if the question is
     * required.
     */
    private final String questionText;

    /**
     * The hint text of the question.
     */
    private final String helpText;

    /**
     * The answer text of the question.
     */
    private final String answerText;

    /**
     * Whether the question is read-only.
     */
    private final boolean isReadOnly;

    /**
     * The choices displayed to a user if this question is of a type that has choices.
     */
    private List<SelectChoice> selectChoices;

    /**
     * Saves all the user-visible aspects of the given {@link FormEntryPrompt}.
     */
    public ImmutableDisplayableQuestion(FormEntryPrompt question) {
        index = question.getIndex();
        questionText = question.getQuestionText();
        helpText = question.getHelpText();
        answerText = question.getAnswerText();
        isReadOnly = question.isReadOnly();

        if (question.getSelectChoices() != null) {
            selectChoices = new ArrayList<>();
            selectChoices.addAll(question.getSelectChoices());
        }
    }

    public FormIndex getFormIndex() {
        return index;
    }

    /**
     * Returns {@code true} if the provided {@link FormEntryPrompt} has the same user-visible
     * aspects, {@code false} otherwise.
     */
    public boolean sameAs(FormEntryPrompt question) {
        return question != null
                && question.getIndex().equals(index)
                && (question.getQuestionText() == null ? questionText == null : question.getQuestionText().equals(questionText))
                && (question.getHelpText() == null ? helpText == null : question.getHelpText().equals(helpText))
                && (question.getAnswerText() == null ? answerText == null : question.getAnswerText().equals(answerText))
                && (question.isReadOnly() == isReadOnly)
                && (question.getSelectChoices() == null ? selectChoices == null : question.getSelectChoices().equals(selectChoices));
    }
}
