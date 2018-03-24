/*
 * Copyright (C) 2017 Shobhit
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.listeners.RecyclerViewClickListener;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.ThemeUtils;

public class SortDialogAdapter extends RecyclerView.Adapter<SortDialogAdapter.ViewHolder> {
    private final RecyclerViewClickListener listener;
    private final int selectedSortingOrder;
    private final Context context;
    private final RecyclerView recyclerView;
    private String[] sortList;

    public SortDialogAdapter(Context context, RecyclerView recyclerView, String[] sortList, int selectedSortingOrder, RecyclerViewClickListener listener) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.sortList = sortList;
        this.selectedSortingOrder = selectedSortingOrder;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SortDialogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sort_item_layout, parent, false);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.bind(sortList[position]);
    }

    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return sortList.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTitle;
        private ImageView ivIcon;

        ViewHolder(final View itemLayoutView) {
            super(itemLayoutView);
            tvTitle = itemLayoutView.findViewById(R.id.title);
            ivIcon = itemLayoutView.findViewById(R.id.icon);
            itemLayoutView.setOnClickListener(v -> listener.onItemClicked(this, getLayoutPosition()));
        }

        public void updateItemColor(int position) {
            ViewHolder previousHolder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
            previousHolder.setUnselected();
            setSelected();
        }

        void setUnselected() {
            if (ThemeUtils.isDarkTheme()) {
                tvTitle.setTextColor(context.getResources().getColor(R.color.white));
                ThemeUtils.setIconTint(context, ivIcon.getDrawable());
            } else {
                tvTitle.setTextColor(context.getResources().getColor(R.color.black));
                DrawableCompat.setTintList(ivIcon.getDrawable(), null);
            }
        }

        void setSelected() {
            tvTitle.setTextColor(context.getResources().getColor(R.color.tintColor));
            DrawableCompat.setTint(ivIcon.getDrawable(), context.getResources().getColor(R.color.tintColor));
        }

        void bind(String sortOrder) {
            tvTitle.setText(sortOrder);
            ivIcon.setImageResource(ApplicationConstants.getSortLabelToIconMap().get(sortOrder));
            ivIcon.setImageDrawable(DrawableCompat.wrap(ivIcon.getDrawable()).mutate());

            if (getAdapterPosition() == selectedSortingOrder) {
                setSelected();
            } else {
                setUnselected();
            }
        }
    }
}
