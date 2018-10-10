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
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.views.MediaLayout;
import org.odk.collect.android.views.ODKView;
import org.odk.collect.android.widgets.SelectWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.odk.collect.android.widgets.QuestionWidget.isRTL;

public abstract class AbstractSelectListAdapter extends RecyclerView.Adapter<AbstractSelectListAdapter.ViewHolder>
        implements Filterable {

    SelectWidget widget;
    List<SelectChoice> items;
    List<SelectChoice> filteredItems;

    AbstractSelectListAdapter(List<SelectChoice> items, SelectWidget widget) {
        this.items = items;
        this.widget = widget;
        filteredItems = items;
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
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String searchStr = charSequence.toString().toLowerCase(Locale.US);
                FilterResults filterResults = new FilterResults();
                if (searchStr.isEmpty()) {
                    filterResults.values = items;
                    filterResults.count = items.size();
                } else {
                    List<SelectChoice> filteredList = new ArrayList<>();
                    FormEntryPrompt formEntryPrompt = widget.getFormEntryPrompt();
                    for (SelectChoice item : items) {
                        if (formEntryPrompt.getSelectChoiceText(item).toLowerCase(Locale.US).contains(searchStr)) {
                            filteredList.add(item);
                        }
                    }
                    filterResults.values = filteredList;
                    filterResults.count = filteredList.size();
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredItems = (List<SelectChoice>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    abstract CompoundButton setUpButton(int index);

    void adjustButton(TextView button, int index) {
        button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Collect.getQuestionFontsize());
        button.setText(FormEntryPromptUtils.getItemText(widget.getFormEntryPrompt(), filteredItems.get(index)));
        button.setTag(items.indexOf(filteredItems.get(index)));
        button.setEnabled(!widget.getFormEntryPrompt().isReadOnly());
        button.setGravity(isRTL() ? Gravity.END : Gravity.START);
        button.setOnLongClickListener((ODKView) widget.getParent().getParent().getParent());
    }

    abstract class ViewHolder extends RecyclerView.ViewHolder {
        MediaLayout mediaLayout;

        ViewHolder(View itemView) {
            super(itemView);
        }

        void bind(final int index) {
            widget.addMediaFromChoice(mediaLayout, index, setUpButton(index));
        }
    }
}
