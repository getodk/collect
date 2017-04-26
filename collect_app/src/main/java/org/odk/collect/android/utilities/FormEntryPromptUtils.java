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

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;

import java.util.Date;

public class FormEntryPromptUtils {

    public static String getAnswerText(FormEntryPrompt fep) {
        IAnswerData data = fep.getAnswerValue();
        String text;
        if (data instanceof DateTimeData) {
            text = DateTimeUtils.getDateTimeBasedOnUserLocale((Date) data.getValue(),
                    fep.getQuestion().getAppearanceAttr(), true);
        } else if (data instanceof DateData) {
            text = DateTimeUtils.getDateTimeBasedOnUserLocale((Date) data.getValue(),
                    fep.getQuestion().getAppearanceAttr(), false);
        } else {
            text = fep.getAnswerText();
        }

        return text;
    }
}
