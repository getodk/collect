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
package org.odk.collect.android.formhierarchy

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class HierarchyListAdapter(
    private val hierarchyItems: List<HierarchyItem>,
    private val listener: OnElementClickListener
) : RecyclerView.Adapter<HierarchyListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val item = HierarchyListItemView(parent.context, viewType).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        return ViewHolder(item)
    }

    override fun getItemViewType(position: Int): Int {
        return hierarchyItems[position].hierarchyItemType.id
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(hierarchyItems[position], listener)
    }

    override fun getItemCount(): Int {
        return hierarchyItems.size
    }

    class ViewHolder(private val view: HierarchyListItemView) : RecyclerView.ViewHolder(view) {
        fun bind(element: HierarchyItem, listener: OnElementClickListener) {
            view.setElement(element)
            view.setOnClickListener { listener.onElementClick(element) }
        }
    }

    interface OnElementClickListener {
        fun onElementClick(element: HierarchyItem?)
    }
}
