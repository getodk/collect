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
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.logic.HierarchyElement;
import org.odk.collect.android.utilities.HtmlUtils;

import java.util.List;

public class HierarchyListAdapter extends RecyclerView.Adapter<HierarchyListAdapter.ViewHolder> {

    private final OnElementClickListener listener;

    private final List<HierarchyElement> hierarchyElements;

    public HierarchyListAdapter(List<HierarchyElement> listElements, OnElementClickListener listener) {
        this.hierarchyElements = listElements;
        this.listener = listener;
    }

    @Override
    public HierarchyListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.hierarchy_element, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(hierarchyElements.get(position), listener);
        if (hierarchyElements.get(position).getIcon() != null) {
            holder.icon.setVisibility(View.VISIBLE);
            holder.icon.setImageDrawable(hierarchyElements.get(position).getIcon());
        } else {
            holder.icon.setVisibility(View.GONE);
        }
        holder.primaryText.setText(HtmlUtils.textToHtml(hierarchyElements.get(position).getPrimaryText()));
        if (hierarchyElements.get(position).getSecondaryText() != null && !hierarchyElements.get(position).getSecondaryText().isEmpty()) {
            holder.secondaryText.setVisibility(View.VISIBLE);
            holder.secondaryText.setText(HtmlUtils.textToHtml(hierarchyElements.get(position).getSecondaryText()));
        } else {
            holder.secondaryText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return hierarchyElements.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView primaryText;
        TextView secondaryText;

        ViewHolder(View v) {
            super(v);
            icon = v.findViewById(R.id.icon);
            primaryText = v.findViewById(R.id.primary_text);
            secondaryText = v.findViewById(R.id.secondary_text);
        }

        void bind(final HierarchyElement element, final OnElementClickListener listener) {
            itemView.setOnClickListener(v -> listener.onElementClick(element));
        }
    }

    public interface OnElementClickListener {
        void onElementClick(HierarchyElement element);
    }
}
