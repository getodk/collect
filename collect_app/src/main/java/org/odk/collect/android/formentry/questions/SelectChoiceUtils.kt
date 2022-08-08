package org.odk.collect.android.formentry.questions

import org.javarosa.core.model.SelectChoice
import org.javarosa.form.api.FormEntryPrompt
import org.javarosa.xpath.parser.XPathSyntaxException
import org.odk.collect.android.exception.ExternalDataException
import org.odk.collect.android.externaldata.ExternalDataUtil
import org.odk.collect.android.fastexternalitemset.ItemsetDao
import org.odk.collect.android.fastexternalitemset.ItemsetDbAdapter
import org.odk.collect.android.fastexternalitemset.XPathParseTool
import org.odk.collect.android.javarosawrapper.FormController
import java.io.FileNotFoundException

object SelectChoiceUtils {

    @JvmStatic
    @Throws(FileNotFoundException::class, XPathSyntaxException::class, ExternalDataException::class)
    fun loadSelectChoices(prompt: FormEntryPrompt, formController: FormController): List<SelectChoice> {
        return when {
            isFastExternalItemsetUsed(prompt) -> readFastExternalItems(prompt, formController)
            isSearchPulldataItemsetUsed(prompt) -> readSearchPulldataItems(prompt, formController)
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
    private fun readFastExternalItems(prompt: FormEntryPrompt, formController: FormController): List<SelectChoice> {
        return ItemsetDao(ItemsetDbAdapter()).getItems(prompt, XPathParseTool(), formController)
    }

    @Throws(FileNotFoundException::class, ExternalDataException::class)
    private fun readSearchPulldataItems(prompt: FormEntryPrompt, formController: FormController): List<SelectChoice> {
        // SurveyCTO-added support for dynamic select content (from .csv files)
        val xpathFuncExpr =
            ExternalDataUtil.getSearchXPathExpression(prompt.appearanceHint)
        return ExternalDataUtil.populateExternalChoices(prompt, xpathFuncExpr, formController)
    }
}
