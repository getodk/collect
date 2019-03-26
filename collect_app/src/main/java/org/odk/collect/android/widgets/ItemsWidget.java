/*
 * Copyright 2019 Nafundi
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

package org.odk.collect.android.widgets;

import android.content.Context;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.odk.collect.android.exception.ExternalDataException;
import org.odk.collect.android.external.ExternalDataUtil;

import java.util.List;

/**
 * ItemsWidget is an abstract class used by widgets containing a list of choices.
 * Those choices might be read from a form (xml file) or an external csv file and used in questions
 * like: SelectOne, SelectMultiple, Ranking.
 */
public abstract class ItemsWidget extends QuestionWidget {
    List<SelectChoice> items;

    public ItemsWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        readItems();
    }

    protected void readItems() {
        // SurveyCTO-added support for dynamic select content (from .csv files)
        XPathFuncExpr xpathFuncExpr = ExternalDataUtil.getSearchXPathExpression(getFormEntryPrompt().getAppearanceHint());
        if (readExternalChoices(xpathFuncExpr)) {
            try {
                items = ExternalDataUtil.populateExternalChoices(getFormEntryPrompt(), xpathFuncExpr);
                addExternalChoices(items);
            } catch (ExternalDataException e) {
                items = getFormEntryPrompt().getSelectChoices();
            }
        } else {
            items = getFormEntryPrompt().getSelectChoices();
        }
    }

    private boolean readExternalChoices(XPathFuncExpr xpathFuncExpr) {
        return xpathFuncExpr != null && getFormEntryPrompt().getQuestion().getNumChoices() == 1;
    }

    private void addExternalChoices(List<SelectChoice> choices) {
        QuestionDef questionDef = getFormEntryPrompt().getQuestion();
        // The first choice in this case is just for providing headers which we need to read external items
        questionDef.removeSelectChoice(questionDef.getChoice(0));
        for (SelectChoice choice : choices) {
            questionDef.addSelectChoice(choice);
        }
    }
}
