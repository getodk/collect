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

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.RankingListAdapter.ItemViewHolder;
import org.odk.collect.android.utilities.HtmlUtils;
import org.odk.collect.android.utilities.QuestionFontSizeUtils;
import org.odk.collect.android.utilities.ThemeUtils;

import java.util.Collections;
import java.util.List;

public class RankingListAdapter extends Adapter<ItemViewHolder> {

    private final List<SelectChoice> items;
    private final FormEntryPrompt formEntryPrompt;

    public RankingListAdapter(List<SelectChoice> items, FormEntryPrompt formEntryPrompt) {
        this.items = items;
        this.formEntryPrompt = formEntryPrompt;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.ranking_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, int position) {
        CharSequence itemName = HtmlUtils.textToHtml(formEntryPrompt.getSelectChoiceText(items.get(position)));
        holder.textView.setText(itemName);
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

    public static class ItemViewHolder extends ViewHolder {

        final TextView textView;
        final ThemeUtils themeUtils;

        ItemViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.rank_item_text);
            textView.setTextSize(QuestionFontSizeUtils.getQuestionFontSize());
            themeUtils = new ThemeUtils(itemView.getContext());
        }

        public void onItemSelected() {
            GradientDrawable border = new GradientDrawable();
            border.setStroke(10, themeUtils.getAccentColor());
            border.setColor(textView.getContext().getResources().getColor(R.color.surfaceButtonColor));
            itemView.setBackground(border);
        }

        public void onItemClear() {
            itemView.setBackgroundColor(textView.getContext().getResources().getColor(R.color.surfaceButtonColor));
        }
    }
}
