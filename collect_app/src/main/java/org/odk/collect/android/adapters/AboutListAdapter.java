/*
 * Copyright (C) 2018 Shobhit Agarwal
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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.odk.collect.android.R;

public class AboutListAdapter extends RecyclerView.Adapter<AboutListAdapter.ViewHolder> {

    private final Context context;
    private final int[][] items;
    private final AboutItemClickListener listener;

    public AboutListAdapter(int[][] items, Context context, AboutItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @Override
    public AboutListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(context)
                .inflate(R.layout.about_item_layout, parent, false);
        return new ViewHolder(itemLayoutView, listener);
    }

    @Override
    public void onBindViewHolder(AboutListAdapter.ViewHolder holder, int position) {
        holder.imageView.setImageResource(items[position][0]);
        holder.title.setText(context.getString(items[position][1]));
        holder.setSummary(items[position][2]);
    }

    @Override
    public int getItemCount() {
        return items.length;
    }

    public interface AboutItemClickListener {
        void onClick(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final AboutItemClickListener listener;
        private final ImageView imageView;
        private final TextView title;
        private final TextView summary;

        ViewHolder(View view, AboutItemClickListener listener) {
            super(view);
            this.listener = listener;
            imageView = view.findViewById(R.id.imageView);
            title = view.findViewById(R.id.title);
            summary = view.findViewById(R.id.summary);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onClick(getAdapterPosition());
        }

        void setSummary(int resId) {
            if (resId == -1) {
                summary.setVisibility(View.GONE);
            } else {
                summary.setVisibility(View.VISIBLE);
                summary.setText(context.getString(resId));
            }
        }
    }
}
