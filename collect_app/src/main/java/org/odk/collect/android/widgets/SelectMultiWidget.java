/*
 * Copyright (C) 2009 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.adapters.SelectMultipleListAdapter;
import org.odk.collect.android.widgets.interfaces.MultiChoiceWidget;
import org.odk.collect.android.widgets.warnings.SpacesInUnderlyingValuesWarning;

import java.util.ArrayList;
import java.util.List;

/**
 * SelectMultiWidget handles multiple selection fields using checkboxes.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class SelectMultiWidget extends SelectTextWidget implements MultiChoiceWidget {
    private final List<Selection> ve;
    SelectMultipleListAdapter adapter;

    public SelectMultiWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        //noinspection unchecked
        ve = getFormEntryPrompt().getAnswerValue() == null ? new ArrayList<>() :
                (List<Selection>) getFormEntryPrompt().getAnswerValue().getValue();
        createLayout();
    }

    @Override
    public void clearAnswer() {
        adapter.clearAnswer();
    }

    @Override
    public IAnswerData getAnswer() {
        List<Selection> vc = adapter.getSelectedItems();
        return vc.isEmpty() ? null : new SelectMultiData(vc);
    }

    private void createLayout() {
        adapter = new SelectMultipleListAdapter(items, ve, this, numColumns);

        if (items != null) {
            // check if any values have spaces
            SpacesInUnderlyingValuesWarning.forQuestionWidget(this).renderWarningIfNecessary(items);

            RecyclerView recyclerView = setUpRecyclerView();
            recyclerView.setAdapter(adapter);
            answerLayout.addView(recyclerView);
            adjustRecyclerViewSize(adapter, recyclerView);
            addAnswerView(answerLayout);
        }
    }

    @Override
    public int getChoiceCount() {
        return adapter.getItemCount();
    }

    @Override
    public void setChoiceSelected(int choiceIndex, boolean isSelected) {
        if (isSelected) {
            adapter.addItem(items.get(choiceIndex).selection());
        } else {
            adapter.removeItem(items.get(choiceIndex).selection());
        }
    }
}