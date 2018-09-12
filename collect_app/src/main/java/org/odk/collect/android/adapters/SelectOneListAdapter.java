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
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RadioButton;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.TextUtils;
import org.odk.collect.android.views.MediaLayout;
import org.odk.collect.android.views.ODKView;
import org.odk.collect.android.widgets.AbstractSelectOneWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.odk.collect.android.widgets.QuestionWidget.isRTL;

public class SelectOneListAdapter extends RecyclerView.Adapter<SelectOneListAdapter.ViewHolder>
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, Filterable {

    private final List<SelectChoice> items;
    private final AbstractSelectOneWidget widget;
    private String selectedValue;
    private RadioButton selectedRadioButton;
    private List<SelectChoice> filteredItems;

    public SelectOneListAdapter(List<SelectChoice> items,
                                String selectedValue,
                                AbstractSelectOneWidget widget) {
        this.items = items;
        this.selectedValue = selectedValue;
        this.widget = widget;
        filteredItems = items;
    }

    @Override
    public SelectOneListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(widget.getContext()).inflate(R.layout.quick_select_layout, null));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int index) {
        holder.bind(index);
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    @Override
    public void onClick(View v) {
        widget.onClick();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (selectedRadioButton != null && buttonView != selectedRadioButton) {
                selectedRadioButton.setChecked(false);
                widget.clearNextLevelsOfCascadingSelect();
            }
            selectedRadioButton = (RadioButton) buttonView;
            selectedValue = items.get((int) selectedRadioButton.getTag()).getValue();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView autoAdvanceIcon;
        MediaLayout mediaLayout;

        ViewHolder(View v) {
            super(v);
            autoAdvanceIcon = v.findViewById(R.id.auto_advance_icon);
            autoAdvanceIcon.setVisibility(widget.isAutoAdvance() ? View.VISIBLE : View.GONE);
            mediaLayout = v.findViewById(R.id.mediaLayout);
            widget.initMediaLayoutSetUp(mediaLayout);
        }

        void bind(final int index) {
            widget.addMediaFromChoice(mediaLayout, index, createRadioButton(index));
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String searchStr = charSequence.toString().toLowerCase(Locale.US);
                if (searchStr.isEmpty()) {
                    filteredItems = items;
                } else {
                    List<SelectChoice> filteredList = new ArrayList<>();
                    FormEntryPrompt formEntryPrompt = widget.getFormEntryPrompt();
                    for (SelectChoice item : items) {
                        if (formEntryPrompt.getSelectChoiceText(item).toLowerCase(Locale.US).contains(searchStr)) {
                            filteredList.add(item);
                        }
                    }

                    filteredItems = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredItems;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredItems = (List<SelectChoice>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    private RadioButton createRadioButton(final int index) {
        AppCompatRadioButton radioButton = new AppCompatRadioButton(widget.getContext());
        radioButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Collect.getQuestionFontsize());
        radioButton.setText(getItemText(index));
        radioButton.setTag(items.indexOf(filteredItems.get(index)));
        radioButton.setEnabled(!widget.getFormEntryPrompt().isReadOnly());
        radioButton.setGravity(isRTL() ? Gravity.END : Gravity.START);
        radioButton.setOnLongClickListener((ODKView) widget.getParent().getParent());
        radioButton.setOnClickListener(this);
        radioButton.setOnCheckedChangeListener(this);

        if (filteredItems.get(index).getValue().equals(selectedValue)) {
            radioButton.setChecked(true);
        }

        return radioButton;
    }

    private String getItemText(int index) {
        String choiceName = widget.getFormEntryPrompt().getSelectChoiceText(filteredItems.get(index));
        return choiceName != null ? TextUtils.textToHtml(choiceName).toString() : "";
    }

    public void clearAnswer() {
        if (selectedRadioButton != null) {
            selectedRadioButton.setChecked(false);
        }
        selectedValue = null;
        widget.clearNextLevelsOfCascadingSelect();
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