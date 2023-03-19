package org.odk.collect.android.formentry.questions;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.widgets.QuestionWidget;

/**
 * Data class representing a "question" for use with {@link QuestionWidget}
 * and its subclasses
 */
public class QuestionDetails {

    private final FormEntryPrompt prompt;
    private final boolean isReadOnly;

    public QuestionDetails(FormEntryPrompt prompt) {
        this(prompt, false);
    }

    public QuestionDetails(FormEntryPrompt prompt, boolean readOnlyOverride) {
        this.prompt = prompt;
        this.isReadOnly = readOnlyOverride || prompt.isReadOnly();
    }

    public FormEntryPrompt getPrompt() {
        return prompt;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }
}
