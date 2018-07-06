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

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.ThemeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RankingListAdapter extends RecyclerView.Adapter<RankingListAdapter.ItemViewHolder> {

    private final List<SelectChoice> items;
    private final FormEntryPrompt formEntryPrompt;

    public RankingListAdapter(List<SelectChoice> items, FormEntryPrompt formEntryPrompt) {
        this.items = new ArrayList<>(items);
        this.formEntryPrompt = formEntryPrompt;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(parent.getContext(), LayoutInflater.from(parent.getContext()).inflate(R.layout.ranking_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, int position) {
        holder.textView.setText(formEntryPrompt.getSelectChoiceText(items.get(position)));
    }

    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(items, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<SelectChoice> getItems() {
        return items;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        final Context context;
        final TextView textView;

        ItemViewHolder(Context context, View itemView) {
            super(itemView);
            this.context = context;
            textView = itemView.findViewById(R.id.rank_item_text);
            textView.setTextSize(Collect.getQuestionFontsize());
        }

        public void onItemSelected() {
            GradientDrawable border = new GradientDrawable();
            border.setColor(new ThemeUtils(context).getPrimaryBackgroundColor());
            border.setStroke(10, ContextCompat.getColor(Collect.getInstance(), R.color.tintColor));
            itemView.setBackground(border);
        }

        public void onItemClear() {
            itemView.setBackgroundColor(new ThemeUtils(context).getPrimaryBackgroundColor());
        }
    }
}
