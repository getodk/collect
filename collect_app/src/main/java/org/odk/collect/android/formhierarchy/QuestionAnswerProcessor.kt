package org.odk.collect.android.formhierarchy

import android.content.Context
import org.javarosa.core.model.Constants
import org.javarosa.core.model.data.DateData
import org.javarosa.core.model.data.DateTimeData
import org.javarosa.core.model.data.MultipleItemsData
import org.javarosa.core.model.data.helper.Selection
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.fastexternalitemset.ItemsetDao
import org.odk.collect.android.fastexternalitemset.ItemsetDbAdapter
import org.odk.collect.android.javarosawrapper.FormController
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.widgets.utilities.DateTimeWidgetUtils
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Date

object QuestionAnswerProcessor {
    @JvmStatic
    fun getQuestionAnswer(fep: FormEntryPrompt, context: Context, formController: FormController): String {
        val appearance: String? = fep.question.appearanceAttr
        if (appearance == Appearances.PRINTER) {
            return ""
        }

        if (!fep.answerText.isNullOrBlank() &&
            Appearances.isMasked(fep) &&
            fep.controlType == Constants.CONTROL_INPUT &&
            (
                fep.dataType == Constants.DATATYPE_TEXT ||
                    fep.dataType == Constants.DATATYPE_INTEGER ||
                    fep.dataType == Constants.DATATYPE_DECIMAL
                )
        ) {
            return "••••••••••"
        }

        val data = fep.answerValue
        if (data is MultipleItemsData) {
            val answerText = StringBuilder()
            val values = data.getValue() as List<Selection>
            for (value in values) {
                if (fep.controlType == Constants.CONTROL_RANK) {
                    answerText
                        .append(values.indexOf(value) + 1)
                        .append(". ")
                }
                answerText.append(fep.getSelectItemText(value))

                if (values.size - 1 > values.indexOf(value)) {
                    answerText.append(", ")
                }
            }
            return answerText.toString()
        }

        if (data is DateTimeData) {
            return DateTimeWidgetUtils.getDateTimeLabel(
                data.getValue() as Date,
                DateTimeWidgetUtils.getDatePickerDetails(appearance),
                true,
                context
            )
        }

        if (data is DateData) {
            return DateTimeWidgetUtils.getDateTimeLabel(
                data.getValue() as Date,
                DateTimeWidgetUtils.getDatePickerDetails(appearance),
                false,
                context
            )
        }

        if (data != null && appearance != null && appearance.contains(Appearances.THOUSANDS_SEP)) {
            return try {
                val answerAsDecimal = BigDecimal(fep.answerText)

                val df = DecimalFormat()
                df.groupingSize = 3
                df.isGroupingUsed = true
                df.maximumFractionDigits = Int.MAX_VALUE

                // Use . as decimal marker for consistency with DecimalWidget
                val customFormat = DecimalFormatSymbols()
                customFormat.decimalSeparator = '.'

                if (df.decimalFormatSymbols.groupingSeparator == '.') {
                    customFormat.groupingSeparator = ' '
                }

                df.decimalFormatSymbols = customFormat
                df.format(answerAsDecimal)
            } catch (e: NumberFormatException) {
                fep.answerText
            }
        }

        if (data != null && fep.dataType == Constants.DATATYPE_TEXT && fep.question.getAdditionalAttribute(null, "query") != null) { // ItemsetWidget
            var language: String? = ""
            if (formController.getLanguages() != null && formController.getLanguages()!!.isNotEmpty()) {
                language = formController.getLanguage()
            }
            return ItemsetDao(ItemsetDbAdapter()).getItemLabel(fep.answerValue!!.displayText, formController.getMediaFolder()!!.absolutePath, language)
        }
        return fep.answerText ?: ""
    }
}
