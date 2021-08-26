/*
 * Copyright 2017 Nafundi
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

package org.odk.collect.android.utilities;

import android.content.Context;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.MultipleItemsData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.fastexternalitemset.ItemsetDao;
import org.odk.collect.android.fastexternalitemset.ItemsetDbAdapter;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.widgets.utilities.DateTimeWidgetUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import static org.javarosa.core.model.Constants.DATATYPE_TEXT;

public final class FormEntryPromptUtils {

    private FormEntryPromptUtils() {
    }

    public static String getAnswerText(FormEntryPrompt fep, Context context, FormController formController) {
        IAnswerData data = fep.getAnswerValue();
        final String appearance = fep.getQuestion().getAppearanceAttr();

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

    public static String markQuestionIfIsRequired(String questionText, boolean isRequired) {
        if (isRequired) {
            if (questionText == null) {
                questionText = "";
            }
            questionText = "<span style=\"color:#F44336\">*</span> " + questionText;
        }

        return questionText;
    }

    @Nullable
    public static String getAttributeValue(FormEntryPrompt prompt, String attributeName) {
        List<TreeElement> attributes = prompt.getBindAttributes();
        Optional<TreeElement> attribute = attributes.stream().filter(attr ->
                attr.getName().equals(attributeName)
        ).findAny();

        return attribute.map(TreeElement::getAttributeValue).orElse(null);
    }
}
