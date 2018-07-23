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
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.javarosa.core.model.SelectChoice;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.RankingListAdapter.ItemViewHolder;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.utilities.ThemeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RankingListAdapter extends Adapter<ItemViewHolder> {

    private final List<String> values;
    private final FormController formController;

    public RankingListAdapter(List<String> values) {
        this.values = new ArrayList<>(values);
        formController = Collect.getInstance().getFormController();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(parent.getContext(), LayoutInflater.from(parent.getContext()).inflate(R.layout.ranking_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, int position) {
        String itemName = formController != null
                ? formController.getQuestionPrompt().getSelectChoiceText(getItem(values.get(position)))
                : values.get(position);
        holder.textView.setText(itemName);
    }

    private SelectChoice getItem(String value) {
        for (SelectChoice item : formController.getQuestionPrompt().getSelectChoices()) {
            if (item.getValue().equals(value)) {
                return item;
            }
        }
        return null;
    }

    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(values, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public List<String> getValues() {
        return values;
    }

    public static class ItemViewHolder extends ViewHolder {

        final Context context;
        final TextView textView;
        final ThemeUtils themeUtils;

        ItemViewHolder(Context context, View itemView) {
            super(itemView);
            this.context = context;
            textView = itemView.findViewById(R.id.rank_item_text);
            textView.setTextSize(Collect.getQuestionFontsize());
            themeUtils = new ThemeUtils(context);
        }

        public void onItemSelected() {
            GradientDrawable border = new GradientDrawable();
            border.setColor(themeUtils.getRankItemColor());
            border.setStroke(10, themeUtils.getAccentColor());
            itemView.setBackground(border);
        }

        public void onItemClear() {
            itemView.setBackgroundColor(themeUtils.getRankItemColor());
        }
    }
}
