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

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.listeners.RecyclerViewClickListener;
import org.odk.collect.android.utilities.ApplicationConstants;

public class SortDialogAdapter extends RecyclerView.Adapter<SortDialogAdapter.ViewHolder> {
    private final RecyclerViewClickListener listener;
    private final int selectedSortingOrder;
    private String[] sortList;

    public SortDialogAdapter(String[] sortList, int selectedSortingOrder, RecyclerViewClickListener recyclerViewClickListener) {
        this.sortList = sortList;
        this.selectedSortingOrder = selectedSortingOrder;
        listener = recyclerViewClickListener;
    }

    @Override
    public SortDialogAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sort_item_layout, parent, false);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        if (position == selectedSortingOrder) {
            viewHolder.txtViewTitle.setTypeface(null, Typeface.BOLD);
        }

        viewHolder.txtViewTitle.setText(sortList[position]);
        viewHolder.imgViewIcon.setImageResource(ApplicationConstants.getSortIcon(sortList[position]));
    }

    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return sortList.length;
    }

    // inner class to hold a reference to each item of RecyclerView 
    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtViewTitle;
        ImageView imgViewIcon;

        ViewHolder(final View itemLayoutView) {
            super(itemLayoutView);
            txtViewTitle = (TextView) itemLayoutView.findViewById(R.id.title);
            imgViewIcon = (ImageView) itemLayoutView.findViewById(R.id.icon);

            itemLayoutView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(ViewHolder.this, getLayoutPosition());
                }
            });
        }
    }
}
