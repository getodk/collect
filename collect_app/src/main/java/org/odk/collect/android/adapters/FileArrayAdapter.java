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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.logic.DriveListItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.support.annotation.NonNull;

public class FileArrayAdapter extends ArrayAdapter<DriveListItem> {

    private Context context;
    private List<DriveListItem> items;


    public FileArrayAdapter(Context context, List<DriveListItem> filteredDriveList) {
        super(context, R.layout.two_item_image, filteredDriveList);
        this.context = context;
        items = filteredDriveList;
    }

    private class ViewHolder {
        ImageView imageView;
        TextView text1;
        TextView text2;
        CheckBox checkBox;
    }

    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        final ViewHolder holder;
        if (row == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.two_item_image, parent, false);

            holder.imageView = row.findViewById(R.id.image);
            holder.text1 = row.findViewById(R.id.text1);
            holder.text2 = row.findViewById(R.id.text2);
            holder.checkBox = row.findViewById(R.id.checkbox);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

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
        return row;
    }
}