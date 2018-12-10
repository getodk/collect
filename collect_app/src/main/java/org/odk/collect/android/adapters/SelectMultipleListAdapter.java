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

package org.odk.collect.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.helper.Selection;
import org.odk.collect.android.views.MediaLayout;
import org.odk.collect.android.widgets.SelectWidget;

import java.util.List;

public class SelectMultipleListAdapter extends AbstractSelectListAdapter {

    private final List<Selection> selectedItems;

    public SelectMultipleListAdapter(List<SelectChoice> items, List<Selection> selectedItems, SelectWidget widget) {
        super(items, widget);
        this.selectedItems = selectedItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(new MediaLayout(parent.getContext()));
    }

    class ViewHolder extends AbstractSelectListAdapter.ViewHolder {
        ViewHolder(View v) {
            super(v);
            mediaLayout = (MediaLayout) v;
            widget.initMediaLayoutSetUp(mediaLayout);
        }
    }

    @Override
    CheckBox setUpButton(final int index) {
        AppCompatCheckBox checkBox = new AppCompatCheckBox(widget.getContext());
        adjustButton(checkBox, index);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Selection selection = filteredItems.get(index).selection();
            if (isChecked) {
                addItem(selection);
            } else {
                removeItem(selection);
            }
        });

        for (Selection selectedItem : selectedItems) {
            // match based on value, not key
            if (filteredItems.get(index).getValue().equals(selectedItem.getValue())) {
                checkBox.setChecked(true);
                break;
            }
        }

        return checkBox;
    }

    public void addItem(Selection item) {
        for (Selection selectedItem : selectedItems) {
            if (selectedItem.getValue().equals(item.getValue())) {
                return;
            }
        }
        selectedItems.add(item);
    }

    public void removeItem(Selection item) {
        for (Selection selectedItem : selectedItems) {
            if (selectedItem.getValue().equals(item.getValue())) {
                selectedItems.remove(selectedItem);
                break;
            }
        }
    }

    public void clearAnswer() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public List<Selection> getSelectedItems() {
        return selectedItems;
    }
}