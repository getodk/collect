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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.Button;
import android.widget.FrameLayout;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.RankingListAdapter;
import org.odk.collect.android.external.ExternalDataUtil;
import org.odk.collect.android.utilities.RankingItemTouchHelperCallback;
import org.odk.collect.android.widgets.warnings.SpacesInUnderlyingValuesWarning;

import java.util.ArrayList;
import java.util.List;

public class RankingWidget extends QuestionWidget {

    private List<SelectChoice> items;
    private RankingListAdapter rankingListAdapter;
    private FrameLayout widgetLayout;
    private boolean nullValue;

    public RankingWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        readItems();
        setUpLayout(getOrderedItems());
    }

    @Override
    public IAnswerData getAnswer() {
        List<Selection> orderedItems = new ArrayList<>();
        for (SelectChoice selectChoice : rankingListAdapter.getItems()) {
            orderedItems.add(new Selection(selectChoice));
        }

        return nullValue ? null : new SelectMultiData(orderedItems);
    }

    @Override
    public void clearAnswer() {
        nullValue = true;
        removeView(widgetLayout);
        setUpLayout(items);
    }

    @Override
    public void setFocus(Context context) {
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
    }

    private void readItems() {
        // SurveyCTO-added support for dynamic select content (from .csv files)
        XPathFuncExpr xpathFuncExpr = ExternalDataUtil.getSearchXPathExpression(getFormEntryPrompt().getAppearanceHint());
        if (xpathFuncExpr != null) {
            items = ExternalDataUtil.populateExternalChoices(getFormEntryPrompt(), xpathFuncExpr);
        } else {
            items = getFormEntryPrompt().getSelectChoices();
        }
    }

    private List<SelectChoice> getOrderedItems() {
        List<Selection> savedOrderedItems =
                getFormEntryPrompt().getAnswerValue() == null
                ? new ArrayList<>()
                : (List<Selection>) getFormEntryPrompt().getAnswerValue().getValue();

        if (savedOrderedItems.isEmpty()) {
            nullValue = true;
            return items;
        } else {
            List<SelectChoice> orderedItems = new ArrayList<>();
            for (Selection selection : savedOrderedItems) {
                for (SelectChoice selectChoice : items) {
                    if (selection.getValue().equals(selectChoice.getValue())) {
                        orderedItems.add(selectChoice);
                        break;
                    }
                }
            }

            for (SelectChoice selectChoice : items) {
                if (!orderedItems.contains(selectChoice)) {
                    orderedItems.add(selectChoice);
                }
            }

            return orderedItems;
        }
    }

    private void setUpLayout(List<SelectChoice> items) {
        rankingListAdapter = new RankingListAdapter(items, getFormEntryPrompt());

        RecyclerView recyclerView = new RecyclerView(getContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(rankingListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setVisibility(nullValue ? GONE : VISIBLE);

        ItemTouchHelper.Callback callback = new RankingItemTouchHelperCallback(rankingListAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        Button startRankingButton = getSimpleButton(getContext().getString(R.string.start_ranking));
        startRankingButton.setVisibility(nullValue ? VISIBLE : GONE);
        startRankingButton.setOnClickListener(view -> {
            nullValue = false;
            recyclerView.setVisibility(VISIBLE);
            startRankingButton.setVisibility(GONE);
        });

        widgetLayout = new FrameLayout(getContext());
        widgetLayout.addView(startRankingButton);
        widgetLayout.addView(recyclerView);

        addAnswerView(widgetLayout);
        SpacesInUnderlyingValuesWarning
                .forQuestionWidget(this)
                .renderWarningIfNecessary(items);
    }
}
