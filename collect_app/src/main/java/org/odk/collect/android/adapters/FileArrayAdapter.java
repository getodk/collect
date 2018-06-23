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
import android.support.annotation.NonNull;
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

public class FileArrayAdapter extends ArrayAdapter<DriveListItem> {

    private final List<DriveListItem> items;

    public FileArrayAdapter(Context context, List<DriveListItem> filteredDriveList) {
        super(context, R.layout.two_item_image, filteredDriveList);
        items = filteredDriveList;
    }

    private class ViewHolder {
        ImageView imageView;
        TextView text1;
        TextView text2;
        CheckBox checkBox;

        ViewHolder(View view) {
            imageView = view.findViewById(R.id.image);
            text1 = view.findViewById(R.id.text1);
            text2 = view.findViewById(R.id.text2);
            checkBox = view.findViewById(R.id.checkbox);
        }

        void onBind(DriveListItem item) {
            String dateModified = null;
            if (item.getDate() != null) {
                dateModified = new SimpleDateFormat(getContext().getString(
                        R.string.modified_on_date_at_time), Locale.getDefault())
                        .format(new Date(item.getDate().getValue()));
            }

            if (item.getType() == DriveListItem.FILE) {
                Drawable d = ContextCompat.getDrawable(getContext(), R.drawable.ic_file_download);
                imageView.setImageDrawable(d);
                checkBox.setVisibility(View.VISIBLE);
            } else {
                Drawable d = ContextCompat.getDrawable(getContext(), R.drawable.ic_folder);
                imageView.setImageDrawable(d);
                checkBox.setVisibility(View.GONE);
            }

            text1.setText(item.getName());
            text2.setText(dateModified);
            checkBox.setChecked(item.isSelected());
        }
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.two_item_image, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) convertView.getTag();
        }
        holder.onBind(items.get(position));
        return view;
    }
}