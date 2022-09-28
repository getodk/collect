package org.odk.collect.android.widgets.interfaces

import org.javarosa.core.model.SelectChoice
import org.javarosa.form.api.FormEntryPrompt
import org.javarosa.xpath.parser.XPathSyntaxException
import org.odk.collect.android.exception.ExternalDataException
import java.io.FileNotFoundException

interface SelectChoiceLoader {

    @Throws(FileNotFoundException::class, XPathSyntaxException::class, ExternalDataException::class)
    fun loadSelectChoices(prompt: FormEntryPrompt): List<SelectChoice>
}
