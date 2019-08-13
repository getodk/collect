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
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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

import timber.log.Timber;

public class FileArrayAdapter extends ArrayAdapter<DriveListItem> {

    private final List<DriveListItem> items;

    public FileArrayAdapter(Context context, List<DriveListItem> filteredDriveList) {
        super(context, R.layout.form_chooser_list_item_multiple_choice, filteredDriveList);
        items = filteredDriveList;
    }

    private class ViewHolder {
        ImageView imageView;
        TextView formTitle;
        TextView formSubtitle;
        CheckBox checkBox;

        ViewHolder(View view) {
            imageView = view.findViewById(R.id.image);
            formTitle = view.findViewById(R.id.form_title);
            formSubtitle = view.findViewById(R.id.form_subtitle);
            checkBox = view.findViewById(R.id.checkbox);
        }

        void onBind(DriveListItem item) {
            String dateModified = null;
            if (item.getDate() != null) {
                try {
                    dateModified = new SimpleDateFormat(getContext().getString(
                            R.string.modified_on_date_at_time), Locale.getDefault())
                            .format(new Date(item.getDate().getValue()));
                } catch (IllegalArgumentException e) {
                    Timber.e(e);
                }
            }

            if (item.getType() == DriveListItem.FILE) {
                Drawable d = ContextCompat.getDrawable(getContext(), R.drawable.form_state_blank);
                imageView.setImageDrawable(d);
                checkBox.setVisibility(View.VISIBLE);
            } else {
                Drawable d = ContextCompat.getDrawable(getContext(), R.drawable.ic_folder);
                imageView.setImageDrawable(d);
                checkBox.setVisibility(View.GONE);
            }

            formTitle.setText(item.getName());
            formSubtitle.setText(dateModified);
            checkBox.setChecked(item.isSelected());
        }
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.form_chooser_list_item_multiple_choice, parent, false);
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
