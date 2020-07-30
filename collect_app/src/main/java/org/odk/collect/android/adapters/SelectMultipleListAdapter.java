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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;

import org.javarosa.core.model.data.helper.Selection;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.AudioVideoImageTextLabel;
import org.odk.collect.android.listeners.SelectItemClickListener;
import org.odk.collect.android.logic.ChoicesRecyclerViewAdapterProps;

import java.util.List;

public class SelectMultipleListAdapter extends AbstractSelectListAdapter {

    private final List<Selection> selectedItems;
    protected SelectItemClickListener listener;

    public SelectMultipleListAdapter(List<Selection> selectedItems, SelectItemClickListener listener, ChoicesRecyclerViewAdapterProps props) {
        super(props);
        this.selectedItems = selectedItems;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(props.isNoButtonsMode()
                ? new FrameLayout(parent.getContext())
                : new AudioVideoImageTextLabel(parent.getContext()));
    }

    public void setSelectItemClickListener(SelectItemClickListener listener) {
        this.listener = listener;
    }

    class ViewHolder extends AbstractSelectListAdapter.ViewHolder {
        ViewHolder(View v) {
            super(v);
            if (props.isNoButtonsMode()) {
                view = (FrameLayout) v;
            } else {
                audioVideoImageTextLabel = (AudioVideoImageTextLabel) v;
                audioVideoImageTextLabel.setPlayTextColor(props.getPlayColor());
                audioVideoImageTextLabel.setItemClickListener(listener);
                adjustAudioVideoImageTextLabelParams();
            }
        }

        void bind(final int index) {
            super.bind(index);
            if (props.isNoButtonsMode()) {
                view.setBackground(null);
                for (Selection selectedItem : selectedItems) {
                    if (props.getFilteredItems().get(index).getValue().equals(selectedItem.getValue())) {
                        view.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.select_item_border));
                        break;
                    }
                }
            }
        }
    }

    @Override
    CheckBox createButton(final int index, ViewGroup parent) {
        AppCompatCheckBox checkBox = (AppCompatCheckBox) LayoutInflater.from(parent.getContext()).inflate(R.layout.select_multi_item, null);
        setUpButton(checkBox, index);
        checkCheckBoxIfNeeded(checkBox, index); // perform before setting onCheckedChangeListener to avoid redundant calls of its body

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                addItem(props.getFilteredItems().get(index).selection());
            } else {
                removeItem(props.getFilteredItems().get(index).selection());
            }
            listener.onItemClicked();
        });

        return checkBox;
    }

    private void checkCheckBoxIfNeeded(CheckBox checkBox, int index) {
        for (Selection selectedItem : selectedItems) {
            // match based on value, not key
            if (props.getFilteredItems().get(index).getValue().equals(selectedItem.getValue())) {
                checkBox.setChecked(true);
                break;
            }
        }
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

    @Override
    public void clearAnswer() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    @Override
    public List<Selection> getSelectedItems() {
        return selectedItems;
    }
}