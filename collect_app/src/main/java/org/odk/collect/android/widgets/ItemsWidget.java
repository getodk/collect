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

import org.javarosa.core.model.SelectChoice;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.odk.collect.android.R;
import org.odk.collect.android.external.ExternalDataUtil;
import org.odk.collect.android.utilities.ToastUtils;

import java.util.ArrayList;
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
        handleItemsWithNullValues();
    }

    protected void readItems() {
        // SurveyCTO-added support for dynamic select content (from .csv files)
        XPathFuncExpr xpathFuncExpr = ExternalDataUtil.getSearchXPathExpression(getFormEntryPrompt().getAppearanceHint());
        if (xpathFuncExpr != null) {
            items = ExternalDataUtil.populateExternalChoices(getFormEntryPrompt(), xpathFuncExpr);
        } else {
            items = getFormEntryPrompt().getSelectChoices();
        }
    }

    // Remove items with null values if exist and display a message about the fact
    private void handleItemsWithNullValues() {
        boolean itemsWithNullValuesExisted = false;
        List<SelectChoice> itemsWithoutNullValues = new ArrayList<>(items);
        for (SelectChoice item : itemsWithoutNullValues) {
            if (item.getValue() == null) {
                items.remove(item);
                itemsWithNullValuesExisted = true;
            }
        }
        if (itemsWithNullValuesExisted) {
            ToastUtils.showLongToast(R.string.item_set_with_null_values);
        }
    }
}
