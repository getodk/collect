package org.odk.collect.android.widgets.support

import org.javarosa.core.model.SelectChoice
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.widgets.interfaces.SelectChoiceLoader

class FormEntryPromptSelectChoiceLoader : SelectChoiceLoader {

    override fun loadSelectChoices(prompt: FormEntryPrompt): List<SelectChoice> {
        return prompt.selectChoices
    }
}
