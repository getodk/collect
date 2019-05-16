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

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.helper.Selection;
import org.odk.collect.android.views.MediaLayout;
import org.odk.collect.android.widgets.SelectWidget;
import org.odk.collect.android.R;

import java.util.List;

public class SelectMultipleListAdapter extends AbstractSelectListAdapter {

    private final List<Selection> selectedItems;

    public SelectMultipleListAdapter(List<SelectChoice> items, List<Selection> selectedItems, SelectWidget widget, int numColumns) {
        super(items, widget, numColumns);
        this.selectedItems = selectedItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(noButtonsMode
                ? LayoutInflater.from(parent.getContext()).inflate(R.layout.select_item_layout, null)
                : new MediaLayout(parent.getContext()));
    }

    class ViewHolder extends AbstractSelectListAdapter.ViewHolder {
        ViewHolder(View v) {
            super(v);
            if (noButtonsMode) {
                view = (FrameLayout) v;
            } else {
                mediaLayout = (MediaLayout) v;
                widget.initMediaLayoutSetUp(mediaLayout);
            }
        }

        void bind(final int index) {
            super.bind(index);
            if (noButtonsMode) {
                for (Selection selectedItem : selectedItems) {
                    if (filteredItems.get(index).getValue().equals(selectedItem.getValue())) {
                        view.getChildAt(0).setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.select_item_border));
                        break;
                    }
                }
            }
        }
    }

    @Override
    CheckBox setUpButton(final int index) {
        AppCompatCheckBox checkBox = new AppCompatCheckBox(widget.getContext());
        adjustButton(checkBox, index);
        checkBox.setOnClickListener(v -> {
            onItemClick(filteredItems.get(index).selection(), null);
            widget.widgetValueChanged();
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

    @Override
    void onItemClick(Selection selection, View view) {
        if (isItemSelected(selectedItems, selection)) {
            removeItem(selection);
            if (view != null) {
                view.setBackground(null);
            }
        } else {
            addItem(selection);
            if (view != null) {
                view.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.select_item_border));
            }
        }
    }

    public void addItem(Selection item) {
        if (!isItemSelected(selectedItems, item)) {
            selectedItems.add(item);
        }
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
        widget.widgetValueChanged();
    }

    public List<Selection> getSelectedItems() {
        return selectedItems;
    }
}