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
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TableLayout;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.utilities.ViewIds;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A base widget class which is responsible for sharing the code used by simple select widgets like
 * {@link AbstractSelectOneWidget} and {@link SelectMultiWidget}.
 */
public abstract class SelectTextWidget extends SelectWidget {
    private static final String SEARCH_TEXT = "search_text";

    protected EditText searchStr;

    public SelectTextWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
    }

    @Override
    protected void saveState() {
        super.saveState();
        if (searchStr != null) {
            getState().putString(SEARCH_TEXT + getFormEntryPrompt().getIndex(), searchStr.getText().toString());
        }
    }

    protected void setUpSearchBox() {
        searchStr = new EditText(getContext());
        searchStr.setId(ViewIds.generateViewId());
        searchStr.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getAnswerFontSize());

        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);
        searchStr.setLayoutParams(params);
        setupChangeListener();
        addAnswerView(searchStr);

        String searchText = null;
        if (getState() != null) {
            searchText = getState().getString(SEARCH_TEXT + getFormEntryPrompt().getIndex());
        }
        if (searchText != null && !searchText.isEmpty()) {
            searchStr.setText(searchText);
            Selection.setSelection(searchStr.getText(), searchStr.getText().toString().length());
        } else {
            doSearch("");
        }
    }

    private void setupChangeListener() {
        searchStr.addTextChangedListener(new TextWatcher() {
            private String oldText = "";

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(oldText)) {
                    doSearch(s.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                oldText = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    protected void doSearch(String searchStr) {
        // First check if there is nothing on search
        if (searchStr == null || searchStr.trim().length() == 0) {
            createFilteredOptions(items, null);
        } else { // Create a List with items that are relevant to the search text
            List<SelectChoice> searchedItems = new ArrayList<>();
            List<Integer> tagList = new ArrayList<>();
            searchStr = searchStr.toLowerCase(Locale.US);
            for (int i = 0; i < items.size(); i++) {
                String choiceText = getFormEntryPrompt().getSelectChoiceText(items.get(i)).toLowerCase(Locale.US);
                if (choiceText.contains(searchStr)) {
                    searchedItems.add(items.get(i));
                    tagList.add(i);
                }
            }
            createFilteredOptions(searchedItems, tagList);
        }
    }

    private void createFilteredOptions(List<SelectChoice> searchedItems, List<Integer> tagList) {
        removeView(answerLayout);
        answerLayout.removeAllViews();

        if (searchedItems != null && !searchedItems.isEmpty()) {
            addButtonsToLayout(tagList);
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.BELOW, searchStr.getId());
        params.setMargins(10, 0, 10, 0);
        addView(answerLayout, params);
    }

    protected void addButtonsToLayout(List<Integer> tagList) {
    }
}