package org.odk.collect.android.formhierarchy;

import static org.javarosa.core.model.Constants.DATATYPE_TEXT;

import android.content.Context;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.MultipleItemsData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.fastexternalitemset.ItemsetDao;
import org.odk.collect.android.fastexternalitemset.ItemsetDbAdapter;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.utilities.Appearances;
import org.odk.collect.android.widgets.utilities.DateTimeWidgetUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.List;

public class QuestionAnswerProcessor {
    public static String getQuestionAnswer(FormEntryPrompt fep, Context context, FormController formController) {
        IAnswerData data = fep.getAnswerValue();
        final String appearance = fep.getQuestion().getAppearanceAttr();

        if (appearance != null && appearance.equals(Appearances.PRINTER)) {
            return "";
        }

        if (data instanceof MultipleItemsData) {
            StringBuilder answerText = new StringBuilder();
            List<Selection> values = (List<Selection>) data.getValue();
            for (Selection value : values) {
                if (fep.getControlType() == Constants.CONTROL_RANK) {
                    answerText
                            .append(values.indexOf(value) + 1)
                            .append(". ");
                }
                answerText.append(fep.getSelectItemText(value));

                if ((values.size() - 1) > values.indexOf(value)) {
                    answerText.append(", ");
                }
            }

            return answerText.toString();
        }

        if (data instanceof DateTimeData) {
            return DateTimeWidgetUtils.getDateTimeLabel((Date) data.getValue(),
                    DateTimeWidgetUtils.getDatePickerDetails(appearance), true, context);
        }

        if (data instanceof DateData) {
            return DateTimeWidgetUtils.getDateTimeLabel((Date) data.getValue(),
                    DateTimeWidgetUtils.getDatePickerDetails(appearance), false, context);
        }

        if (data != null && appearance != null && appearance.contains(Appearances.THOUSANDS_SEP)) {
            try {
                final BigDecimal answerAsDecimal = new BigDecimal(fep.getAnswerText());

                DecimalFormat df = new DecimalFormat();
                df.setGroupingSize(3);
                df.setGroupingUsed(true);
                df.setMaximumFractionDigits(Integer.MAX_VALUE);

                // Use . as decimal marker for consistency with DecimalWidget
                DecimalFormatSymbols customFormat = new DecimalFormatSymbols();
                customFormat.setDecimalSeparator('.');

                if (df.getDecimalFormatSymbols().getGroupingSeparator() == '.') {
                    customFormat.setGroupingSeparator(' ');
                }

                df.setDecimalFormatSymbols(customFormat);

                return df.format(answerAsDecimal);
            } catch (NumberFormatException e) {
                return fep.getAnswerText();
            }
        }

        if (data != null && data.getValue() != null && fep.getDataType() == DATATYPE_TEXT
                && fep.getQuestion().getAdditionalAttribute(null, "query") != null) { // ItemsetWidget

            String language = "";
            if (formController.getLanguages() != null && formController.getLanguages().length > 0) {
                language = formController.getLanguage();
            }

            return new ItemsetDao(new ItemsetDbAdapter()).getItemLabel(fep.getAnswerValue().getDisplayText(), formController.getMediaFolder().getAbsolutePath(), language);
        }

        return fep.getAnswerText();
    }
}
