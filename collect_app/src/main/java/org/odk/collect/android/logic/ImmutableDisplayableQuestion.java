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
     * The guidance hint text of the question.
     */
    private final String guidanceText;

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
        guidanceText = question.getSpecialFormQuestionText(question.getQuestion().getHelpTextID(), "guidance");
        answerText = question.getAnswerText();
        isReadOnly = question.isReadOnly();

        List<SelectChoice> choices = question.getSelectChoices();
        if (choices != null) {
            selectChoices = new ArrayList<>();
            selectChoices.addAll(choices);
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
                && (getGuidanceHintText(question) == null ? guidanceText == null : getGuidanceHintText(question).equals(guidanceText))
                && (question.getAnswerText() == null ? answerText == null : question.getAnswerText().equals(answerText))
                && (question.isReadOnly() == isReadOnly)
                && selectChoiceListsEqual(question.getSelectChoices(), selectChoices);
    }

    private static boolean selectChoiceListsEqual(List<SelectChoice> selectChoiceList1, List<SelectChoice> selectChoiceList2) {
        if (selectChoiceList1 == null) {
            return selectChoiceList2 == null;
        }

        if (selectChoiceList1.size() != selectChoiceList2.size()) {
            return false;
        }

        for (int i = 0; i < selectChoiceList1.size(); i++) {
            if (!selectChoicesEqual(selectChoiceList1.get(i), selectChoiceList2.get(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * TODO: move to JavaRosa
     */
    private static String getGuidanceHintText(FormEntryPrompt question) {
        return question.getSpecialFormQuestionText(question.getQuestion().getHelpTextID(), "guidance");
    }

    /**
     * Returns true if the two SelectChoice objects currently represent the same choice in the same
     * position.
     *
     * Note: this is here rather than as a .equals for SelectChoice in JavaRosa because SelectChoice
     * is mutable and keeps track of both the choice and its current position. Clients may want to
     * define equality differently for different usages.
     */
    private static boolean selectChoicesEqual(SelectChoice selectChoice1, SelectChoice selectChoice2) {
        if (selectChoice1 == null) {
            return selectChoice2 == null;
        }

        if (selectChoice1.getLabelInnerText() == null) {
            if (selectChoice2.getLabelInnerText() != null) {
                return false;
            }
        } else if (!selectChoice1.getLabelInnerText().equals(selectChoice2.getLabelInnerText())) {
            return false;
        }

        if (selectChoice1.getTextID() == null) {
            if (selectChoice2.getTextID() != null) {
                return false;
            }
        } else if (!selectChoice1.getTextID().equals(selectChoice2.getTextID())) {
            return false;
        }

        if (selectChoice1.getValue() == null) {
            if (selectChoice2.getValue() != null) {
                return false;
            }
        } else if (!selectChoice1.getValue().equals(selectChoice2.getValue())) {
            return false;
        }

        return selectChoice1.getIndex() == selectChoice2.getIndex()
                && selectChoice1.isLocalizable() == selectChoice2.isLocalizable()
                && selectChoice1.copyNode == selectChoice2.copyNode;
    }
}
