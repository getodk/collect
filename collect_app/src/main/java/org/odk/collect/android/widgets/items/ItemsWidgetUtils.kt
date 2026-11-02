package org.odk.collect.android.widgets.items

import org.javarosa.core.model.SelectChoice
import org.javarosa.form.api.FormEntryPrompt
import org.javarosa.xpath.parser.XPathSyntaxException
import org.odk.collect.android.R
import org.odk.collect.android.exception.ExternalDataException
import org.odk.collect.android.widgets.QuestionWidget
import org.odk.collect.android.widgets.interfaces.SelectChoiceLoader
import java.io.FileNotFoundException

object ItemsWidgetUtils {

    @JvmStatic
    fun loadItemsAndHandleErrors(
        widget: QuestionWidget,
        prompt: FormEntryPrompt,
        selectChoiceLoader: SelectChoiceLoader
    ): List<SelectChoice> {
        return try {
            val choices = selectChoiceLoader.loadSelectChoices(prompt)

            val choicesWithValue = choices.filterNot { it.value.isNullOrEmpty() }
            val skippedCount = choices.size - choicesWithValue.size
            if (skippedCount > 0) {
                widget.showWarning(widget.context.resources.getQuantityString(org.odk.collect.strings.R.plurals.choices_without_value_skipped, skippedCount, skippedCount))
            }

            choicesWithValue
        } catch (e: FileNotFoundException) {
            widget.showWarning(widget.context.getString(org.odk.collect.strings.R.string.file_missing, e.message))
            emptyList()
        } catch (e: XPathSyntaxException) {
            widget.showWarning(widget.context.getString(org.odk.collect.strings.R.string.parser_exception, e.message))
            emptyList()
        } catch (e: ExternalDataException) {
            widget.showWarning(e.message)
            emptyList()
        }
    }
}
