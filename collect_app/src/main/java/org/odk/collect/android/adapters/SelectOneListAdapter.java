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
import android.support.v7.widget.AppCompatRadioButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;

import org.javarosa.core.model.SelectChoice;
import org.odk.collect.android.R;
import org.odk.collect.android.widgets.AbstractSelectOneWidget;

import java.util.List;

public class SelectOneListAdapter extends AbstractSelectListAdapter
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private String selectedValue;
    private RadioButton selectedRadioButton;

    public SelectOneListAdapter(List<SelectChoice> items, String selectedValue, AbstractSelectOneWidget widget) {
        super(items, widget);
        this.selectedValue = selectedValue;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.quick_select_layout, null));
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
            autoAdvanceIcon = v.findViewById(R.id.auto_advance_icon);
            autoAdvanceIcon.setVisibility(((AbstractSelectOneWidget) widget).isAutoAdvance() ? View.VISIBLE : View.GONE);
            mediaLayout = v.findViewById(R.id.mediaLayout);
            widget.initMediaLayoutSetUp(mediaLayout);
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

    public void clearAnswer() {
        if (selectedRadioButton != null) {
            selectedRadioButton.setChecked(false);
        }
        selectedValue = null;
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