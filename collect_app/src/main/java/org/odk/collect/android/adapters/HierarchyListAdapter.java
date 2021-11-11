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

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.odk.collect.android.logic.HierarchyElement;

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
        return new ViewHolder(new HierarchyListItemView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HierarchyElement element = hierarchyElements.get(position);
        holder.bind(element, listener);
    }

    @Override
    public int getItemCount() {
        return hierarchyElements.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final HierarchyListItemView view;

        ViewHolder(HierarchyListItemView v) {
            super(v);
            this.view = v;
        }

        public void bind(final HierarchyElement element, final OnElementClickListener listener) {
            view.setElement(element);
            view.setOnClickListener(v -> listener.onElementClick(element));
        }
    }

    public interface OnElementClickListener {
        void onElementClick(HierarchyElement element);
    }
}
