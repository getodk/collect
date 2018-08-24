/*
 * Copyright 2018 Nafundi
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.external.ExternalDataUtil;
import org.odk.collect.android.fragments.dialogs.RankingWidgetDialog;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.widgets.interfaces.BinaryWidget;
import org.odk.collect.android.widgets.warnings.SpacesInUnderlyingValuesWarning;

import java.util.ArrayList;
import java.util.List;

public class RankingWidget extends QuestionWidget implements BinaryWidget {

    private List<SelectChoice> originalItems;
    private List<SelectChoice> savedItems;
    private LinearLayout widgetLayout;
    private Button showRankingDialogButton;

    public RankingWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        readItems();
        setUpLayout(getOrderedItems());
    }

    @Override
    public IAnswerData getAnswer() {
        List<Selection> orderedItems = new ArrayList<>();
        if (savedItems != null) {
            for (SelectChoice selectChoice : savedItems) {
                orderedItems.add(new Selection(selectChoice));
            }
        }

        return orderedItems.isEmpty() ? null : new SelectMultiData(orderedItems);
    }

    @Override
    public void clearAnswer() {
        savedItems = null;
        setUpLayout(originalItems);
    }

    @Override
    public void setFocus(Context context) {
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        showRankingDialogButton.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        showRankingDialogButton.cancelLongPress();
    }

    @Override
    public void setBinaryData(Object values) {
        savedItems = getSavedItems((List<String>) values);
        setUpLayout(savedItems);
    }

    @Override
    public void onButtonClick(int buttonId) {
        FormController formController = Collect.getInstance().getFormController();
        if (formController != null) {
            formController.setIndexWaitingForData(formController.getFormIndex());
        }
        RankingWidgetDialog rankingWidgetDialog = RankingWidgetDialog.newInstance(savedItems == null
                ? getValues(originalItems)
                : getValues(savedItems));
        rankingWidgetDialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), "RankingDialog");
    }

    private List<SelectChoice> getSavedItems(List<String> values) {
        List<SelectChoice> savedItems = new ArrayList<>();
        for (String value : values) {
            for (SelectChoice item : originalItems) {
                if (item.getValue().equals(value)) {
                    savedItems.add(item);
                    break;
                }
            }
        }
        return savedItems;
    }

    private List<String> getValues(List<SelectChoice> items) {
        List<String> values = new ArrayList<>();
        for (SelectChoice item : items) {
            values.add(item.getValue());
        }
        return values;
    }

    private void readItems() {
        // SurveyCTO-added support for dynamic select content (from .csv files)
        XPathFuncExpr xpathFuncExpr = ExternalDataUtil.getSearchXPathExpression(getFormEntryPrompt().getAppearanceHint());
        if (xpathFuncExpr != null) {
            originalItems = ExternalDataUtil.populateExternalChoices(getFormEntryPrompt(), xpathFuncExpr);
        } else {
            originalItems = getFormEntryPrompt().getSelectChoices();
        }
    }

    private List<SelectChoice> getOrderedItems() {
        List<Selection> savedOrderedItems =
                getFormEntryPrompt().getAnswerValue() == null
                ? new ArrayList<>()
                : (List<Selection>) getFormEntryPrompt().getAnswerValue().getValue();

        if (savedOrderedItems.isEmpty()) {
            return originalItems;
        } else {
            savedItems = new ArrayList<>();
            for (Selection selection : savedOrderedItems) {
                for (SelectChoice selectChoice : originalItems) {
                    if (selection.getValue().equals(selectChoice.getValue())) {
                        savedItems.add(selectChoice);
                        break;
                    }
                }
            }

            for (SelectChoice selectChoice : originalItems) {
                if (!savedItems.contains(selectChoice)) {
                    savedItems.add(selectChoice);
                }
            }

            return savedItems;
        }
    }

    private void setUpLayout(List<SelectChoice> items) {
        removeView(widgetLayout);

        widgetLayout = new LinearLayout(getContext());
        widgetLayout.setOrientation(LinearLayout.VERTICAL);
        showRankingDialogButton = getSimpleButton(getContext().getString(R.string.rank_items));
        widgetLayout.addView(showRankingDialogButton);
        widgetLayout.addView(setUpAnswerTextView());

        addAnswerView(widgetLayout);
        SpacesInUnderlyingValuesWarning
                .forQuestionWidget(this)
                .renderWarningIfNecessary(items);
    }

    private TextView setUpAnswerTextView() {
        StringBuilder answerText = new StringBuilder();
        if (savedItems != null) {
            for (SelectChoice item : savedItems) {
                answerText
                        .append(savedItems.indexOf(item) + 1)
                        .append(". ")
                        .append(getFormEntryPrompt().getSelectChoiceText(item));
                if ((savedItems.size() - 1) > savedItems.indexOf(item)) {
                    answerText.append('\n');
                }
            }
        }
        return getAnswerTextView(answerText.toString());
    }
}
