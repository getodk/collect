package org.odk.collect.android.formentry.questions;

import org.javarosa.form.api.FormEntryPrompt;

/**
 * Data class representing a "question" for use with {@link org.odk.collect.android.widgets.QuestionWidget}
 * and its subclasses
 */
public class QuestionDetails {

    private final FormEntryPrompt prompt;

    public QuestionDetails(FormEntryPrompt prompt) {
        this.prompt = prompt;
    }

    public FormEntryPrompt getPrompt() {
        return prompt;
    }
}
