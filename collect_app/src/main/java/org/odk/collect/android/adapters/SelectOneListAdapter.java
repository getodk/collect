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
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.helper.Selection;
import org.odk.collect.android.R;
import org.odk.collect.android.widgets.AbstractSelectOneWidget;

import java.util.List;

public class SelectOneListAdapter extends AbstractSelectListAdapter
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private String selectedValue;
    private RadioButton selectedRadioButton;
    private View selectedItem;

    public SelectOneListAdapter(List<SelectChoice> items, String selectedValue, AbstractSelectOneWidget widget, int numColumns) {
        super(items, widget, numColumns);
        this.selectedValue = selectedValue;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(noButtonsMode
                ? LayoutInflater.from(parent.getContext()).inflate(R.layout.select_item_layout, null)
                : LayoutInflater.from(parent.getContext()).inflate(R.layout.quick_select_layout, null));
    }

    @Override
    public void onClick(View v) {
        ((AbstractSelectOneWidget) widget).onClick();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (selectedRadioButton != null && buttonView != selectedRadioButton) {
                selectedRadioButton.setChecked(false);
                ((AbstractSelectOneWidget) widget).clearNextLevelsOfCascadingSelect();
            }
            selectedRadioButton = (RadioButton) buttonView;
            selectedValue = items.get((int) selectedRadioButton.getTag()).getValue();
        }
    }

    class ViewHolder extends AbstractSelectListAdapter.ViewHolder {
        ImageView autoAdvanceIcon;

        ViewHolder(View v) {
            super(v);
            if (noButtonsMode) {
                view = (FrameLayout) v;
            } else {
                autoAdvanceIcon = v.findViewById(R.id.auto_advance_icon);
                autoAdvanceIcon.setVisibility(((AbstractSelectOneWidget) widget).isAutoAdvance() ? View.VISIBLE : View.GONE);
                mediaLayout = v.findViewById(R.id.mediaLayout);
                widget.initMediaLayoutSetUp(mediaLayout);
            }
        }

        void bind(final int index) {
            super.bind(index);
            if (noButtonsMode && filteredItems.get(index).getValue().equals(selectedValue)) {
                view.getChildAt(0).setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.select_item_border));
                selectedItem = view.getChildAt(0);
            }
        }
    }

    @Override
    RadioButton setUpButton(final int index) {
        AppCompatRadioButton radioButton = new AppCompatRadioButton(widget.getContext());
        adjustButton(radioButton, index);
        radioButton.setOnClickListener(this);
        radioButton.setOnCheckedChangeListener(this);

        if (filteredItems.get(index).getValue().equals(selectedValue)) {
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
        ((AbstractSelectOneWidget) widget).onClick();
    }

    public void clearAnswer() {
        if (selectedRadioButton != null) {
            selectedRadioButton.setChecked(false);
        }
        selectedValue = null;
        if (selectedItem != null) {
            selectedItem.setBackground(null);
            selectedItem = null;
        }
        ((AbstractSelectOneWidget) widget).clearNextLevelsOfCascadingSelect();
    }

    public SelectChoice getSelectedItem() {
        if (selectedValue != null) {
            for (SelectChoice item : items) {
                if (selectedValue.equalsIgnoreCase(item.getValue())) {
                    return item;
                }
            }
        }
        return null;
    }
}