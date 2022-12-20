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
package org.odk.collect.android.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.databinding.AboutItemLayoutBinding

class AboutListAdapter(
    private val items: Array<IntArray>,
    private val listener: AboutItemClickListener
) : RecyclerView.Adapter<AboutListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AboutItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            binding.root.setOnClickListener {
                listener.onClick(position)
            }
            binding.imageView.setImageResource(items[position][0])
            binding.imageView.tag = items[position][0]
            binding.title.text = holder.binding.root.context.getString(items[position][1])
            binding.summary.text = holder.binding.root.context.getString(items[position][2])
        }
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(val binding: AboutItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}

interface AboutItemClickListener {
    fun onClick(position: Int)
}
