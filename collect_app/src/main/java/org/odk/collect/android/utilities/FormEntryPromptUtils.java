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

import android.text.SpannableStringBuilder;

import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.form.api.FormEntryPrompt;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public final class FormEntryPromptUtils {

    private FormEntryPromptUtils() {
    }

    public static CharSequence styledQuestionText(String questionText, boolean isRequired) {
        CharSequence styledQuestionText = HtmlUtils.textToHtml(questionText);
        return isRequired
               /*
                Question text should be added first, then the asterisk mark which represents
                required questions. If the order is changed some styling might not work well.
                */
               ? new SpannableStringBuilder(styledQuestionText)
                    .insert(0, " ")
                    .insert(0, HtmlUtils.textToHtml("<span style=\"color:#F44336\">*</span>"))
               : styledQuestionText;
    }

    @Nullable
    public static String getBindAttribute(FormEntryPrompt prompt, String attributeName) {
        List<TreeElement> attributes = prompt.getBindAttributes();
        Optional<TreeElement> attribute = attributes.stream().filter(attr ->
                attr.getName().equals(attributeName)
        ).findAny();

        return attribute.map(TreeElement::getAttributeValue).orElse(null);
    }

    @Nullable
    public static String getAdditionalAttribute(FormEntryPrompt formEntryPrompt, String attributeName) {
        String value = formEntryPrompt.getQuestion().getAdditionalAttribute(null, attributeName);
        return value != null && !value.isEmpty() ? value : null;
    }
}
