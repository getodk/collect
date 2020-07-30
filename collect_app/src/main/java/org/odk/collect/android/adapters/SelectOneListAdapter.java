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
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.helper.Selection;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.AudioVideoImageTextLabel;
import org.odk.collect.android.listeners.SelectOneItemClickListener;
import org.odk.collect.android.logic.ChoicesRecyclerViewAdapterProps;

import java.util.Collections;
import java.util.List;

public class SelectOneListAdapter extends AbstractSelectListAdapter implements CompoundButton.OnCheckedChangeListener {
    private String selectedValue;
    private RadioButton selectedRadioButton;
    private View selectedItem;
    private SelectOneItemClickListener listener;

    public SelectOneListAdapter(String selectedValue, SelectOneItemClickListener listener, ChoicesRecyclerViewAdapterProps props) {
        super(props);
        this.selectedValue = selectedValue;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(props.isNoButtonsMode()
                ? new FrameLayout(parent.getContext())
                : new AudioVideoImageTextLabel(parent.getContext()));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (selectedRadioButton != null && buttonView != selectedRadioButton) {
                selectedRadioButton.setChecked(false);
                listener.onClearNextLevelsOfCascadingSelect();
            }
            selectedRadioButton = (RadioButton) buttonView;
            selectedValue = props.getItems().get((int) selectedRadioButton.getTag()).getValue();
        }
    }

    public void setSelectItemClickListener(SelectOneItemClickListener listener) {
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
                if (props.getFilteredItems().get(index).getValue().equals(selectedValue)) {
                    view.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.select_item_border));
                    selectedItem = view;
                } else {
                    view.setBackground(null);
                }
            }
        }
    }

    @Override
    RadioButton createButton(final int index, ViewGroup parent) {
        RadioButton radioButton = (RadioButton) LayoutInflater.from(parent.getContext()).inflate(R.layout.select_one_item, null);
        setUpButton(radioButton, index);
        radioButton.setOnCheckedChangeListener(this);

        String value = props.getFilteredItems().get(index).getValue();

        if (value != null && value.equals(selectedValue)) {
            radioButton.setChecked(true);
        }
        return radioButton;
    }

    @Override
    void onItemClick(Selection selection, View view) {
        if (!selection.getValue().equals(selectedValue)) {
            if (selectedItem != null) {
                selectedItem.setBackground(null);
            }
            view.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.select_item_border));
            selectedItem = view;
            selectedValue = selection.getValue();
        }
        listener.onItemClicked();
    }

    @Override
    public List<Selection> getSelectedItems() {
        return getSelectedItem() == null
                ? null
                : Collections.singletonList(getSelectedItem());
    }

    @Override
    public void clearAnswer() {
        if (selectedRadioButton != null) {
            selectedRadioButton.setChecked(false);
        }
        selectedValue = null;
        if (selectedItem != null) {
            selectedItem.setBackground(null);
            selectedItem = null;
        }
        listener.onClearNextLevelsOfCascadingSelect();
    }

    public Selection getSelectedItem() {
        if (selectedValue != null) {
            for (SelectChoice item : props.getItems()) {
                if (selectedValue.equalsIgnoreCase(item.getValue())) {
                    return item.selection();
                }
            }
        }
        return null;
    }
}