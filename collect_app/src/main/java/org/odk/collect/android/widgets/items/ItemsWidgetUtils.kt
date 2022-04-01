package org.odk.collect.android.widgets.items

import org.javarosa.core.model.SelectChoice
import org.javarosa.form.api.FormEntryPrompt
import org.javarosa.xpath.parser.XPathSyntaxException
import org.odk.collect.android.R
import org.odk.collect.android.exception.ExternalDataException
import org.odk.collect.android.externaldata.ExternalDataUtil
import org.odk.collect.android.fastexternalitemset.ItemsetDao
import org.odk.collect.android.fastexternalitemset.ItemsetDbAdapter
import org.odk.collect.android.fastexternalitemset.XPathParseTool
import org.odk.collect.android.widgets.QuestionWidget
import java.io.FileNotFoundException

object ItemsWidgetUtils {

    @JvmStatic
    fun loadItemsAndHandleErrors(widget: QuestionWidget, prompt: FormEntryPrompt): List<SelectChoice> {
        return try {
            loadItems(prompt)
        } catch (e: FileNotFoundException) {
            widget.showWarning(widget.context.getString(R.string.file_missing, e.message))
            emptyList()
        } catch (e: XPathSyntaxException) {
            widget.showWarning(widget.context.getString(R.string.parser_exception, e.message))
            emptyList()
        } catch (e: ExternalDataException) {
            widget.showWarning(e.message)
            emptyList()
        }
    }

    @Throws(FileNotFoundException::class, XPathSyntaxException::class, ExternalDataException::class)
    private fun loadItems(prompt: FormEntryPrompt): List<SelectChoice> {
        return when {
            isFastExternalItemsetUsed(prompt) -> readFastExternalItems(prompt)
            isSearchPulldataItemsetUsed(prompt) -> readSearchPulldataItems(prompt)
            else -> prompt.selectChoices
        }
    }

    private fun isFastExternalItemsetUsed(prompt: FormEntryPrompt): Boolean {
        val questionDef = prompt.question
        return questionDef?.getAdditionalAttribute(null, "query") != null
    }

    private fun isSearchPulldataItemsetUsed(prompt: FormEntryPrompt): Boolean {
        return ExternalDataUtil.getSearchXPathExpression(prompt.appearanceHint) != null
    }

    @Throws(FileNotFoundException::class, XPathSyntaxException::class)
    private fun readFastExternalItems(prompt: FormEntryPrompt): List<SelectChoice> {
        return ItemsetDao(ItemsetDbAdapter()).getItems(prompt, XPathParseTool())
    }

    @Throws(FileNotFoundException::class, ExternalDataException::class)
    private fun readSearchPulldataItems(prompt: FormEntryPrompt): List<SelectChoice> {
        // SurveyCTO-added support for dynamic select content (from .csv files)
        val xpathFuncExpr =
            ExternalDataUtil.getSearchXPathExpression(prompt.appearanceHint)
        return ExternalDataUtil.populateExternalChoices(prompt, xpathFuncExpr)
    }
}
