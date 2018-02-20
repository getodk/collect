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
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.logic.DriveListItem;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileArrayAdapter extends RecyclerView.Adapter<FileArrayAdapter.ViewHolder> {

    private Context context;
    private List<DriveListItem> items;

    public interface OnItemClickListener {
        void onItemClick(View v, DriveListItem item);
    }

    private final OnItemClickListener listener;

    public FileArrayAdapter(Context context, List<DriveListItem> objects, OnItemClickListener listener) {
        this.context = context;
        items = objects;
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView text1;
        TextView text2;
        CheckBox checkBox;

        ViewHolder(View v) {
            super(v);
            imageView = v.findViewById(R.id.image);
            text1 = v.findViewById(R.id.text1);
            text2 = v.findViewById(R.id.text2);
            checkBox = v.findViewById(R.id.checkbox);
        }

        void bind(final DriveListItem item, final OnItemClickListener listener) {
            itemView.setOnClickListener(v -> listener.onItemClick(v, item));
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.two_item_image, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);

        final DriveListItem o = items.get(position);
        if (o != null) {

            String dateModified = null;
            if (o.getDate() != null) {
                dateModified = new SimpleDateFormat(context.getString(
                        R.string.modified_on_date_at_time), Locale.getDefault())
                        .format(new Date(o.getDate().getValue()));
            }

            if (o.getType() == DriveListItem.FILE) {
                Drawable d = ContextCompat.getDrawable(context, R.drawable.ic_file_download);
                holder.imageView.setImageDrawable(d);
                holder.checkBox.setVisibility(View.VISIBLE);
            }
            if (o.getType() == DriveListItem.DIR) {
                Drawable d = ContextCompat.getDrawable(context, R.drawable.ic_folder);
                holder.imageView.setImageDrawable(d);
                holder.checkBox.setVisibility(View.GONE);
            }

            if (holder.text1 != null) {
                holder.text1.setText(o.getName());
            }
            if (holder.text2 != null) {
                holder.text2.setText(dateModified);
            }

            //in some cases, it will prevent unwanted situations
            holder.checkBox.setOnCheckedChangeListener(null);
            //if true, your checkbox will be selected, else unselected
            holder.checkBox.setChecked(o.isSelected());
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> o.setSelected(buttonView.isChecked()));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public DriveListItem getItem(int i) {
        return items.get(i);
    }

    public void add(DriveListItem item) {
        items.add(item);
    }

    public void addAll(List<DriveListItem> items) {
        this.items.addAll(items);
    }

    public void sort(Comparator<DriveListItem> comparator) {
        Collections.sort(items, comparator);
        notifyItemRangeChanged(0, getItemCount());
    }
}